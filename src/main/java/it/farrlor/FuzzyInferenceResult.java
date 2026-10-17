package it.farrlor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Traceable result of one inference pass: the fuzzified inputs, their dominant
 * categories, every rule's firing strength, the aggregated strength per action,
 * the crisp control signal and the action nearest to it. Holds everything needed
 * to explain why the controller chose a given action.
 */
public record FuzzyInferenceResult(EnvironmentState input,
                                   Map<TemperatureCategory, Double> temperatureMemberships,
                                   Map<HumidityCategory, Double> humidityMemberships,
                                   TemperatureCategory dominantTemperature,
                                   HumidityCategory dominantHumidity,
                                   List<RuleActivation> ruleActivations,
                                   Map<ControlAction, Double> aggregatedActionStrengths,
                                   double controlSignal,
                                   ControlAction resolvedAction) {

    public FuzzyInferenceResult {
        Objects.requireNonNull(input, "input");
        temperatureMemberships = Map.copyOf(temperatureMemberships);
        humidityMemberships = Map.copyOf(humidityMemberships);
        Objects.requireNonNull(dominantTemperature, "dominantTemperature");
        Objects.requireNonNull(dominantHumidity, "dominantHumidity");
        ruleActivations = List.copyOf(ruleActivations);
        aggregatedActionStrengths = Map.copyOf(aggregatedActionStrengths);
        Objects.requireNonNull(resolvedAction, "resolvedAction");
        if (!Double.isFinite(controlSignal)) {
            throw new IllegalArgumentException("controlSignal must be finite but was " + controlSignal);
        }
    }

    /** Rules that fired with positive strength, strongest first. */
    public List<RuleActivation> activeRules() {
        return ruleActivations.stream()
                .filter(RuleActivation::isActive)
                .sorted(Comparator.comparingDouble(RuleActivation::strength).reversed())
                .toList();
    }

    public String explain() {
        return """
                Environment: %.1f °C, %.1f %% RH
                Classified as: %s temperature, %s humidity
                Decision: %s (control signal = %+.3f)""".formatted(
                input.temperatureCelsius(),
                input.relativeHumidityPercent(),
                dominantTemperature.label(),
                dominantHumidity.label(),
                resolvedAction.label(),
                controlSignal);
    }
}
