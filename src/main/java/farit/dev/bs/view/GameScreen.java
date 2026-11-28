package farit.dev.bs.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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

        Canvas canvas = new Canvas(1040, 640);
        drawTable(canvas);

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
        root.setCenter(canvas);
        root.setBottom(bottom);
        BorderPane.setAlignment(nick, Pos.TOP_LEFT);
        BorderPane.setAlignment(canvas, Pos.CENTER);

        stage.setScene(new Scene(root, 1080, 800));
    }

    private void drawTable(Canvas canvas) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        double size = Math.min(w, h) - 16;
        double ox = (w - size) / 2;
        double oy = (h - size) / 2;
        double midX = w / 2;
        double midY = h / 2;
        double radius = size / 2;

        g.setFill(Color.web("#1e3d2f"));
        g.fillOval(ox, oy, size, size);

        g.setStroke(Color.WHITE);
        g.setLineWidth(3);
        g.strokeOval(ox, oy, size, size);
        g.setStroke(Color.BLACK);
        g.setLineWidth(4);
        g.strokeOval(ox - 3, oy - 3, size + 6, size + 6);

        double inner = size * 0.28;
        g.setStroke(Color.WHITE);
        g.setLineWidth(2);
        g.strokeOval(midX - inner / 2, midY - inner / 2, inner, inner);

        try {
            Image rev = new Image(getClass().getResourceAsStream("/images/revolver.png"));
            g.drawImage(rev, midX - 100, midY - 55, 200, 110);
        } catch (Exception e) {
            System.out.println("нет револьвера");
        }

        int n = allPlayers.size();
        double seatR = radius * 0.70;
        for (int i = 0; i < n; i++) {
            String name = cleanName(allPlayers.get(i));
            double ang = -Math.PI / 2 + i * (2 * Math.PI / Math.max(n, 1));
            double cx = midX + Math.cos(ang) * seatR;
            double cy = midY + Math.sin(ang) * seatR;

            g.setFill(Color.rgb(0, 0, 0, 0.45));
            g.fillRoundRect(cx - 70, cy - 34, 140, 62, 10, 10);

            g.setFill(Color.WHITE);
            g.setFont(Font.font(15));
            g.fillText(name, cx - 55, cy - 6);

            for (int hp = 0; hp < 3; hp++) {
                g.setFill(Color.CRIMSON);
                g.fillRoundRect(cx - 55 + hp * 32, cy + 12, 26, 12, 3, 3);
            }
        }
    }

    private String cleanName(String n) {
        return n.replace(" (хост)", "").trim();
    }
}
