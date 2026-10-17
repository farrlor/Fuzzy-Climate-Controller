package it.farrlor;

/**
 * Triangular membership function with feet at {@code a} and {@code c} and peak at
 * {@code b} ({@code a <= b <= c}):
 * <pre>
 *          ⎧ 0                     x <= a
 *   μ(x) = ⎨ (x - a) / (b - a)     a < x <= b
 *          ⎪ (c - x) / (c - b)     b < x < c
 *          ⎩ 0                     x >= c
 * </pre>
 * {@code a == b} or {@code b == c} produces a vertical edge, allowing a triangle
 * clamped against a domain boundary.
 */
public final class TriangularMembershipFunction implements MembershipFunction {

    private final double a;
    private final double b;
    private final double c;

    public TriangularMembershipFunction(double a, double b, double c) {
        MembershipFunction.requireFinite(a);
        MembershipFunction.requireFinite(b);
        MembershipFunction.requireFinite(c);
        if (!(a <= b && b <= c)) {
            throw new IllegalArgumentException(
                    "Triangular parameters must satisfy a <= b <= c but were a=%s, b=%s, c=%s".formatted(a, b, c));
        }
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public double membership(double x) {
        MembershipFunction.requireFinite(x);
        if (x == b) {
            return 1.0;
        }
        if (x <= a || x >= c) {
            return 0.0;
        }
        double value = x < b ? (x - a) / (b - a) : (c - x) / (c - b);
        return Math.clamp(value, 0.0, 1.0);
    }

    @Override
    public String toString() {
        return "Triangular(a=%s, b=%s, c=%s)".formatted(a, b, c);
    }
}
