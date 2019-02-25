package model.supportNodes;

import java.awt.*;
import java.util.ArrayList;

public class Pixel extends Position{
    private Color color;
    private ArrayList<Neighbor> neighbors = new ArrayList<>();
    private boolean isPlaced = false;

    public Pixel(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
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

    public boolean isPlaced() {
        return isPlaced;
    }

    public void setPlaced(boolean placed) {
        isPlaced = placed;
    }
}