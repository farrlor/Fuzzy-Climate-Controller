package it.farrlor;

/** Lifecycle state of the {@link SimulationEngine}. Only RUNNING advances on a step. */
public enum SimulationState {

    STOPPED("Stopped"),
    RUNNING("Running"),
    PAUSED("Paused");

    private final String label;

    SimulationState(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
