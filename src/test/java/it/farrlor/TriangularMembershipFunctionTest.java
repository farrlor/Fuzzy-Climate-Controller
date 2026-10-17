package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TriangularMembershipFunctionTest {

    private static final double DELTA = 1.0e-9;
    private final MembershipFunction triangle = new TriangularMembershipFunction(0.0, 5.0, 10.0);

    @Test
    @DisplayName("peak evaluates to 1 and feet evaluate to 0")
    void peakAndFeet() {
        assertAll(
                () -> assertEquals(0.0, triangle.membership(0.0), DELTA),
                () -> assertEquals(1.0, triangle.membership(5.0), DELTA),
                () -> assertEquals(0.0, triangle.membership(10.0), DELTA));
    }

    @Test
    @DisplayName("rising and falling slopes are linear")
    void slopes() {
        assertAll(
                () -> assertEquals(0.5, triangle.membership(2.5), DELTA),
                () -> assertEquals(0.25, triangle.membership(1.25), DELTA),
                () -> assertEquals(0.5, triangle.membership(7.5), DELTA),
                () -> assertEquals(0.25, triangle.membership(8.75), DELTA));
    }

    @Test
    @DisplayName("values outside the support are zero")
    void outsideSupport() {
        assertAll(
                () -> assertEquals(0.0, triangle.membership(-100.0), DELTA),
                () -> assertEquals(0.0, triangle.membership(100.0), DELTA));
    }

    @Test
    @DisplayName("a left-clamped triangle (a == b) has a vertical rising edge")
    void degenerateLeftEdge() {
        MembershipFunction clamped = new TriangularMembershipFunction(0.0, 0.0, 4.0);
        assertAll(
                () -> assertEquals(1.0, clamped.membership(0.0), DELTA),
                () -> assertEquals(0.5, clamped.membership(2.0), DELTA),
                () -> assertEquals(0.0, clamped.membership(-0.001), DELTA));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-5.0, 0.0, 2.3, 5.0, 7.7, 10.0, 15.0})
    @DisplayName("membership is always within [0, 1]")
    void withinUnitInterval(double x) {
        double mu = triangle.membership(x);
        assertTrue(mu >= 0.0 && mu <= 1.0, "mu=" + mu);
    }

    @Test
    @DisplayName("invalid parameter ordering is rejected")
    void invalidOrdering() {
        assertThrows(IllegalArgumentException.class, () -> new TriangularMembershipFunction(10.0, 5.0, 0.0));
    }

    @Test
    @DisplayName("non-finite constructor parameters are rejected")
    void nonFiniteParameters() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new TriangularMembershipFunction(Double.NaN, 1.0, 2.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new TriangularMembershipFunction(0.0, 1.0, Double.POSITIVE_INFINITY)));
    }

    @Test
    @DisplayName("non-finite input is rejected")
    void nonFiniteInput() {
        assertThrows(IllegalArgumentException.class, () -> triangle.membership(Double.NaN));
    }
}
