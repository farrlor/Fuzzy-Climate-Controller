package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FuzzyVariableTest {

    private static final double DELTA = 1.0e-9;

    @Test
    @DisplayName("fuzzify returns a membership for every category")
    void fuzzifyAllCategories() {
        FuzzyVariable<TemperatureCategory> temperature = FuzzyControllerFactory.createTemperatureVariable();
        Map<TemperatureCategory, Double> memberships = temperature.fuzzify(23.5);
        assertEquals(TemperatureCategory.values().length, memberships.size());
        assertEquals(1.0, memberships.get(TemperatureCategory.COMFORTABLE), DELTA);
    }

    @Test
    @DisplayName("inputs beyond the domain are clamped to the boundary sets")
    void clampsOutOfDomainInputs() {
        FuzzyVariable<TemperatureCategory> temperature = FuzzyControllerFactory.createTemperatureVariable();
        assertEquals(1.0, temperature.fuzzify(-40.0).get(TemperatureCategory.VERY_COLD), DELTA);
        assertEquals(1.0, temperature.fuzzify(500.0).get(TemperatureCategory.VERY_HOT), DELTA);
    }

    @Test
    @DisplayName("every point of the temperature domain belongs to at least one category")
    void temperatureDomainIsFullyCovered() {
        FuzzyVariable<TemperatureCategory> temperature = FuzzyControllerFactory.createTemperatureVariable();
        for (double t = FuzzyControllerFactory.TEMPERATURE_MIN_CELSIUS;
             t <= FuzzyControllerFactory.TEMPERATURE_MAX_CELSIUS; t += 0.05) {
            double total = temperature.fuzzify(t).values().stream().mapToDouble(Double::doubleValue).sum();
            assertTrue(total > 0.0, "No coverage at temperature " + t);
        }
    }

    @Test
    @DisplayName("every point of the humidity domain belongs to at least one category")
    void humidityDomainIsFullyCovered() {
        FuzzyVariable<HumidityCategory> humidity = FuzzyControllerFactory.createHumidityVariable();
        for (double h = FuzzyControllerFactory.HUMIDITY_MIN_PERCENT;
             h <= FuzzyControllerFactory.HUMIDITY_MAX_PERCENT; h += 0.5) {
            double total = humidity.fuzzify(h).values().stream().mapToDouble(Double::doubleValue).sum();
            assertTrue(total > 0.0, "No coverage at humidity " + h);
        }
    }

    @Test
    @DisplayName("non-finite inputs and empty configurations are rejected")
    void rejectsInvalidUse() {
        FuzzyVariable<HumidityCategory> humidity = FuzzyControllerFactory.createHumidityVariable();
        assertThrows(IllegalArgumentException.class, () -> humidity.fuzzify(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new FuzzyVariable<>("empty", 0.0, 1.0, Map.<HumidityCategory, MembershipFunction>of()));
    }
}
