package model.supportNodes;

import java.awt.*;
import java.util.ArrayList;

public class SegmentNode {

    private short id;

    private int nrPixels;
    private double avgRed;
    private double avgGreen;
    private double avgBlue;

    private int rank;

    private boolean f = false;

    ArrayList<Integer> neighbors = new ArrayList<>();

    public void addColor(Color c) {
        avgBlue += c.getBlue();
        avgGreen +=  c.getGreen();
        avgRed += c.getRed();

        nrPixels++;
    }
    public void addColor(double red, double green, double blue) {
        avgBlue += blue;
        avgGreen +=  green;
        avgRed += red;

    }

    public void makeAvg() {
        avgBlue /= nrPixels;
        avgGreen /= nrPixels;
        avgRed /= nrPixels;
    }

    public void addNeighbor(Integer add){
        neighbors.add(add);
    }

    public double getAvgRed() {
        return avgRed;
    }

    public void setAvgRed(double avgRed) {
        this.avgRed = avgRed;
    }

    public double getAvgGreen() {
        return avgGreen;
    }

    public void setAvgGreen(double avgGreen) {
        this.avgGreen = avgGreen;
    }

    public double getAvgBlue() {
        return avgBlue;
    }

    public void setAvgBlue(double avgBlue) {
        this.avgBlue = avgBlue;
    }

    public ArrayList<Integer> getNeighbors() {
        return neighbors;
    }

    public int getNrPixels() {
        return nrPixels;
    }

    public void setNrPixels(int nrPixels) {
        this.nrPixels = nrPixels;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public boolean isF() {
        return f;
    }

    public void setF(boolean f) {
        this.f = f;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
