package it.farrlor;

import java.util.Objects;

/**
 * A declarative rule: {@code IF temperature IS t AND humidity IS h THEN control
 * IS action}, with an optional weight in {@code (0, 1]}. Representing rules as
 * data keeps the rule base inspectable and testable independently of the engine.
 */
public record FuzzyRule(TemperatureCategory temperature,
                        HumidityCategory humidity,
                        ControlAction action,
                        double weight) {

    public FuzzyRule {
        Objects.requireNonNull(temperature, "temperature");
        Objects.requireNonNull(humidity, "humidity");
        Objects.requireNonNull(action, "action");
        if (!(weight > 0.0 && weight <= 1.0)) {
            throw new IllegalArgumentException("Rule weight must be within (0, 1] but was " + weight);
        }
    }

    public FuzzyRule(TemperatureCategory temperature, HumidityCategory humidity, ControlAction action) {
        this(temperature, humidity, action, 1.0);
    }

    /** Weighted firing strength, using the minimum t-norm for the fuzzy AND. */
    public double firingStrength(double temperatureMembership, double humidityMembership) {
        return weight * Math.min(temperatureMembership, humidityMembership);
    }

    @Override
    public String toString() {
        return "IF temperature IS %s AND humidity IS %s THEN control IS %s%s".formatted(
                temperature.label(),
                humidity.label(),
                action.label(),
                weight == 1.0 ? "" : " [w=%.2f]".formatted(weight));
    }
}
