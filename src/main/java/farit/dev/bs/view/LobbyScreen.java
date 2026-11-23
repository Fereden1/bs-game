package farit.dev.bs.view;

import farit.dev.bs.network.GameServer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LobbyScreen {

    private final Stage stage;
    private final boolean host;
    private final String playerName;
    private final String hostIp;
    private final GameServer server;
    private final Runnable onBack;

    public LobbyScreen(Stage stage, boolean host, String playerName, String hostIp,
                       GameServer server, Runnable onBack) {
        this.stage = stage;
        this.host = host;
        this.playerName = playerName;
        this.hostIp = hostIp;
        this.server = server;
        this.onBack = onBack;
    }

    public void show() {
        Label title;
        if (host) {
            title = new Label("лобби (хост)");
        } else {
            title = new Label("лобби");
        }

        Label info = new Label("для старта нужно минимум 3");

        ObservableList<String> players = FXCollections.observableArrayList();
        if (host) {
            players.add(playerName + " (хост)");
        } else {
            players.add(playerName);
        }

        ListView<String> list = new ListView<>(players);
        list.setPrefHeight(180);
        list.setMaxWidth(280);

        Label status;
        if (host) {
            status = new Label("сервер слушает");
        } else {
            status = new Label("ip: " + hostIp);
        }

        Button btnStart = new Button("Старт");
        btnStart.setDisable(!host);
        btnStart.setOnAction(e -> {
            if (players.size() < 3) {
                status.setText("нужно минимум 3 игрока");
            } else {
                status.setText("игра пока не готова");
            }
        });

        Button btnBack = new Button("Назад");
        btnBack.setOnAction(e -> {
            if (server != null) {
                server.stop();
            }
            onBack.run();
        });

        VBox root = new VBox(12, title, info, list, btnStart, btnBack, status);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 640, 480));
    }
}
