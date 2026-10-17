package it.farrlor;

/** Temperature categories, ordered coldest to hottest. */
public enum TemperatureCategory {

    VERY_COLD("Very cold"),
    COLD("Cold"),
    COMFORTABLE("Comfortable"),
    WARM("Warm"),
    VERY_HOT("Very hot");

    private final String label;

    TemperatureCategory(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
