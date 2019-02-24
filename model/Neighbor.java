package model;

public class Neighbor {
    private Pixel pixel;
    private Pixel neighbor;
    private double distance; // Euclidean distance between colors

    public Neighbor(Pixel pixel, Pixel neighbor, double distance) {
        this.pixel = pixel;
        this.neighbor = neighbor;
        this.distance = distance;
    }

    public Pixel getPixel() {
        return pixel;
    }
    public Pixel getNeighbor() {
        return neighbor;
    }
    public double getDistance() {
        return distance;
    }
}