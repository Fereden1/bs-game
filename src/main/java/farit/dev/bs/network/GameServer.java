package farit.dev.bs.network;

import farit.dev.bs.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    public interface LobbyListener {
        void onLobbyUpdate(List<String> names);

        void onStart();
    }

    private final String hostName;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private volatile boolean running;
    private LobbyListener listener;

    public GameServer(String hostName) {
        this.hostName = hostName;
    }

    public void setListener(LobbyListener listener) {
        this.listener = listener;
        sendLobbyToUi();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(Protocol.PORT);
        running = true;
        System.out.println("сервер на порту " + Protocol.PORT);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                acceptLoop();
            }
        });
        t.setName("server-accept");
        t.start();
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("подключился: " + socket.getInetAddress());

                synchronized (clients) {
                    if (clients.size() + 1 >= Protocol.MAX_PLAYERS) {
                        System.out.println("лобби полное");
                        socket.close();
                        continue;
                    }
                }

                ClientHandler handler = new ClientHandler(socket);
                Thread ht = new Thread(handler);
                ht.setName("client-handler");
                ht.start();
            } catch (IOException e) {
                if (running) {
                    System.out.println("ошибка accept: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).closeSocket();
            }
            clients.clear();
        }
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("ошибка стоп: " + e.getMessage());
        }
        System.out.println("сервер выключен");
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String name;

        private ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                );

                synchronized (clients) {
                    clients.add(this);
                }

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("получил: " + line);
                    handleLine(line);
                }
            } catch (IOException e) {
                System.out.println("клиент отвалился");
            }

            synchronized (clients) {
                clients.remove(this);
            }
            closeSocket();
            broadcastLobby();
        }

        private void handleLine(String line) {
            String[] parts = line.split("\\|", 2);
            String type = parts[0];
            String data = "";
            if (parts.length > 1) {
                data = parts[1].trim();
            }

            if (Protocol.JOIN.equals(type)) {
                name = data;
                System.out.println("JOIN от " + name + " (хост " + hostName + ")");
                broadcastLobby();
            } else if (Protocol.LEAVE.equals(type)) {
                closeSocket();
            }
        }

        private void send(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        private void closeSocket() {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("сокет уже закрыт");
            }
        }
    }

    private List<String> buildNames() {
        List<String> names = new ArrayList<>();
        names.add(hostName + " (хост)");
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                ClientHandler c = clients.get(i);
                if (c.name != null) {
                    names.add(c.name);
                }
            }
        }
        return names;
    }

    private void broadcastLobby() {
        List<String> names = buildNames();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(names.get(i));
        }

        String msg = Protocol.LOBBY + "|" + sb;
        System.out.println("рассылка " + msg);

        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).send(msg);
            }
        }

        sendLobbyToUi(names);
    }

    private void sendLobbyToUi() {
        sendLobbyToUi(buildNames());
    }

    private void sendLobbyToUi(List<String> names) {
        if (listener != null) {
            listener.onLobbyUpdate(names);
        }
    }

    public void broadcastStart() {
        String msg = Protocol.START;
        System.out.println("старт игры");
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                clients.get(i).send(msg);
            }
        }
        if (listener != null) {
            listener.onStart();
        }
    }
}
