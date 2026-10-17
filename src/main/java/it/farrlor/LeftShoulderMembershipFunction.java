package it.farrlor;

/**
 * Left-shoulder (Z-shaped) membership function: full membership at and below
 * {@code start}, falling linearly to zero at {@code end} ({@code start < end}).
 * Used for the lowest category of a variable so the domain's lower bound keeps
 * full membership.
 * <pre>
 *          ⎧ 1                             x <= start
 *   μ(x) = ⎨ (end - x) / (end - start)     start < x < end
 *          ⎩ 0                             x >= end
 * </pre>
 */
public final class LeftShoulderMembershipFunction implements MembershipFunction {

    private final double start;
    private final double end;

    public LeftShoulderMembershipFunction(double start, double end) {
        MembershipFunction.requireFinite(start);
        MembershipFunction.requireFinite(end);
        if (!(start < end)) {
            throw new IllegalArgumentException(
                    "Left-shoulder parameters must satisfy start < end but were start=%s, end=%s"
                            .formatted(start, end));
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public double membership(double x) {
        MembershipFunction.requireFinite(x);
        if (x <= start) {
            return 1.0;
        }
        if (x >= end) {
            return 0.0;
        }
        return Math.clamp((end - x) / (end - start), 0.0, 1.0);
    }

    @Override
    public String toString() {
        return "LeftShoulder(start=%s, end=%s)".formatted(start, end);
    }
}
