package uk.gov.ukho.aisbatchlambda;

public class Job {
    private final Double resolution;
    private final String prefix;

    public Job(final Double resolution, final String prefix) {
        this.resolution = resolution;
        this.prefix = prefix;
    }

    public Double getResolution() {
        return resolution;
    }

    public String getPrefix() {
        return prefix;
    }
}
