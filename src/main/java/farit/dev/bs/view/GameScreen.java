package farit.dev.bs.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.List;

public class GameScreen {

    private final Stage stage;
    private final String playerName;
    private final List<String> allPlayers;
    private final Runnable onBack;

    public GameScreen(Stage stage, String playerName, List<String> allPlayers, Runnable onBack) {
        this.stage = stage;
        this.playerName = playerName;
        this.allPlayers = allPlayers;
        this.onBack = onBack;
    }

    public void show() {
        Label nick = new Label(playerName);
        nick.setPadding(new Insets(10, 12, 10, 12));

        HBox slots = new HBox(8);
        slots.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 5; i++) {
            Button slot = new Button();
            slot.setPrefSize(48, 48);
            slot.setDisable(true);
            slots.getChildren().add(slot);
        }

        ComboBox<String> targetBox = new ComboBox<>();
        targetBox.setPromptText("цель");
        targetBox.setPrefWidth(140);
        for (int i = 0; i < allPlayers.size(); i++) {
            String n = cleanName(allPlayers.get(i));
            if (!n.equals(playerName)) {
                targetBox.getItems().add(n);
            }
        }
        if (!targetBox.getItems().isEmpty()) {
            targetBox.getSelectionModel().select(0);
        }
        Button btnUse = new Button("Предмет");

        Button btnBack = new Button("В меню");
        btnBack.setOnAction(e -> onBack.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottom = new HBox(12,
                new Label("инвентарь:"),
                slots,
                spacer,
                new Label("цель:"),
                targetBox,
                btnUse,
                btnBack
        );
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(10, 14, 10, 14));
        bottom.setStyle("-fx-background-color: #e8e8e8;");

        BorderPane root = new BorderPane();
        root.setTop(nick);
        root.setBottom(bottom);

        BorderPane.setAlignment(nick, Pos.TOP_LEFT);

        stage.setScene(new Scene(root, 720, 420));
    }

    private String cleanName(String n) {
        return n.replace(" (хост)", "").trim();
    }
}
