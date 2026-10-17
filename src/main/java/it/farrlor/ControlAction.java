package it.farrlor;

/**
 * Output categories of the controller and their representative crisp positions on
 * the control-signal axis {@code [-1, +1]}: negative removes moisture, zero holds,
 * positive adds moisture. The representative signals are the peaks of the output
 * fuzzy sets ({@link FuzzyControllerFactory}) and the anchors used by
 * {@link #fromSignal(double)}.
 */
public enum ControlAction {

    STRONG_DEHUMIDIFICATION("Strong dehumidification", -1.0),
    DEHUMIDIFICATION("Dehumidification", -0.5),
    MAINTAIN("Maintain", 0.0),
    HUMIDIFICATION("Humidification", 0.5),
    STRONG_HUMIDIFICATION("Strong humidification", 1.0);

    private final String label;
    private final double representativeSignal;

    ControlAction(String label, double representativeSignal) {
        this.label = label;
        this.representativeSignal = representativeSignal;
    }

    public String label() {
        return label;
    }

    public double representativeSignal() {
        return representativeSignal;
    }

    /**
     * Resolves a crisp signal to the action with the nearest representative signal.
     * Ties favour the first action in declaration order.
     */
    public static ControlAction fromSignal(double signal) {
        ControlAction nearest = MAINTAIN;
        double smallestDistance = Double.MAX_VALUE;
        for (ControlAction action : values()) {
            double distance = Math.abs(action.representativeSignal - signal);
            if (distance < smallestDistance) {
                smallestDistance = distance;
                nearest = action;
            }
        }
        return nearest;
    }
}
