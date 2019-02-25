package model;

import model.supportNodes.Node;
import model.supportNodes.Position;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Segment {

    private HashSet<Position> pixels;

    public Segment() {
        pixels = new HashSet<>();
    }

    public Segment(Color[][] list) {
        for(int i = 0; i < list.length; i++) {
            for(int j = 0; j < list[i].length; j++) {
                pixels.add(new Position(j,i));
            }
        }
    }

    public Segment(ArrayList<Node> list) {
        pixels = new HashSet<>();
        for(Node n : list) {
            pixels.add(n.getPosition());
        }
    }
    public void addTo(Position p){
        pixels.add(p);
    }


    public ArrayList<Position> getPixels() {
        return new ArrayList<>(pixels);
    }

    public boolean contains(int x, int y) {
        return pixels.contains(new Position(x,y));
    }

    public boolean contains(Position p) {
        return pixels.contains(p);
    }
}
