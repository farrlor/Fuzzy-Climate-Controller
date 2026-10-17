package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FuzzyInferenceEngineTest {

    private final FuzzyInferenceEngine engine = FuzzyControllerFactory.createEngine();

    private FuzzyInferenceResult infer(double temperature, double humidity) {
        return engine.infer(new EnvironmentState(temperature, humidity));
    }

    @Test
    @DisplayName("a dry room at a comfortable temperature is humidified")
    void dryRoomIsHumidified() {
        FuzzyInferenceResult result = infer(23.5, 20.0);
        assertTrue(result.controlSignal() > 0.0,
                "Expected a positive (humidifying) signal but was " + result.controlSignal());
        assertEquals(ControlAction.HUMIDIFICATION, result.resolvedAction());
    }

    @Test
    @DisplayName("a humid room at a comfortable temperature is dehumidified")
    void humidRoomIsDehumidified() {
        FuzzyInferenceResult result = infer(23.5, 85.0);
        assertTrue(result.controlSignal() < 0.0,
                "Expected a negative (dehumidifying) signal but was " + result.controlSignal());
        assertEquals(ControlAction.DEHUMIDIFICATION, result.resolvedAction());
    }

    @Test
    @DisplayName("a comfortable room is left essentially unchanged")
    void comfortableRoomIsMaintained() {
        FuzzyInferenceResult result = infer(23.5, 50.0);
        assertEquals(0.0, result.controlSignal(), 1.0e-6);
        assertEquals(ControlAction.MAINTAIN, result.resolvedAction());
    }

    @Test
    @DisplayName("a very hot, very humid room receives strong dehumidification")
    void hotHumidGetsStrongDehumidification() {
        FuzzyInferenceResult result = infer(27.0, 90.0);
        assertTrue(result.controlSignal() < -0.6,
                "Expected a strongly negative signal but was " + result.controlSignal());
        assertEquals(ControlAction.STRONG_DEHUMIDIFICATION, result.resolvedAction());
    }

    @Test
    @DisplayName("a very cold, very dry room receives strong humidification")
    void coldDryGetsStrongHumidification() {
        FuzzyInferenceResult result = infer(20.0, 10.0);
        assertTrue(result.controlSignal() > 0.6,
                "Expected a strongly positive signal but was " + result.controlSignal());
        assertEquals(ControlAction.STRONG_HUMIDIFICATION, result.resolvedAction());
    }

    @Test
    @DisplayName("an already-dry but hot room is not dehumidified (original flaw fixed)")
    void hotDryRoomIsNotDehumidified() {
        FuzzyInferenceResult result = infer(27.0, 15.0);
        assertTrue(result.controlSignal() >= -1.0e-6,
                "A dry hot room must not be dehumidified but signal was " + result.controlSignal());
    }

    @Test
    @DisplayName("an already-humid but cold room is not humidified (original flaw fixed)")
    void coldHumidRoomIsNotHumidified() {
        FuzzyInferenceResult result = infer(20.0, 90.0);
        assertTrue(result.controlSignal() <= 1.0e-6,
                "A humid cold room must not be humidified but signal was " + result.controlSignal());
    }

    @ParameterizedTest
    @CsvSource({
            "20.0, 0.0", "20.0, 100.0", "23.5, 50.0", "27.0, 0.0", "27.0, 100.0",
            "21.0, 33.0", "24.3, 62.0", "25.9, 48.0", "22.2, 77.0"
    })
    @DisplayName("the control signal is always finite and within [-1, +1]")
    void controlSignalWithinBounds(double temperature, double humidity) {
        double signal = infer(temperature, humidity).controlSignal();
        assertTrue(Double.isFinite(signal), "signal must be finite");
        assertTrue(signal >= -1.0 && signal <= 1.0, "signal out of range: " + signal);
    }

    @Test
    @DisplayName("inference is deterministic")
    void deterministic() {
        assertEquals(infer(24.1, 58.0).controlSignal(), infer(24.1, 58.0).controlSignal());
    }

    @Test
    @DisplayName("increasing humidity monotonically decreases the control signal at a fixed temperature")
    void monotonicInHumidity() {
        double previous = Double.POSITIVE_INFINITY;
        for (double h = 0.0; h <= 100.0; h += 5.0) {
            double signal = infer(23.5, h).controlSignal();
            assertTrue(signal <= previous + 1.0e-6,
                    "Signal increased with humidity at h=" + h + " (" + signal + " > " + previous + ")");
            previous = signal;
        }
    }

    @Test
    @DisplayName("the result exposes diagnostics for explainability")
    void exposesDiagnostics() {
        FuzzyInferenceResult result = infer(23.5, 20.0);
        assertEquals(TemperatureCategory.COMFORTABLE, result.dominantTemperature());
        assertEquals(HumidityCategory.DRY, result.dominantHumidity());
        assertTrue(result.activeRules().stream().anyMatch(RuleActivation::isActive));
        assertEquals(TemperatureCategory.values().length, result.temperatureMemberships().size());
        assertTrue(result.explain().contains("Humidification"));
    }
}
