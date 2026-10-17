package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FuzzyRuleBaseTest {

    private static final double DELTA = 1.0e-9;

    private final FuzzyRuleBase ruleBase = FuzzyRuleBase.defaultRuleBase();

    private Map<String, ControlAction> matrix() {
        return ruleBase.rules().stream().collect(Collectors.toMap(
                rule -> rule.temperature() + "|" + rule.humidity(),
                FuzzyRule::action));
    }

    @Test
    @DisplayName("the default rule base covers all 15 temperature/humidity combinations")
    void coversAllCombinations() {
        assertEquals(TemperatureCategory.values().length * HumidityCategory.values().length,
                ruleBase.rules().size());
        assertEquals(ruleBase.rules().size(), matrix().size(), "Combinations must be unique");
    }

    @ParameterizedTest(name = "{0} + {1} -> {2}")
    @CsvSource({
            "VERY_COLD, DRY, STRONG_HUMIDIFICATION",
            "VERY_COLD, COMFORTABLE, HUMIDIFICATION",
            "VERY_COLD, HUMID, MAINTAIN",
            "COLD, DRY, STRONG_HUMIDIFICATION",
            "COLD, COMFORTABLE, HUMIDIFICATION",
            "COLD, HUMID, MAINTAIN",
            "COMFORTABLE, DRY, HUMIDIFICATION",
            "COMFORTABLE, COMFORTABLE, MAINTAIN",
            "COMFORTABLE, HUMID, DEHUMIDIFICATION",
            "WARM, DRY, MAINTAIN",
            "WARM, COMFORTABLE, DEHUMIDIFICATION",
            "WARM, HUMID, STRONG_DEHUMIDIFICATION",
            "VERY_HOT, DRY, MAINTAIN",
            "VERY_HOT, COMFORTABLE, DEHUMIDIFICATION",
            "VERY_HOT, HUMID, STRONG_DEHUMIDIFICATION"
    })
    @DisplayName("the decision matrix matches the documented strategy")
    void decisionMatrix(TemperatureCategory temperature, HumidityCategory humidity, ControlAction expected) {
        assertEquals(expected, matrix().get(temperature + "|" + humidity));
    }

    @Test
    @DisplayName("an already-humid cold room is maintained, not humidified")
    void coldHumidIsNotHumidified() {
        assertEquals(ControlAction.MAINTAIN, matrix().get(TemperatureCategory.VERY_COLD + "|" + HumidityCategory.HUMID));
    }

    @Test
    @DisplayName("an already-dry hot room is maintained, not dehumidified")
    void hotDryIsNotDehumidified() {
        assertEquals(ControlAction.MAINTAIN, matrix().get(TemperatureCategory.VERY_HOT + "|" + HumidityCategory.DRY));
    }

    @Test
    @DisplayName("firing strength is the weighted minimum of the antecedents")
    void firingStrength() {
        FuzzyRule rule = new FuzzyRule(TemperatureCategory.COLD, HumidityCategory.DRY, ControlAction.HUMIDIFICATION, 0.8);
        assertEquals(0.8 * 0.3, rule.firingStrength(0.7, 0.3), DELTA);
        assertEquals(0.8 * 0.4, rule.firingStrength(0.4, 0.9), DELTA);
    }

    @Test
    @DisplayName("rule weights outside (0, 1] are rejected")
    void invalidWeight() {
        assertThrows(IllegalArgumentException.class,
                () -> new FuzzyRule(TemperatureCategory.COLD, HumidityCategory.DRY, ControlAction.MAINTAIN, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> new FuzzyRule(TemperatureCategory.COLD, HumidityCategory.DRY, ControlAction.MAINTAIN, 1.5));
    }

    @Test
    @DisplayName("an empty rule base is rejected")
    void emptyRuleBase() {
        assertThrows(IllegalArgumentException.class, () -> new FuzzyRuleBase(List.of()));
    }

    @Test
    @DisplayName("the matrix is monotonic: drier and colder never means less humidification")
    void monotonic() {
        // For a fixed humidity, colder temperatures must not demand less humidification.
        for (HumidityCategory humidity : HumidityCategory.values()) {
            double previous = Double.NEGATIVE_INFINITY;
            // Iterate from hottest to coldest; the signal should be non-decreasing.
            TemperatureCategory[] hotToCold = {
                    TemperatureCategory.VERY_HOT, TemperatureCategory.WARM, TemperatureCategory.COMFORTABLE,
                    TemperatureCategory.COLD, TemperatureCategory.VERY_COLD};
            for (TemperatureCategory temperature : hotToCold) {
                double signal = matrix().get(temperature + "|" + humidity).representativeSignal();
                assertTrue(signal >= previous,
                        "Non-monotonic at " + temperature + "/" + humidity);
                previous = signal;
            }
        }
    }
}
