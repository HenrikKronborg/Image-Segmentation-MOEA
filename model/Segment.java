package model;

import model.functions.ImageLoader;
import model.segmentGeneration.Node;

import java.util.ArrayList;

public class Segment {

    private ArrayList<Position> pixels = new ArrayList<>();

    public Segment(){

    }

    public Segment(int x, int y){

    }

    public Segment(ArrayList<Node> list) {
        for(Node n : list) {
            pixels.add(n.getPosition());
        }
    }

    public ArrayList<Position> getPixels() {
        return pixels;
    }

}
