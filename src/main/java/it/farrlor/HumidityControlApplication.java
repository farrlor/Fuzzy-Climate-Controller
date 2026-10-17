package it.farrlor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/** JavaFX entry point: wires the inference engine, simulation and dashboard, then shows the window. */
public final class HumidityControlApplication extends Application {

    private static final EnvironmentState DEFAULT_ENVIRONMENT = new EnvironmentState(23.5, 70.0);

    private DashboardController controller;

    @Override
    public void start(Stage stage) {
        FuzzyInferenceEngine inferenceEngine = FuzzyControllerFactory.createEngine();
        SimulationEngine simulationEngine = new SimulationEngine(
                inferenceEngine, SimulationConfiguration.defaultConfiguration(), DEFAULT_ENVIRONMENT);

        FXMLLoader loader = new FXMLLoader(resource("dashboard.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to load the dashboard layout", ex);
        }
        controller = loader.getController();
        controller.initialize(simulationEngine);

        Scene scene = new Scene(root, 1040, 720);
        scene.getStylesheets().add(resource("dashboard.css").toExternalForm());

        stage.setTitle("Fuzzy Climate Controller");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(640);
        tryLoadIcon(stage);
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }

    private void tryLoadIcon(Stage stage) {
        URL iconUrl = HumidityControlApplication.class.getResource("/app_icon.png");
        if (iconUrl != null) {
            stage.getIcons().add(new Image(iconUrl.toExternalForm()));
        }
    }

    private static URL resource(String name) {
        return Objects.requireNonNull(
                HumidityControlApplication.class.getResource(name),
                () -> "Missing classpath resource: " + name);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
