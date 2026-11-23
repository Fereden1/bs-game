package farit.dev.bs;

import farit.dev.bs.view.LobbyScreen;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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

        String ip = null;
        if (!host) {
            ip = askIp();
            if (ip == null) {
                return;
            }
            System.out.println("подключаемся к " + ip);
        }

        System.out.println("лобби host=" + host);

        new LobbyScreen(stage, host, name, ip, new Runnable() {
            @Override
            public void run() {
                showMenu();
            }
        }).show();
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

}
