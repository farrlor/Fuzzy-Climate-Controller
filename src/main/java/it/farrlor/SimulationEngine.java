package it.farrlor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Advances the environment in discrete steps using a {@link FuzzyInferenceEngine}.
 * Contains no timer: a caller (the JavaFX {@code Timeline}) invokes {@link #step()}
 * once per {@link SimulationConfiguration#stepInterval()}, which keeps the model
 * deterministic and testable without a clock or UI.
 *
 * <p>Each running step applies {@code Δhumidity = maxChangePerStep × controlSignal}
 * ({@code controlSignal ∈ [-1, +1]}; positive humidifies, negative dehumidifies),
 * suppresses changes below the deadband to avoid oscillation, and clamps humidity
 * to the configured range.
 *
 * <p>Not thread-safe; used from a single thread (the JavaFX Application Thread).
 * This is a simplified teaching model, not a physically calibrated controller.
 */
public final class SimulationEngine {

    private final FuzzyInferenceEngine inferenceEngine;
    private final SimulationConfiguration configuration;
    private final List<Consumer<SimulationTick>> tickListeners = new ArrayList<>();

    private EnvironmentState initialState;
    private EnvironmentState currentState;
    private SimulationState state = SimulationState.STOPPED;
    private long stepCount = 0L;

    public SimulationEngine(FuzzyInferenceEngine inferenceEngine,
                            SimulationConfiguration configuration,
                            EnvironmentState initialState) {
        this.inferenceEngine = Objects.requireNonNull(inferenceEngine, "inferenceEngine");
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.initialState = Objects.requireNonNull(initialState, "initialState");
        this.currentState = clampHumidity(initialState);
    }

    /** Registers a listener notified after every running {@link #step()}. */
    public void addTickListener(Consumer<SimulationTick> listener) {
        tickListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    public void start() {
        state = SimulationState.RUNNING;
    }

    public void pause() {
        if (state == SimulationState.RUNNING) {
            state = SimulationState.PAUSED;
        }
    }

    public void resume() {
        if (state == SimulationState.PAUSED) {
            state = SimulationState.RUNNING;
        }
    }

    public void stop() {
        state = SimulationState.STOPPED;
    }

    /** Restores the initial environment, STOPPED state and a zero step count. */
    public void reset() {
        this.currentState = clampHumidity(initialState);
        this.state = SimulationState.STOPPED;
        this.stepCount = 0L;
    }

    /** Advances one step when running, updating humidity and notifying listeners. */
    public Optional<SimulationTick> step() {
        if (state != SimulationState.RUNNING) {
            return Optional.empty();
        }
        FuzzyInferenceResult inference = inferenceEngine.infer(currentState);
        double signal = inference.controlSignal();

        double change = Math.abs(signal) < configuration.controlSignalDeadband()
                ? 0.0
                : configuration.maximumHumidityChangePercentPerStep() * signal;

        double updatedHumidity = Math.clamp(
                currentState.relativeHumidityPercent() + change,
                configuration.minimumHumidityPercent(),
                configuration.maximumHumidityPercent());
        // The clamp may reduce the effective change at the domain boundary.
        double appliedChange = updatedHumidity - currentState.relativeHumidityPercent();

        currentState = currentState.withRelativeHumidity(updatedHumidity);
        stepCount++;

        SimulationTick tick = new SimulationTick(stepCount, currentState, inference, appliedChange);
        tickListeners.forEach(listener -> listener.accept(tick));
        return Optional.of(tick);
    }

    /** Runs inference on the current environment without changing state. */
    public FuzzyInferenceResult evaluate() {
        return inferenceEngine.infer(currentState);
    }

    public void setTemperature(double temperatureCelsius) {
        currentState = currentState.withTemperature(temperatureCelsius);
    }

    /** Sets the humidity (clamped) and makes it the new {@link #reset()} baseline. */
    public void setHumidity(double relativeHumidityPercent) {
        currentState = clampHumidity(currentState.withRelativeHumidity(relativeHumidityPercent));
        initialState = currentState;
    }

    public EnvironmentState currentState() {
        return currentState;
    }

    public SimulationState state() {
        return state;
    }

    public long stepCount() {
        return stepCount;
    }

    public SimulationConfiguration configuration() {
        return configuration;
    }

    private EnvironmentState clampHumidity(EnvironmentState candidate) {
        double clamped = Math.clamp(
                candidate.relativeHumidityPercent(),
                configuration.minimumHumidityPercent(),
                configuration.maximumHumidityPercent());
        return candidate.withRelativeHumidity(clamped);
    }
}
