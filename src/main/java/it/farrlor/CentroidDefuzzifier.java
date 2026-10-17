package it.farrlor;

import java.util.function.DoubleUnaryOperator;

/**
 * Centroid (centre-of-gravity) defuzzifier:
 * <pre>
 *   x* = ∫ x·μ(x) dx / ∫ μ(x) dx     over [minimum, maximum]
 * </pre>
 * Both integrals are approximated with the composite trapezoidal rule over a
 * fixed number of intervals, giving a deterministic {@code O(sampleCount)} cost.
 * An empty output set (zero area) has no centroid, so the domain midpoint &ndash;
 * the neutral signal &ndash; is returned instead of {@code NaN}.
 */
public final class CentroidDefuzzifier implements Defuzzifier {

    public static final int DEFAULT_SAMPLE_COUNT = 1_000;
    private static final double AREA_EPSILON = 1.0e-9;

    private final int sampleCount;

    public CentroidDefuzzifier() {
        this(DEFAULT_SAMPLE_COUNT);
    }

    public CentroidDefuzzifier(int sampleCount) {
        if (sampleCount < 1) {
            throw new IllegalArgumentException("sampleCount must be >= 1 but was " + sampleCount);
        }
        this.sampleCount = sampleCount;
    }

    @Override
    public double defuzzify(DoubleUnaryOperator aggregatedMembership, double minimum, double maximum) {
        if (!(minimum < maximum)) {
            throw new IllegalArgumentException(
                    "Output domain must satisfy minimum < maximum but was [%s, %s]".formatted(minimum, maximum));
        }
        double step = (maximum - minimum) / sampleCount;
        double weightedArea = 0.0;
        double area = 0.0;

        double previousX = minimum;
        double previousMu = clampMembership(aggregatedMembership.applyAsDouble(previousX));
        for (int i = 1; i <= sampleCount; i++) {
            double currentX = (i == sampleCount) ? maximum : minimum + i * step;
            double currentMu = clampMembership(aggregatedMembership.applyAsDouble(currentX));
            area += 0.5 * (previousMu + currentMu) * step;
            weightedArea += 0.5 * (previousX * previousMu + currentX * currentMu) * step;
            previousX = currentX;
            previousMu = currentMu;
        }

        if (area < AREA_EPSILON) {
            return (minimum + maximum) / 2.0;
        }
        return Math.clamp(weightedArea / area, minimum, maximum);
    }

    private static double clampMembership(double mu) {
        if (!Double.isFinite(mu)) {
            throw new IllegalArgumentException("Aggregated membership produced a non-finite value: " + mu);
        }
        return Math.clamp(mu, 0.0, 1.0);
    }
}
