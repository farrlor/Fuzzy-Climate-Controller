package it.farrlor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/**
 * Mamdani fuzzy inference engine. Each pass fuzzifies the inputs, evaluates the
 * rules (firing strength = weight × min of the antecedent memberships), clips each
 * action's output set at its aggregated strength (min implication) and combines
 * them with max, then defuzzifies to a crisp control signal in {@code [-1, +1]}.
 *
 * <p>The engine is immutable and free of any UI dependency. With no rule firing,
 * the centroid defuzzifier yields the neutral signal and the resolved action is
 * {@link ControlAction#MAINTAIN}.
 */
public final class FuzzyInferenceEngine {

    private final FuzzyVariable<TemperatureCategory> temperatureVariable;
    private final FuzzyVariable<HumidityCategory> humidityVariable;
    private final FuzzyVariable<ControlAction> outputVariable;
    private final FuzzyRuleBase ruleBase;
    private final Defuzzifier defuzzifier;

    public FuzzyInferenceEngine(FuzzyVariable<TemperatureCategory> temperatureVariable,
                                FuzzyVariable<HumidityCategory> humidityVariable,
                                FuzzyVariable<ControlAction> outputVariable,
                                FuzzyRuleBase ruleBase,
                                Defuzzifier defuzzifier) {
        this.temperatureVariable = Objects.requireNonNull(temperatureVariable, "temperatureVariable");
        this.humidityVariable = Objects.requireNonNull(humidityVariable, "humidityVariable");
        this.outputVariable = Objects.requireNonNull(outputVariable, "outputVariable");
        this.ruleBase = Objects.requireNonNull(ruleBase, "ruleBase");
        this.defuzzifier = Objects.requireNonNull(defuzzifier, "defuzzifier");
    }

    public FuzzyInferenceResult infer(EnvironmentState environment) {
        Objects.requireNonNull(environment, "environment");

        Map<TemperatureCategory, Double> temperatureMemberships =
                temperatureVariable.fuzzify(environment.temperatureCelsius());
        Map<HumidityCategory, Double> humidityMemberships =
                humidityVariable.fuzzify(environment.relativeHumidityPercent());

        List<RuleActivation> activations = new ArrayList<>(ruleBase.rules().size());
        EnumMap<ControlAction, Double> actionStrengths = new EnumMap<>(ControlAction.class);
        for (FuzzyRule rule : ruleBase.rules()) {
            double strength = rule.firingStrength(
                    temperatureMemberships.get(rule.temperature()),
                    humidityMemberships.get(rule.humidity()));
            activations.add(new RuleActivation(rule, strength));
            actionStrengths.merge(rule.action(), strength, Math::max);
        }

        double controlSignal = defuzzifier.defuzzify(
                aggregatedMembership(actionStrengths), outputVariable.minimum(), outputVariable.maximum());

        return new FuzzyInferenceResult(
                environment,
                temperatureMemberships,
                humidityMemberships,
                dominantCategory(temperatureMemberships),
                dominantCategory(humidityMemberships),
                activations,
                actionStrengths,
                controlSignal,
                ControlAction.fromSignal(controlSignal));
    }

    /** Aggregated output μ(x) = max over actions of min(strength, μ_action(x)). */
    private DoubleUnaryOperator aggregatedMembership(Map<ControlAction, Double> actionStrengths) {
        return x -> {
            double aggregate = 0.0;
            for (Map.Entry<ControlAction, Double> entry : actionStrengths.entrySet()) {
                double strength = entry.getValue();
                if (strength > 0.0) {
                    double clipped = Math.min(strength, outputVariable.membershipFunction(entry.getKey()).membership(x));
                    aggregate = Math.max(aggregate, clipped);
                }
            }
            return aggregate;
        };
    }

    /** Category with the highest membership; ties favour the first in enum order. */
    private static <C extends Enum<C>> C dominantCategory(Map<C, Double> memberships) {
        C dominant = null;
        double best = Double.NEGATIVE_INFINITY;
        for (Map.Entry<C, Double> entry : memberships.entrySet()) {
            if (entry.getValue() > best) {
                best = entry.getValue();
                dominant = entry.getKey();
            }
        }
        return dominant;
    }
}
