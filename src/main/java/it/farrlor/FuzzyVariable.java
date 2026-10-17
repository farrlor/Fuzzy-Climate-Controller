package it.farrlor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * A linguistic variable over a bounded domain, pairing each category with its
 * membership function. {@link #fuzzify(double)} clamps the input into
 * {@code [minimum, maximum]} before evaluation, so values at or beyond the
 * boundary are absorbed by the shoulder sets rather than producing gaps.
 *
 * @param <C> the enumeration of linguistic categories
 */
public final class FuzzyVariable<C extends Enum<C>> {

    private final String name;
    private final double minimum;
    private final double maximum;
    private final Map<C, MembershipFunction> sets;

    public FuzzyVariable(String name, double minimum, double maximum, Map<C, MembershipFunction> sets) {
        this.name = Objects.requireNonNull(name, "name");
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum)) {
            throw new IllegalArgumentException("Domain bounds must be finite");
        }
        if (!(minimum < maximum)) {
            throw new IllegalArgumentException(
                    "Domain bounds must satisfy minimum < maximum but were [%s, %s]".formatted(minimum, maximum));
        }
        if (Objects.requireNonNull(sets, "sets").isEmpty()) {
            throw new IllegalArgumentException("A fuzzy variable must declare at least one category");
        }
        EnumMap<C, MembershipFunction> copy = new EnumMap<>(sets);
        copy.forEach((category, function) -> Objects.requireNonNull(function, "membership function for " + category));
        this.minimum = minimum;
        this.maximum = maximum;
        this.sets = Collections.unmodifiableMap(copy);
    }

    /** Membership degree of every category for the (clamped) crisp value. */
    public Map<C, Double> fuzzify(double crispValue) {
        MembershipFunction.requireFinite(crispValue);
        double clamped = Math.clamp(crispValue, minimum, maximum);
        EnumMap<C, Double> result = new EnumMap<>(sets.keySet().iterator().next().getDeclaringClass());
        sets.forEach((category, function) -> result.put(category, function.membership(clamped)));
        return Collections.unmodifiableMap(result);
    }

    public MembershipFunction membershipFunction(C category) {
        MembershipFunction function = sets.get(category);
        if (function == null) {
            throw new IllegalArgumentException("Variable '" + name + "' has no category " + category);
        }
        return function;
    }

    public double minimum() {
        return minimum;
    }

    public double maximum() {
        return maximum;
    }
}
