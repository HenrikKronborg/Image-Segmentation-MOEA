package model;

import model.segmentGeneration.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Segment {

    private HashSet<Position> pixels = new HashSet<>();

    public Segment(){

    }

    public Segment(Color[][] list){
        for(int i = 0; i < list.length; i++){
            for(int j = 0; j < list[i].length; j++){
                pixels.add(new Position(j,i));
            }
        }
    }

    public Segment(ArrayList<Node> list) {
        for(Node n : list) {
            pixels.add(n.getPosition());
        }
    }

    public ArrayList<Position> getPixels() {
        return new ArrayList<>(pixels);
    }

    public boolean contains(int x, int y) {
        return pixels.contains(new Position(x,y));
    }
}
