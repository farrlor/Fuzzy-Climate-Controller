package it.farrlor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShoulderMembershipFunctionTest {

    private static final double DELTA = 1.0e-9;

    @Test
    @DisplayName("left shoulder is full below start and zero above end")
    void leftShoulder() {
        MembershipFunction left = new LeftShoulderMembershipFunction(2.0, 6.0);
        assertAll(
                () -> assertEquals(1.0, left.membership(-10.0), DELTA),
                () -> assertEquals(1.0, left.membership(2.0), DELTA),
                () -> assertEquals(0.5, left.membership(4.0), DELTA),
                () -> assertEquals(0.0, left.membership(6.0), DELTA),
                () -> assertEquals(0.0, left.membership(10.0), DELTA));
    }

    @Test
    @DisplayName("right shoulder is zero below start and full above end")
    void rightShoulder() {
        MembershipFunction right = new RightShoulderMembershipFunction(2.0, 6.0);
        assertAll(
                () -> assertEquals(0.0, right.membership(-10.0), DELTA),
                () -> assertEquals(0.0, right.membership(2.0), DELTA),
                () -> assertEquals(0.5, right.membership(4.0), DELTA),
                () -> assertEquals(1.0, right.membership(6.0), DELTA),
                () -> assertEquals(1.0, right.membership(10.0), DELTA));
    }

    @Test
    @DisplayName("shoulder constructors reject start >= end and non-finite parameters")
    void invalidParameters() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new LeftShoulderMembershipFunction(6.0, 2.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new RightShoulderMembershipFunction(5.0, 5.0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new LeftShoulderMembershipFunction(Double.NaN, 1.0)));
    }
}
