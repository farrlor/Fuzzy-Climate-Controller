package it.farrlor;

import java.util.EnumMap;
import java.util.Map;

/**
 * Single source of truth for the controller's fuzzy configuration. Declares the
 * domain boundaries as named constants and builds the temperature, humidity and
 * output {@link FuzzyVariable}s, the default {@link FuzzyRuleBase} and a wired
 * {@link FuzzyInferenceEngine}. See the README for the full set of membership
 * functions and their parameters.
 */
public final class FuzzyControllerFactory {

    // --- Temperature domain and membership boundaries (degrees Celsius) ---
    public static final double TEMPERATURE_MIN_CELSIUS = 20.0;
    public static final double TEMPERATURE_MAX_CELSIUS = 27.0;
    private static final double TEMP_PEAK_VERY_COLD = 20.0;
    private static final double TEMP_PEAK_COLD = 21.75;
    private static final double TEMP_PEAK_COMFORTABLE = 23.5;
    private static final double TEMP_PEAK_WARM = 25.25;
    private static final double TEMP_PEAK_VERY_HOT = 27.0;

    // --- Humidity domain and membership boundaries (percent) ---
    public static final double HUMIDITY_MIN_PERCENT = 0.0;
    public static final double HUMIDITY_MAX_PERCENT = 100.0;

    // --- Output (control signal) domain ---
    public static final double CONTROL_SIGNAL_MIN = -1.0;
    public static final double CONTROL_SIGNAL_MAX = 1.0;

    private FuzzyControllerFactory() {
        throw new AssertionError("Utility class must not be instantiated");
    }

    public static FuzzyVariable<TemperatureCategory> createTemperatureVariable() {
        Map<TemperatureCategory, MembershipFunction> sets = new EnumMap<>(TemperatureCategory.class);
        sets.put(TemperatureCategory.VERY_COLD,
                new LeftShoulderMembershipFunction(TEMP_PEAK_VERY_COLD, TEMP_PEAK_COLD));
        sets.put(TemperatureCategory.COLD,
                new TriangularMembershipFunction(TEMP_PEAK_VERY_COLD, TEMP_PEAK_COLD, TEMP_PEAK_COMFORTABLE));
        sets.put(TemperatureCategory.COMFORTABLE,
                new TriangularMembershipFunction(TEMP_PEAK_COLD, TEMP_PEAK_COMFORTABLE, TEMP_PEAK_WARM));
        sets.put(TemperatureCategory.WARM,
                new TriangularMembershipFunction(TEMP_PEAK_COMFORTABLE, TEMP_PEAK_WARM, TEMP_PEAK_VERY_HOT));
        sets.put(TemperatureCategory.VERY_HOT,
                new RightShoulderMembershipFunction(TEMP_PEAK_WARM, TEMP_PEAK_VERY_HOT));
        return new FuzzyVariable<>("temperature", TEMPERATURE_MIN_CELSIUS, TEMPERATURE_MAX_CELSIUS, sets);
    }

    public static FuzzyVariable<HumidityCategory> createHumidityVariable() {
        Map<HumidityCategory, MembershipFunction> sets = new EnumMap<>(HumidityCategory.class);
        sets.put(HumidityCategory.DRY, new LeftShoulderMembershipFunction(30.0, 50.0));
        sets.put(HumidityCategory.COMFORTABLE, new TrapezoidalMembershipFunction(35.0, 45.0, 55.0, 65.0));
        sets.put(HumidityCategory.HUMID, new RightShoulderMembershipFunction(50.0, 70.0));
        return new FuzzyVariable<>("relativeHumidity", HUMIDITY_MIN_PERCENT, HUMIDITY_MAX_PERCENT, sets);
    }

    public static FuzzyVariable<ControlAction> createOutputVariable() {
        Map<ControlAction, MembershipFunction> sets = new EnumMap<>(ControlAction.class);
        sets.put(ControlAction.STRONG_DEHUMIDIFICATION, new LeftShoulderMembershipFunction(-1.0, -0.5));
        sets.put(ControlAction.DEHUMIDIFICATION, new TriangularMembershipFunction(-1.0, -0.5, 0.0));
        sets.put(ControlAction.MAINTAIN, new TriangularMembershipFunction(-0.5, 0.0, 0.5));
        sets.put(ControlAction.HUMIDIFICATION, new TriangularMembershipFunction(0.0, 0.5, 1.0));
        sets.put(ControlAction.STRONG_HUMIDIFICATION, new RightShoulderMembershipFunction(0.5, 1.0));
        return new FuzzyVariable<>("controlSignal", CONTROL_SIGNAL_MIN, CONTROL_SIGNAL_MAX, sets);
    }

    public static FuzzyInferenceEngine createEngine() {
        return new FuzzyInferenceEngine(
                createTemperatureVariable(),
                createHumidityVariable(),
                createOutputVariable(),
                FuzzyRuleBase.defaultRuleBase(),
                new CentroidDefuzzifier());
    }
}
