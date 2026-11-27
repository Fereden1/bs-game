package farit.dev.bs.view;

import farit.dev.bs.network.GameClient;
import farit.dev.bs.network.GameServer;
import farit.dev.bs.protocol.Protocol;
import javafx.application.Platform;
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

import java.util.ArrayList;
import java.util.List;

public class LobbyScreen {

    private final Stage stage;
    private final boolean host;
    private final String playerName;
    private final String hostIp;
    private final GameServer server;
    private final GameClient client;
    private final Runnable onBack;

    private ObservableList<String> players;
    private Label info;
    private Label status;

    public LobbyScreen(Stage stage, boolean host, String playerName, String hostIp,
                       GameServer server, GameClient client, Runnable onBack) {
        this.stage = stage;
        this.host = host;
        this.playerName = playerName;
        this.hostIp = hostIp;
        this.server = server;
        this.client = client;
        this.onBack = onBack;
    }

    public void show() {
        Label title;
        if (host) {
            title = new Label("лобби (хост)");
        } else {
            title = new Label("лобби");
        }

        info = new Label("для старта нужно минимум 3");

        players = FXCollections.observableArrayList();
        if (host) {
            players.add(playerName + " (хост)");
        } else {
            players.add(playerName);
        }

        ListView<String> list = new ListView<>(players);
        list.setPrefHeight(180);
        list.setMaxWidth(280);

        if (host) {
            status = new Label("сервер слушает");
        } else {
            status = new Label("сижу на " + hostIp);
        }

        Button btnStart = new Button("Старт");
        btnStart.setDisable(!host);
        btnStart.setOnAction(e -> {
            if (players.size() < 3) {
                status.setText("нужно минимум 3 игрока");
                return;
            }
            if (server != null) {
                server.broadcastStart();
            }
        });

        Button btnBack = new Button("Назад");
        btnBack.setOnAction(e -> goBack());

        VBox root = new VBox(12, title, info, list, btnStart, btnBack, status);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 640, 480));

        if (server != null) {
            server.setListener(new GameServer.LobbyListener() {
                @Override
                public void onLobbyUpdate(List<String> names) {
                    updatePlayers(names);
                }

                @Override
                public void onStart() {
                    openGame();
                }
            });
        }

        if (client != null) {
            client.setListener(new GameClient.LobbyListener() {
                @Override
                public void onLobbyUpdate(List<String> names) {
                    updatePlayers(names);
                }

                @Override
                public void onStart() {
                    openGame();
                }

                @Override
                public void onDisconnected() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("отключило(");
                        }
                    });
                }
            });
        }
    }

    private void updatePlayers(List<String> names) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                players.clear();
                for (int i = 0; i < names.size(); i++) {
                    players.add(names.get(i));
                }
                info.setText("сейчас " + names.size() + "/" + Protocol.MAX_PLAYERS);
            }
        });
    }

    private void openGame() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                System.out.println("открываем игру");
                List<String> names = new ArrayList<>(players);
                new GameScreen(stage, playerName, names, new Runnable() {
                    @Override
                    public void run() {
                        goBack();
                    }
                }).show();
            }
        });
    }

    private void goBack() {
        if (client != null) {
            client.sendLeave();
            client.disconnect();
        }
        if (server != null) {
            server.stop();
        }
        onBack.run();
    }
}
