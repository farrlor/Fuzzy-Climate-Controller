package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SimulationEngineTest {

    private FuzzyInferenceEngine inferenceEngine;
    private SimulationConfiguration configuration;

    @BeforeEach
    void setUp() {
        inferenceEngine = FuzzyControllerFactory.createEngine();
        configuration = SimulationConfiguration.defaultConfiguration();
    }

    private SimulationEngine engine(double temperature, double humidity) {
        return new SimulationEngine(inferenceEngine, configuration, new EnvironmentState(temperature, humidity));
    }

    @Test
    @DisplayName("a stopped simulation performs no update")
    void stoppedDoesNothing() {
        SimulationEngine engine = engine(23.5, 85.0);
        assertEquals(SimulationState.STOPPED, engine.state());
        assertTrue(engine.step().isEmpty());
        assertEquals(85.0, engine.currentState().relativeHumidityPercent());
    }

    @Test
    @DisplayName("a running humid room loses moisture each step")
    void dehumidifiesWhenHumid() {
        SimulationEngine engine = engine(23.5, 85.0);
        engine.start();
        double before = engine.currentState().relativeHumidityPercent();
        engine.step();
        assertTrue(engine.currentState().relativeHumidityPercent() < before);
    }

    @Test
    @DisplayName("a running dry room gains moisture each step")
    void humidifiesWhenDry() {
        SimulationEngine engine = engine(23.5, 15.0);
        engine.start();
        double before = engine.currentState().relativeHumidityPercent();
        engine.step();
        assertTrue(engine.currentState().relativeHumidityPercent() > before);
    }

    @Test
    @DisplayName("a comfortable room stays within the deadband (no oscillation)")
    void deadbandSuppressesChange() {
        SimulationEngine engine = engine(23.5, 50.0);
        engine.start();
        engine.step();
        assertEquals(50.0, engine.currentState().relativeHumidityPercent(), 1.0e-9);
    }

    @Test
    @DisplayName("humidity never leaves the [0, 100] range")
    void humidityStaysBounded() {
        SimulationEngine engine = engine(20.0, 5.0);
        engine.start();
        for (int i = 0; i < 200; i++) {
            engine.step();
            double humidity = engine.currentState().relativeHumidityPercent();
            assertTrue(humidity >= 0.0 && humidity <= 100.0, "Out of range: " + humidity);
        }
    }

    @Test
    @DisplayName("the simulation converges towards the comfortable band and stabilises")
    void convergesAndStabilises() {
        SimulationEngine engine = engine(23.5, 90.0);
        engine.start();
        double last = engine.currentState().relativeHumidityPercent();
        for (int i = 0; i < 300; i++) {
            engine.step();
            last = engine.currentState().relativeHumidityPercent();
        }
        assertTrue(last >= 44.0 && last <= 58.0, "Did not converge to comfort band: " + last);

        // Once settled, further steps must not cause large swings.
        engine.step();
        double afterOneMore = engine.currentState().relativeHumidityPercent();
        assertTrue(Math.abs(afterOneMore - last) < 0.5, "Unstable near equilibrium");
    }

    @Test
    @DisplayName("start, pause, resume and reset transition the lifecycle correctly")
    void lifecycleTransitions() {
        SimulationEngine engine = engine(23.5, 85.0);

        engine.start();
        assertEquals(SimulationState.RUNNING, engine.state());

        engine.pause();
        assertEquals(SimulationState.PAUSED, engine.state());
        assertTrue(engine.step().isEmpty(), "A paused simulation must not advance");

        engine.resume();
        assertEquals(SimulationState.RUNNING, engine.state());

        engine.step();
        assertTrue(engine.stepCount() > 0);

        engine.reset();
        assertEquals(SimulationState.STOPPED, engine.state());
        assertEquals(0L, engine.stepCount());
        assertEquals(85.0, engine.currentState().relativeHumidityPercent(), 1.0e-9);
    }

    @Test
    @DisplayName("setHumidity clamps to the configured range")
    void setHumidityClamps() {
        SimulationEngine engine = engine(23.5, 50.0);
        engine.setHumidity(150.0);
        assertEquals(100.0, engine.currentState().relativeHumidityPercent(), 1.0e-9);
        engine.setHumidity(-20.0);
        assertEquals(0.0, engine.currentState().relativeHumidityPercent(), 1.0e-9);
    }

    @Test
    @DisplayName("tick listeners are notified on each running step")
    void listenersNotified() {
        SimulationEngine engine = engine(23.5, 85.0);
        List<SimulationTick> ticks = new ArrayList<>();
        engine.addTickListener(ticks::add);
        engine.start();
        engine.step();
        engine.step();
        assertEquals(2, ticks.size());
        assertEquals(1L, ticks.get(0).stepIndex());
        assertFalse(ticks.get(1).environment().relativeHumidityPercent() > 85.0);
    }
}
