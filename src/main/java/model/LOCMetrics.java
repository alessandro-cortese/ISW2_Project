package model;

public class LOCMetrics {

    private int maxLoc;
    private int loc;
    private double avgLoc;

    public LOCMetrics() {}

    public LOCMetrics(int maxLoc, int loc, double avgLoc) {
        this.maxLoc = maxLoc;
        this.loc = loc;
        this.avgLoc = avgLoc;
    }

    public int getMaxLoc() {
        return maxLoc;
    }

    public void setMaxLoc(int maxLoc) {
        this.maxLoc = maxLoc;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public double getAvgLoc() {
        return avgLoc;
    }

    public void setAvgLoc(double avgLoc) {
        this.avgLoc = avgLoc;
    }
}