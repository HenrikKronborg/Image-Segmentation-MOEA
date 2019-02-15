package model;

import model.functions.ImageLoader;
import model.segmentGeneration.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Chromosome {
    private int[] gene;

    /*
     * Methods
     */
    public Segment[] generatePhenotype() {
        Node[] nodes = new Node[gene.length];
        ArrayList<Integer> notPlaced = new ArrayList<>();

        for(int i = 0;i < gene.length; i++){
            nodes[i] = new Node(i);
            notPlaced.add(i);
        }

        for(int i = 0;i < gene.length;i++){
            nodes[i].setChild(nodes[gene[i]]);
        }

        ArrayList<Segment> segments = new ArrayList<>();

        while (notPlaced.size() >0){
            Node toRemove = nodes[notPlaced.remove(0)];

            ArrayList<Node> list = generateSegment(toRemove);

            segments.add(new Segment(list));
        }

        return null;
    }

    private ArrayList<Node> generateSegment(Node node){
        ArrayList<Node> segment = new ArrayList<>();

        return null;

    }
    public Position[] getSegmentBorder() {
        return null;
    }

    public void generateGenFromImage(ImageLoader img){


    }

}
