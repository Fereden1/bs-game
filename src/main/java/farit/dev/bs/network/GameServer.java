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

    private final String hostName;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private volatile boolean running;

    public GameServer(String hostName) {
        this.hostName = hostName;
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
            System.out.println("игроков сейчас: " + (1 + namedCount()));
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
            } else if (Protocol.LEAVE.equals(type)) {
                closeSocket();
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

    private int namedCount() {
        int n = 0;
        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).name != null) {
                    n++;
                }
            }
        }
        return n;
    }
}
