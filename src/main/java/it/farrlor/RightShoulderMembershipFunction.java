package it.farrlor;

/**
 * Right-shoulder (S-shaped) membership function: zero at and below {@code start},
 * rising linearly to full membership at {@code end} ({@code start < end}). Used
 * for the highest category of a variable so the domain's upper bound keeps full
 * membership.
 * <pre>
 *          ⎧ 0                               x <= start
 *   μ(x) = ⎨ (x - start) / (end - start)     start < x < end
 *          ⎩ 1                               x >= end
 * </pre>
 */
public final class RightShoulderMembershipFunction implements MembershipFunction {

    private final double start;
    private final double end;

    public RightShoulderMembershipFunction(double start, double end) {
        MembershipFunction.requireFinite(start);
        MembershipFunction.requireFinite(end);
        if (!(start < end)) {
            throw new IllegalArgumentException(
                    "Right-shoulder parameters must satisfy start < end but were start=%s, end=%s"
                            .formatted(start, end));
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public double membership(double x) {
        MembershipFunction.requireFinite(x);
        if (x <= start) {
            return 0.0;
        }
        if (x >= end) {
            return 1.0;
        }
        return Math.clamp((x - start) / (end - start), 0.0, 1.0);
    }

    @Override
    public String toString() {
        return "RightShoulder(start=%s, end=%s)".formatted(start, end);
    }
}
