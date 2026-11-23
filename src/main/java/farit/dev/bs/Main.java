package farit.dev.bs;

import farit.dev.bs.network.GameClient;
import farit.dev.bs.network.GameServer;
import farit.dev.bs.view.LobbyScreen;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Buck Shot Game");
        showMenu();
        stage.show();
    }

    private void showMenu() {
        Label title = new Label("Buck Shot Game");

        Button createButton = new Button("Создать лобби");
        Button joinButton = new Button("Присоединиться");
        Button exitButton = new Button("Выход");

        createButton.setOnAction(e -> openLobby(true));
        joinButton.setOnAction(e -> openLobby(false));
        exitButton.setOnAction(e -> stage.close());

        VBox root = new VBox(15, title, createButton, joinButton, exitButton);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 640, 480));
    }

    private void openLobby(boolean host) {
        String name = askName();
        if (name == null) {
            return;
        }

        String ip = "127.0.0.1";
        if (!host) {
            ip = askIp();
            if (ip == null) {
                return;
            }
            System.out.println("подключаемся к " + ip);
        }

        System.out.println("лобби host=" + host);

        GameServer server = null;
        GameClient client = null;
        try {
            if (host) {
                server = new GameServer(name);
                server.start();
            } else {
                client = new GameClient();
            }

            new LobbyScreen(stage, host, name, ip, server, client, new Runnable() {
                @Override
                public void run() {
                    showMenu();
                }
            }).show();

            if (client != null) {
                client.connect(ip, name);
            }
        } catch (IOException e) {
            System.out.println("ошибка сети: " + e.getMessage());
            if (client != null) {
                client.disconnect();
            }
            if (server != null) {
                server.stop();
            }
            showMenu();
            showError(toRussianError(e.getMessage()));
        }
    }

    private String toRussianError(String message) {
        if (message != null && message.contains("Address already in use")) {
            return "Порт 5555 занят";
        }
        if (message != null && message.contains("Connection refused")) {
            return "Нет лобби. Сначала создайте лобби у хоста.";
        }
        return "Ошибка сети";
    }

    private String askName() {
        TextInputDialog dialog = new TextInputDialog("Игрок");
        dialog.setTitle("Имя");
        dialog.setHeaderText(null);
        dialog.setContentText("Введите имя:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        String name = result.get().trim();
        if (name.isEmpty()) {
            return null;
        }
        return name;
    }

    private String askIp() {
        TextInputDialog dialog = new TextInputDialog("127.0.0.1");
        dialog.setTitle("IP");
        dialog.setHeaderText(null);
        dialog.setContentText("IP хоста:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }

        String ip = result.get().trim();
        if (ip.isEmpty()) {
            return null;
        }
        return ip;
    }

    private void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

}
