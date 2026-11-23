package farit.dev.bs.network;

import farit.dev.bs.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class GameClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private volatile boolean connected;

    public void connect(String host, String playerName) throws IOException {
        this.playerName = playerName;
        socket = new Socket(host, Protocol.PORT);
        out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        connected = true;

        out.println(Protocol.JOIN + "|" + playerName);
        System.out.println("отправил JOIN " + playerName);

        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                readLoop();
            }
        });
        reader.setName("client-reader");
        reader.start();
    }

    private void readLoop() {
        try {
            while (connected) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                System.out.println("клиент получил: " + line);
            }
        } catch (IOException e) {
            if (connected) {
                System.out.println("ошибка чтения: " + e.getMessage());
            }
        }
        connected = false;
    }

    public void sendLeave() {
        if (out != null && connected) {
            out.println(Protocol.LEAVE + "|" + playerName);
            System.out.println("отправил LEAVE");
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("ошибка закрытия клиента");
        }
    }
}
