package it.farrlor;

/**
 * Trapezoidal membership function with feet at {@code a}, {@code d} and a unit
 * plateau between {@code b} and {@code c} ({@code a <= b <= c <= d}):
 * <pre>
 *          ⎧ 0                     x <= a
 *          ⎪ (x - a) / (b - a)     a < x < b
 *   μ(x) = ⎨ 1                     b <= x <= c
 *          ⎪ (d - x) / (d - c)     c < x < d
 *          ⎩ 0                     x >= d
 * </pre>
 * {@code a == b} or {@code c == d} produces a vertical edge.
 */
public final class TrapezoidalMembershipFunction implements MembershipFunction {

    private final double a;
    private final double b;
    private final double c;
    private final double d;

    public TrapezoidalMembershipFunction(double a, double b, double c, double d) {
        MembershipFunction.requireFinite(a);
        MembershipFunction.requireFinite(b);
        MembershipFunction.requireFinite(c);
        MembershipFunction.requireFinite(d);
        if (!(a <= b && b <= c && c <= d)) {
            throw new IllegalArgumentException(
                    "Trapezoidal parameters must satisfy a <= b <= c <= d but were a=%s, b=%s, c=%s, d=%s"
                            .formatted(a, b, c, d));
        }
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    @Override
    public double membership(double x) {
        MembershipFunction.requireFinite(x);
        if (x >= b && x <= c) {
            return 1.0;
        }
        if (x <= a || x >= d) {
            return 0.0;
        }
        double value = x < b ? (x - a) / (b - a) : (d - x) / (d - c);
        return Math.clamp(value, 0.0, 1.0);
    }

    @Override
    public String toString() {
        return "Trapezoidal(a=%s, b=%s, c=%s, d=%s)".formatted(a, b, c, d);
    }
}
