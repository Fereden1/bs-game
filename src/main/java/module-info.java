module farit.dev.bs {
    requires javafx.controls;
    requires javafx.fxml;


    opens farit.dev.bs to javafx.fxml;
    exports farit.dev.bs;
    exports farit.dev.bs.view;
    opens farit.dev.bs.view to javafx.fxml;
}