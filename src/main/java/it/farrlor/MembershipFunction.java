package it.farrlor;

/**
 * A fuzzy membership function μ(x) over a one-dimensional universe of discourse.
 * Implementations are immutable and must return a finite value in {@code [0, 1]}
 * for every finite input.
 */
@FunctionalInterface
public interface MembershipFunction {

    double membership(double x);

    /**
     * Rejects {@code NaN} and infinite values so they never propagate through the
     * inference pipeline.
     */
    static double requireFinite(double x) {
        if (!Double.isFinite(x)) {
            throw new IllegalArgumentException("Membership input must be finite but was " + x);
        }
        return x;
    }
}
