package it.farrlor;

import java.util.List;
import java.util.Objects;

/**
 * Immutable, ordered set of {@link FuzzyRule}s. {@link #defaultRuleBase()} encodes
 * the control strategy below.
 *
 * <p>Humidity sets the base action (dry → humidify, comfortable → maintain, humid
 * → dehumidify); temperature shifts it, because warm air feels muggier at a given
 * humidity (bias toward drying) while cold air makes dryness more noticeable and
 * high humidity more tolerable (bias toward moistening). On the integer scale
 * {@code STRONG_DEHUMIDIFICATION = -2 … STRONG_HUMIDIFICATION = +2} the consequent
 * is {@code humidityBase + temperatureShift} saturated to {@code [-2, +2]}, with
 * humidity base {@code DRY = +1, COMFORTABLE = 0, HUMID = -1} and temperature
 * shift {@code VERY_COLD/COLD = +1, COMFORTABLE = 0, WARM/VERY_HOT = -1}:
 * <pre>
 *   Temp \ Humidity | DRY                    | COMFORTABLE       | HUMID
 *   ----------------+------------------------+-------------------+------------------------
 *   VERY_COLD       | STRONG_HUMIDIFICATION  | HUMIDIFICATION    | MAINTAIN
 *   COLD            | STRONG_HUMIDIFICATION  | HUMIDIFICATION    | MAINTAIN
 *   COMFORTABLE     | HUMIDIFICATION         | MAINTAIN          | DEHUMIDIFICATION
 *   WARM            | MAINTAIN               | DEHUMIDIFICATION  | STRONG_DEHUMIDIFICATION
 *   VERY_HOT        | MAINTAIN               | DEHUMIDIFICATION  | STRONG_DEHUMIDIFICATION
 * </pre>
 *
 * <p>Unlike the original prototype this never humidifies an already-humid cold
 * room nor dehumidifies an already-dry hot room (both {@code MAINTAIN}), and it is
 * monotonic along both axes, so neighbouring conditions cannot demand opposite
 * corrections.
 */
public final class FuzzyRuleBase {

    private final List<FuzzyRule> rules;

    public FuzzyRuleBase(List<FuzzyRule> rules) {
        if (Objects.requireNonNull(rules, "rules").isEmpty()) {
            throw new IllegalArgumentException("A rule base must contain at least one rule");
        }
        this.rules = List.copyOf(rules);
    }

    public List<FuzzyRule> rules() {
        return rules;
    }

    public static FuzzyRuleBase defaultRuleBase() {
        // Humidity base action on the [-2, +2] scale.
        int dryBase = +1;
        int comfortableBase = 0;
        int humidBase = -1;

        List<FuzzyRule> rules = List.of(
                rule(TemperatureCategory.VERY_COLD, HumidityCategory.DRY, dryBase + 1),
                rule(TemperatureCategory.VERY_COLD, HumidityCategory.COMFORTABLE, comfortableBase + 1),
                rule(TemperatureCategory.VERY_COLD, HumidityCategory.HUMID, humidBase + 1),

                rule(TemperatureCategory.COLD, HumidityCategory.DRY, dryBase + 1),
                rule(TemperatureCategory.COLD, HumidityCategory.COMFORTABLE, comfortableBase + 1),
                rule(TemperatureCategory.COLD, HumidityCategory.HUMID, humidBase + 1),

                rule(TemperatureCategory.COMFORTABLE, HumidityCategory.DRY, dryBase),
                rule(TemperatureCategory.COMFORTABLE, HumidityCategory.COMFORTABLE, comfortableBase),
                rule(TemperatureCategory.COMFORTABLE, HumidityCategory.HUMID, humidBase),

                rule(TemperatureCategory.WARM, HumidityCategory.DRY, dryBase - 1),
                rule(TemperatureCategory.WARM, HumidityCategory.COMFORTABLE, comfortableBase - 1),
                rule(TemperatureCategory.WARM, HumidityCategory.HUMID, humidBase - 1),

                rule(TemperatureCategory.VERY_HOT, HumidityCategory.DRY, dryBase - 1),
                rule(TemperatureCategory.VERY_HOT, HumidityCategory.COMFORTABLE, comfortableBase - 1),
                rule(TemperatureCategory.VERY_HOT, HumidityCategory.HUMID, humidBase - 1));

        return new FuzzyRuleBase(rules);
    }

    private static FuzzyRule rule(TemperatureCategory temperature, HumidityCategory humidity, int level) {
        return new FuzzyRule(temperature, humidity, actionForLevel(level));
    }

    private static ControlAction actionForLevel(int level) {
        int saturated = Math.clamp(level, -2, 2);
        return switch (saturated) {
            case -2 -> ControlAction.STRONG_DEHUMIDIFICATION;
            case -1 -> ControlAction.DEHUMIDIFICATION;
            case 0 -> ControlAction.MAINTAIN;
            case 1 -> ControlAction.HUMIDIFICATION;
            case 2 -> ControlAction.STRONG_HUMIDIFICATION;
            default -> throw new IllegalStateException("Unreachable level " + saturated);
        };
    }
}
