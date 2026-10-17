package it.farrlor;

/** Relative-humidity categories, ordered driest to most humid. */
public enum HumidityCategory {

    DRY("Dry"),
    COMFORTABLE("Comfortable"),
    HUMID("Humid");

    private final String label;

    HumidityCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
