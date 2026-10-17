package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ControlActionTest {

    @Test
    @DisplayName("a crisp signal resolves to the nearest action")
    void fromSignal() {
        assertEquals(ControlAction.STRONG_HUMIDIFICATION, ControlAction.fromSignal(0.95));
        assertEquals(ControlAction.HUMIDIFICATION, ControlAction.fromSignal(0.4));
        assertEquals(ControlAction.MAINTAIN, ControlAction.fromSignal(0.1));
        assertEquals(ControlAction.DEHUMIDIFICATION, ControlAction.fromSignal(-0.45));
        assertEquals(ControlAction.STRONG_DEHUMIDIFICATION, ControlAction.fromSignal(-1.0));
    }

    @Test
    @DisplayName("EnvironmentState rejects non-finite measurements")
    void environmentStateValidation() {
        assertThrows(IllegalArgumentException.class, () -> new EnvironmentState(Double.NaN, 50.0));
        assertThrows(IllegalArgumentException.class,
                () -> new EnvironmentState(23.0, Double.POSITIVE_INFINITY));
    }

    @Test
    @DisplayName("EnvironmentState copy helpers preserve the other field")
    void environmentStateCopies() {
        EnvironmentState state = new EnvironmentState(23.0, 50.0);
        assertEquals(60.0, state.withRelativeHumidity(60.0).relativeHumidityPercent());
        assertEquals(23.0, state.withRelativeHumidity(60.0).temperatureCelsius());
        assertEquals(25.0, state.withTemperature(25.0).temperatureCelsius());
    }
}
