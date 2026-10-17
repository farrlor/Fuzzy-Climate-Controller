package it.farrlor;

/**
 * Parses user-entered temperature and humidity. Invalid input is rejected with an
 * {@link InputValidationException} rather than silently defaulted or clamped. Both
 * '.' and ',' are accepted as the decimal separator (e.g. for the pl_PL locale).
 */
public final class ClimateInputParser {

    private ClimateInputParser() {
        throw new AssertionError("Utility class must not be instantiated");
    }

    public static double parseTemperature(String rawText) {
        double value = parseNumber(rawText, "Temperature");
        if (value < FuzzyControllerFactory.TEMPERATURE_MIN_CELSIUS
                || value > FuzzyControllerFactory.TEMPERATURE_MAX_CELSIUS) {
            throw new InputValidationException(
                    "Temperature must be between %.1f and %.1f °C.".formatted(
                            FuzzyControllerFactory.TEMPERATURE_MIN_CELSIUS,
                            FuzzyControllerFactory.TEMPERATURE_MAX_CELSIUS));
        }
        return value;
    }

    public static double parseHumidity(String rawText) {
        double value = parseNumber(rawText, "Relative humidity");
        if (value < FuzzyControllerFactory.HUMIDITY_MIN_PERCENT
                || value > FuzzyControllerFactory.HUMIDITY_MAX_PERCENT) {
            throw new InputValidationException(
                    "Relative humidity must be between %.0f and %.0f %%.".formatted(
                            FuzzyControllerFactory.HUMIDITY_MIN_PERCENT,
                            FuzzyControllerFactory.HUMIDITY_MAX_PERCENT));
        }
        return value;
    }

    private static double parseNumber(String rawText, String fieldLabel) {
        if (rawText == null || rawText.isBlank()) {
            throw new InputValidationException(fieldLabel + " must not be empty.");
        }
        String normalized = rawText.trim().replace(',', '.');
        double value;
        try {
            value = Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            throw new InputValidationException(fieldLabel + " must be a valid number.");
        }
        if (!Double.isFinite(value)) {
            throw new InputValidationException(fieldLabel + " must be a finite number.");
        }
        return value;
    }
}
