package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TrapezoidalMembershipFunctionTest {

    private static final double DELTA = 1.0e-9;
    private final MembershipFunction trapezoid = new TrapezoidalMembershipFunction(0.0, 2.0, 8.0, 10.0);

    @Test
    @DisplayName("plateau evaluates to 1 and feet evaluate to 0")
    void plateauAndFeet() {
        assertAll(
                () -> assertEquals(0.0, trapezoid.membership(0.0), DELTA),
                () -> assertEquals(1.0, trapezoid.membership(2.0), DELTA),
                () -> assertEquals(1.0, trapezoid.membership(5.0), DELTA),
                () -> assertEquals(1.0, trapezoid.membership(8.0), DELTA),
                () -> assertEquals(0.0, trapezoid.membership(10.0), DELTA));
    }

    @Test
    @DisplayName("rising and falling slopes are linear")
    void slopes() {
        assertAll(
                () -> assertEquals(0.5, trapezoid.membership(1.0), DELTA),
                () -> assertEquals(0.5, trapezoid.membership(9.0), DELTA));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, 0.0, 1.0, 5.0, 9.0, 10.0, 20.0})
    @DisplayName("membership is always within [0, 1]")
    void withinUnitInterval(double x) {
        double mu = trapezoid.membership(x);
        assertTrue(mu >= 0.0 && mu <= 1.0, "mu=" + mu);
    }

    @Test
    @DisplayName("invalid parameter ordering is rejected")
    void invalidOrdering() {
        assertThrows(IllegalArgumentException.class,
                () -> new TrapezoidalMembershipFunction(0.0, 5.0, 3.0, 10.0));
    }

    @Test
    @DisplayName("a degenerate trapezoid behaves like a triangle")
    void degenerateIsTriangle() {
        MembershipFunction triangleLike = new TrapezoidalMembershipFunction(0.0, 5.0, 5.0, 10.0);
        assertAll(
                () -> assertEquals(1.0, triangleLike.membership(5.0), DELTA),
                () -> assertEquals(0.5, triangleLike.membership(2.5), DELTA));
    }
}
