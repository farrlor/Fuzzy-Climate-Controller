package it.farrlor;

import java.util.function.DoubleUnaryOperator;

/**
 * Reduces an aggregated output set μ(x), defined over {@code [minimum, maximum]},
 * to a single crisp value.
 */
@FunctionalInterface
public interface Defuzzifier {

    double defuzzify(DoubleUnaryOperator aggregatedMembership, double minimum, double maximum);
}
