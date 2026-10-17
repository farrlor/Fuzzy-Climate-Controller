package it.farrlor;

import java.util.Objects;

/**
 * Outcome of one simulation step: its 1-based index, the resulting environment, the
 * inference that produced it and the applied humidity change (percentage points).
 */
public record SimulationTick(long stepIndex,
                             EnvironmentState environment,
                             FuzzyInferenceResult inference,
                             double humidityChangePercent) {

    public SimulationTick {
        if (stepIndex < 1) {
            throw new IllegalArgumentException("stepIndex must be >= 1 but was " + stepIndex);
        }
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(inference, "inference");
        if (!Double.isFinite(humidityChangePercent)) {
            throw new IllegalArgumentException(
                    "humidityChangePercent must be finite but was " + humidityChangePercent);
        }
    }
}
