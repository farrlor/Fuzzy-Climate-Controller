/**
 * Module descriptor for the Fuzzy Climate Controller.
 *
 * <p>The package {@code it.farrlor} is opened to {@code javafx.fxml} so that the
 * FXML loader can reflectively inject controls and bind event handlers.
 */
module it.farrlor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens it.farrlor to javafx.fxml;
    exports it.farrlor;
}
