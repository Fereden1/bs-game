package farit.dev.bs.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameScreen {

    private final Stage stage;
    private final String playerName;
    private final Runnable onBack;

    public GameScreen(Stage stage, String playerName, Runnable onBack) {
        this.stage = stage;
        this.playerName = playerName;
        this.onBack = onBack;
    }

    public void show() {
        Label info = new Label(playerName);

        Button btnBack = new Button("В меню");
        btnBack.setOnAction(e -> onBack.run());

        VBox root = new VBox(15, info, btnBack);
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 640, 480));
    }
}
