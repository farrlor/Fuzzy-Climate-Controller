package it.farrlor;

import java.time.Duration;
import java.util.Objects;

/**
 * Simulation parameters.
 *
 * @param stepInterval                        wall-clock time represented by one step
 * @param maximumHumidityChangePercentPerStep humidity change (percentage points) at
 *                                            full intensity, {@code |controlSignal| = 1}
 * @param controlSignalDeadband               signals below this magnitude produce no
 *                                            change, damping oscillation near comfort
 * @param minimumHumidityPercent              inclusive lower clamp for humidity
 * @param maximumHumidityPercent              inclusive upper clamp for humidity
 */
public record SimulationConfiguration(Duration stepInterval,
                                      double maximumHumidityChangePercentPerStep,
                                      double controlSignalDeadband,
                                      double minimumHumidityPercent,
                                      double maximumHumidityPercent) {

    public SimulationConfiguration {
        Objects.requireNonNull(stepInterval, "stepInterval");
        if (stepInterval.isZero() || stepInterval.isNegative()) {
            throw new IllegalArgumentException("stepInterval must be positive but was " + stepInterval);
        }
        if (!(maximumHumidityChangePercentPerStep > 0.0) || !Double.isFinite(maximumHumidityChangePercentPerStep)) {
            throw new IllegalArgumentException(
                    "maximumHumidityChangePercentPerStep must be positive and finite but was "
                            + maximumHumidityChangePercentPerStep);
        }
        if (!(controlSignalDeadband >= 0.0 && controlSignalDeadband < 1.0)) {
            throw new IllegalArgumentException(
                    "controlSignalDeadband must be within [0, 1) but was " + controlSignalDeadband);
        }
        if (!(minimumHumidityPercent < maximumHumidityPercent)) {
            throw new IllegalArgumentException(
                    "Humidity clamp bounds must satisfy minimum < maximum but were [%s, %s]"
                            .formatted(minimumHumidityPercent, maximumHumidityPercent));
        }
    }

    /** One step every 2 s, up to 2 pp/step, 5 % deadband, humidity clamped to [0, 100]. */
    public static SimulationConfiguration defaultConfiguration() {
        return new SimulationConfiguration(
                Duration.ofSeconds(2),
                2.0,
                0.05,
                FuzzyControllerFactory.HUMIDITY_MIN_PERCENT,
                FuzzyControllerFactory.HUMIDITY_MAX_PERCENT);
    }
}
