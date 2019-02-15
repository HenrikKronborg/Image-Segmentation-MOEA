package model;

import model.functions.ImageLoader;
import model.segmentGeneration.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Chromosome {
    private int[] gene;

    /*
     * Methods
     */
    public  List<Segment> generatePhenotype() {
        Node[] nodes = new Node[gene.length];
        ArrayList<Node> notPlaced = new ArrayList<>();

        for(int i = 0;i < gene.length; i++){
            nodes[i] = new Node(i);
            notPlaced.add(nodes[i]);
        }

        Node.generatePositions(nodes);

        for(int i = 0;i < gene.length;i++){
            nodes[i].setChild(nodes[gene[i]]);
        }

        ArrayList<Segment> segments = new ArrayList<>();

        while (notPlaced.size() > 0){
            Node toRemove = notPlaced.remove(0);

            HashSet<Node> list = generateSegment(toRemove, new HashSet<>());

            notPlaced.removeAll(list);

            segments.add(new Segment(new ArrayList<>(list)));
        }

        return segments;
    }

    private HashSet<Node> generateSegment(Node node, HashSet<Node> segment){
        if(segment.contains(node)){
            return segment;
        }else{
            segment.add(node);
        }

        generateSegment(node.getChild(),segment);
        for(Node n : node.getParents()){
            generateSegment(n,segment);
        }

        return segment;

    }
    public Position[] getSegmentBorder() {
        return null;
    }

    public void generateGenFromImage(ImageLoader img){


    }

}
