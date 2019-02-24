package model;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Pixel {
    private int x;
    private int y;
    private Color color;
    private ArrayList<Neighbor> neighbors = new ArrayList<>();

    public Pixel(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public ArrayList<Neighbor> getNeighbors() {
        return neighbors;
    }
    public void addNeighbor(Pixel neighbor) {
        double distance = Math.sqrt(Math.pow(neighbor.getColor().getRed()-color.getRed(),2)+Math.pow(neighbor.getColor().getGreen()-color.getGreen(),2)+Math.pow(neighbor.getColor().getBlue()-color.getBlue(),2));

        this.neighbors.add(new Neighbor(this, neighbor, distance));
    }
}