package farit.dev.bs.network;

import farit.dev.bs.protocol.Protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {

    private ServerSocket serverSocket;
    private volatile boolean running;

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
                socket.close();
            } catch (IOException e) {
                if (running) {
                    System.out.println("ошибка accept: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.out.println("ошибка стоп: " + e.getMessage());
        }
        System.out.println("сервер выключен");
    }
}
