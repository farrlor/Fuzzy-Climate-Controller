package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CentroidDefuzzifierTest {

    private final Defuzzifier defuzzifier = new CentroidDefuzzifier();

    @Test
    @DisplayName("centroid of a constant set is the midpoint of the domain")
    void constantSet() {
        assertEquals(5.0, defuzzifier.defuzzify(x -> 1.0, 0.0, 10.0), 1.0e-6);
    }

    @Test
    @DisplayName("centroid of a symmetric triangle is its axis of symmetry")
    void symmetricTriangle() {
        MembershipFunction triangle = new TriangularMembershipFunction(-1.0, 0.0, 1.0);
        assertEquals(0.0, defuzzifier.defuzzify(triangle::membership, -1.0, 1.0), 1.0e-6);
    }

    @Test
    @DisplayName("centroid of a linear ramp matches the analytical value")
    void linearRamp() {
        // mu(x) = x/10 on [0, 10]; centroid = (integral x^2/10) / (integral x/10) = 20/3.
        DoubleUnaryOperator ramp = x -> x / 10.0;
        assertEquals(20.0 / 3.0, defuzzifier.defuzzify(ramp, 0.0, 10.0), 1.0e-3);
    }

    @Test
    @DisplayName("an empty output set falls back to the neutral midpoint")
    void emptySet() {
        assertEquals(0.0, defuzzifier.defuzzify(x -> 0.0, -1.0, 1.0), 1.0e-9);
    }

    @Test
    @DisplayName("invalid domains and sample counts are rejected")
    void invalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> defuzzifier.defuzzify(x -> 1.0, 1.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new CentroidDefuzzifier(0));
    }

    @Test
    @DisplayName("a non-finite aggregated membership is rejected")
    void nonFiniteMembership() {
        assertThrows(IllegalArgumentException.class,
                () -> defuzzifier.defuzzify(x -> Double.NaN, -1.0, 1.0));
    }
}
