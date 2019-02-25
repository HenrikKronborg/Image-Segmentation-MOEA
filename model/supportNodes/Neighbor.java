package model.supportNodes;

import model.supportNodes.Pixel;

public class Neighbor implements Comparable<Neighbor> {
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

    @Override
    public int compareTo(Neighbor o) {
        double cmp = this.distance - o.distance;
        if(cmp > 0){
            return 1;
        } else if(cmp == 0){
            return 0;
        }
        return -1;
    }
}

