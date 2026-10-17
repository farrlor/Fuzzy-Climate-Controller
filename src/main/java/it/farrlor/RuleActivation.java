package it.farrlor;

import java.util.Objects;

/** A rule paired with its firing strength, for diagnostics and explainability. */
public record RuleActivation(FuzzyRule rule, double strength) {

    public RuleActivation {
        Objects.requireNonNull(rule, "rule");
        if (!Double.isFinite(strength) || strength < 0.0 || strength > 1.0) {
            throw new IllegalArgumentException("Rule strength must be within [0, 1] but was " + strength);
        }
    }

    public boolean isActive() {
        return strength > 0.0;
    }
}
