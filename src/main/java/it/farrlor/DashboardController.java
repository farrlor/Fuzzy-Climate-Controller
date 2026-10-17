package it.farrlor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * JavaFX controller for the dashboard. Forwards user commands to a
 * {@link SimulationEngine}, drives the clock with a {@link Timeline} so every
 * update runs on the Application Thread, and renders state and diagnostics. No
 * fuzzy mathematics live here.
 */
public final class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());
    private static final int MAX_CHART_POINTS = 60;

    // Environmental measurements
    @FXML private Label temperatureValueLabel;
    @FXML private Label humidityValueLabel;
    @FXML private Label temperatureClassLabel;
    @FXML private Label humidityClassLabel;

    // Controller status
    @FXML private Label actionLabel;
    @FXML private Label intensityLabel;
    @FXML private ProgressBar intensityBar;
    @FXML private Label stateLabel;
    @FXML private Label stepLabel;
    @FXML private Label targetRangeLabel;

    // Diagnostics
    @FXML private VBox temperatureMembershipBox;
    @FXML private VBox humidityMembershipBox;
    @FXML private ListView<String> activeRulesList;
    @FXML private Label crispOutputLabel;
    @FXML private LineChart<Number, Number> humidityChart;

    // Controls
    @FXML private TextField temperatureField;
    @FXML private TextField humidityField;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button resetButton;
    @FXML private Label validationLabel;

    private SimulationEngine simulationEngine;
    private Timeline clock;
    private XYChart.Series<Number, Number> humiditySeries;

    private final Map<TemperatureCategory, ProgressBar> temperatureBars = new EnumMap<>(TemperatureCategory.class);
    private final Map<HumidityCategory, ProgressBar> humidityBars = new EnumMap<>(HumidityCategory.class);

    /** Injects the simulation engine and performs the initial render. Call once after FXML load. */
    public void initialize(SimulationEngine engine) {
        this.simulationEngine = engine;

        buildMembershipRows();

        humiditySeries = new XYChart.Series<>();
        humiditySeries.setName("Relative humidity");
        humidityChart.getData().add(humiditySeries);
        humidityChart.setAnimated(false);
        humidityChart.setCreateSymbols(false);

        SimulationConfiguration config = engine.configuration();
        targetRangeLabel.setText("Target band: 45–55 %% RH · max %.1f %%/step · interval %ds".formatted(
                config.maximumHumidityChangePercentPerStep(), config.stepInterval().toSeconds()));

        EnvironmentState initial = engine.currentState();
        temperatureField.setText("%.1f".formatted(initial.temperatureCelsius()));
        humidityField.setText("%.1f".formatted(initial.relativeHumidityPercent()));

        clock = new Timeline(new KeyFrame(
                Duration.millis(config.stepInterval().toMillis()),
                event -> onTick()));
        clock.setCycleCount(Animation.INDEFINITE);

        render(engine.evaluate());
        updateButtons();
    }

    private void buildMembershipRows() {
        for (TemperatureCategory category : TemperatureCategory.values()) {
            temperatureMembershipBox.getChildren().add(createMembershipRow(category.label(), temperatureBars, category));
        }
        for (HumidityCategory category : HumidityCategory.values()) {
            humidityMembershipBox.getChildren().add(createMembershipRow(category.label(), humidityBars, category));
        }
    }

    private <C extends Enum<C>> HBox createMembershipRow(String label, Map<C, ProgressBar> registry, C category) {
        Label name = new Label(label);
        name.getStyleClass().add("membership-name");
        name.setMinWidth(110);

        ProgressBar bar = new ProgressBar(0);
        bar.getStyleClass().add("membership-bar");
        bar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);
        registry.put(category, bar);

        Label value = new Label("0.00");
        value.getStyleClass().add("membership-value");
        value.setMinWidth(44);
        bar.setUserData(value);

        HBox row = new HBox(10, name, bar, value);
        row.getStyleClass().add("membership-row");
        return row;
    }

    @FXML
    private void onStart() {
        simulationEngine.start();
        clock.play();
        clearValidation();
        updateButtons();
        LOGGER.fine("Simulation started");
    }

    @FXML
    private void onPause() {
        simulationEngine.pause();
        clock.pause();
        updateButtons();
        LOGGER.fine("Simulation paused");
    }

    @FXML
    private void onResume() {
        simulationEngine.resume();
        clock.play();
        updateButtons();
        LOGGER.fine("Simulation resumed");
    }

    @FXML
    private void onReset() {
        clock.stop();
        simulationEngine.reset();
        humiditySeries.getData().clear();
        EnvironmentState state = simulationEngine.currentState();
        temperatureField.setText("%.1f".formatted(state.temperatureCelsius()));
        humidityField.setText("%.1f".formatted(state.relativeHumidityPercent()));
        render(simulationEngine.evaluate());
        updateButtons();
        LOGGER.fine("Simulation reset");
    }

    @FXML
    private void onApplyInputs() {
        try {
            double temperature = ClimateInputParser.parseTemperature(temperatureField.getText());
            double humidity = ClimateInputParser.parseHumidity(humidityField.getText());
            simulationEngine.setTemperature(temperature);
            simulationEngine.setHumidity(humidity);
            clearValidation();
            render(simulationEngine.evaluate());
        } catch (InputValidationException ex) {
            showValidation(ex.getMessage());
        }
    }

    private void onTick() {
        simulationEngine.step().ifPresent(tick -> {
            render(tick.inference());
            appendHumidityPoint(tick);
        });
    }

    private void appendHumidityPoint(SimulationTick tick) {
        humiditySeries.getData().add(new XYChart.Data<>(tick.stepIndex(), tick.environment().relativeHumidityPercent()));
        if (humiditySeries.getData().size() > MAX_CHART_POINTS) {
            humiditySeries.getData().remove(0);
        }
    }

    private void render(FuzzyInferenceResult result) {
        EnvironmentState state = result.input();
        temperatureValueLabel.setText("%.1f °C".formatted(state.temperatureCelsius()));
        humidityValueLabel.setText("%.1f %%".formatted(state.relativeHumidityPercent()));
        temperatureClassLabel.setText(result.dominantTemperature().label());
        humidityClassLabel.setText(result.dominantHumidity().label());

        actionLabel.setText(result.resolvedAction().label());
        double signal = result.controlSignal();
        intensityLabel.setText("%+.3f".formatted(signal));
        intensityBar.setProgress(Math.abs(signal));
        intensityBar.getStyleClass().removeAll("intensity-humidify", "intensity-dehumidify", "intensity-neutral");
        intensityBar.getStyleClass().add(signal > 0.001 ? "intensity-humidify"
                : signal < -0.001 ? "intensity-dehumidify" : "intensity-neutral");
        crispOutputLabel.setText("Crisp control signal: %+.4f  →  %s".formatted(signal, result.resolvedAction().label()));

        stateLabel.setText(simulationEngine.state().label());
        stepLabel.setText(Long.toString(simulationEngine.stepCount()));

        updateBars(temperatureBars, result.temperatureMemberships());
        updateBars(humidityBars, result.humidityMemberships());
        updateActiveRules(result.activeRules());
    }

    private <C extends Enum<C>> void updateBars(Map<C, ProgressBar> bars, Map<C, Double> memberships) {
        bars.forEach((category, bar) -> {
            double value = memberships.getOrDefault(category, 0.0);
            bar.setProgress(value);
            if (bar.getUserData() instanceof Label valueLabel) {
                valueLabel.setText("%.2f".formatted(value));
            }
        });
    }

    private void updateActiveRules(List<RuleActivation> activations) {
        activeRulesList.getItems().setAll(activations.stream()
                .map(activation -> "%.2f  ·  %s".formatted(activation.strength(), activation.rule()))
                .toList());
        if (activations.isEmpty()) {
            activeRulesList.getItems().add("No rules firing (neutral region).");
        }
    }

    private void updateButtons() {
        SimulationState state = simulationEngine.state();
        startButton.setDisable(state == SimulationState.RUNNING || state == SimulationState.PAUSED);
        pauseButton.setDisable(state != SimulationState.RUNNING);
        resumeButton.setDisable(state != SimulationState.PAUSED);
        resetButton.setDisable(state == SimulationState.STOPPED && simulationEngine.stepCount() == 0);
    }

    private void showValidation(String message) {
        validationLabel.setText(message);
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
    }

    private void clearValidation() {
        validationLabel.setText("");
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);
    }

    /** Stops the clock so no background task outlives the window. */
    public void shutdown() {
        if (clock != null) {
            clock.stop();
        }
    }
}
