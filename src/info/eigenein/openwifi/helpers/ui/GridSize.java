package info.eigenein.openwifi.helpers.ui;

public class GridSize {
    private final double latitideStep;

    private final double longitudeStep;

    public static GridSize fromLongitudeStep(final double longitudeStep) {
        return new GridSize(longitudeStep / 2.0, longitudeStep);
    }

    private GridSize(final double latitideStep, final double longitudeStep) {
        this.latitideStep = latitideStep;
        this.longitudeStep = longitudeStep;
    }

    public double getLatitideStep() {
        return latitideStep;
    }

    public double getLongitudeStep() {
        return longitudeStep;
    }
}
