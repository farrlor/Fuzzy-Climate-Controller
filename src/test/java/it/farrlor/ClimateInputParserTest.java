package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClimateInputParserTest {

    private static final double DELTA = 1.0e-9;

    @Test
    @DisplayName("valid temperatures are parsed, accepting comma decimals")
    void parsesTemperature() {
        assertEquals(23.5, ClimateInputParser.parseTemperature("23.5"), DELTA);
        assertEquals(23.5, ClimateInputParser.parseTemperature(" 23,5 "), DELTA);
    }

    @Test
    @DisplayName("valid humidity values are parsed")
    void parsesHumidity() {
        assertEquals(0.0, ClimateInputParser.parseHumidity("0"), DELTA);
        assertEquals(100.0, ClimateInputParser.parseHumidity("100"), DELTA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "abc", "19.9", "27.1", "NaN"})
    @DisplayName("invalid temperatures are rejected")
    void rejectsInvalidTemperature(String raw) {
        assertThrows(InputValidationException.class, () -> ClimateInputParser.parseTemperature(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "-0.1", "100.1", "oops", "Infinity"})
    @DisplayName("invalid humidity values are rejected")
    void rejectsInvalidHumidity(String raw) {
        assertThrows(InputValidationException.class, () -> ClimateInputParser.parseHumidity(raw));
    }

    @Test
    @DisplayName("null input is rejected")
    void rejectsNull() {
        assertThrows(InputValidationException.class, () -> ClimateInputParser.parseTemperature(null));
    }
}
