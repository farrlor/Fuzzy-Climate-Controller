package it.farrlor;

/** Immutable snapshot of the measured temperature (°C) and relative humidity (%). */
public record EnvironmentState(double temperatureCelsius, double relativeHumidityPercent) {

    public EnvironmentState {
        if (!Double.isFinite(temperatureCelsius)) {
            throw new IllegalArgumentException("temperatureCelsius must be finite but was " + temperatureCelsius);
        }
        if (!Double.isFinite(relativeHumidityPercent)) {
            throw new IllegalArgumentException(
                    "relativeHumidityPercent must be finite but was " + relativeHumidityPercent);
        }
    }

    public EnvironmentState withRelativeHumidity(double newRelativeHumidityPercent) {
        return new EnvironmentState(temperatureCelsius, newRelativeHumidityPercent);
    }

    public EnvironmentState withTemperature(double newTemperatureCelsius) {
        return new EnvironmentState(newTemperatureCelsius, relativeHumidityPercent);
    }
}
