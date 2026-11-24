package farit.dev.bs.network;

import farit.dev.bs.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GameClient {

    public interface LobbyListener {
        void onLobbyUpdate(List<String> names);

        void onDisconnected();
    }

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private volatile boolean connected;
    private LobbyListener listener;

    public void setListener(LobbyListener listener) {
        this.listener = listener;
    }

    public void connect(String host, String playerName) throws IOException {
        this.playerName = playerName;
        socket = new Socket(host, Protocol.PORT);
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        connected = true;

        out.println(Protocol.JOIN + "|" + playerName);
        System.out.println("join -> " + playerName);

        new Thread(new Runnable() {
            @Override
            public void run() {
                readLoop();
            }
        }).start();
    }

    private void readLoop() {
        try {
            while (connected) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                System.out.println("пришло: " + line);
                if (line.startsWith(Protocol.LOBBY)) {
                    parseLobby(line);
                }
            }
        } catch (IOException e) {
            System.out.println("чтение сломалось");
        }

        connected = false;
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    private void parseLobby(String line) {
        String data = "";
        int p = line.indexOf("|");
        if (p >= 0 && p + 1 < line.length()) {
            data = line.substring(p + 1);
        }

        List<String> names = new ArrayList<>();
        if (data.length() > 0) {
            String[] arr = data.split(",");
            for (int i = 0; i < arr.length; i++) {
                String n = arr[i].trim();
                if (n.length() > 0) {
                    names.add(n);
                }
            }
        }

        System.out.println("в лобби человек: " + names.size());
        if (listener != null) {
            listener.onLobbyUpdate(names);
        }
    }

    public void sendLeave() {
        if (out != null && connected) {
            out.println(Protocol.LEAVE + "|" + playerName);
            System.out.println("leave");
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }
    }
}
