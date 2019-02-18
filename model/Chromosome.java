package model;

import model.functions.ImageLoader;
import model.segmentGeneration.Node;

import java.util.*;

public class Chromosome {
    private int[] gene;

    /*
     * Methods
     */
    public Chromosome(int[] gene){
        this.gene = gene;
    }

    public void generateRandomGene(int width, int height){
        gene = new int[(width*height)];
        Random rand = new Random();

        int x = 0;
        int y = 0;
        for(int i = 0; i < gene.length; i++){
            int select = 0; // Equals 0 to 4 representing all neighboring cells and itself.
            if(x == width){
                x = 0;
                y++;
            }

            if(x == 0){
                if(y == 0){
                    int rnd = rand.nextInt(3); // 0,1 and 4 available.
                    if(rnd == 2){
                        rnd = 4;
                    }
                    select = rnd;

                }else if(y == height -1){
                    int rnd = rand.nextInt(3); // 0,1 and 3 available.
                    if(rnd == 2){
                        rnd = 3;
                    }
                    select = rnd;

                }else{
                    int rnd = rand.nextInt(4); // 0,1,3 and 4 available.
                    if(rnd == 2){
                        rnd = 4;
                    }
                    select = rnd;

                }

            } else if(x == width -1){
                if(y == 0){
                    int rnd = rand.nextInt(3); // 0,2 and 4 available.
                    if(rnd == 1){
                        rnd = 4;
                    }
                    select = rnd;

                }else if(y == height -1){
                    int rnd = rand.nextInt(3); // 0,2 and 3 available.
                    if(rnd == 1){
                        rnd = 3;
                    }
                    select = rnd;


                }else{
                    int rnd = rand.nextInt(4); // 0,2,3 and 4 available.
                    if(rnd == 1){
                        rnd = 4;
                    }
                    select = rnd;
                }
            }else{
                if(y == 0){
                    int rnd = rand.nextInt(4); // 0,1,2 and 4 available.
                    if(rnd == 3){
                        rnd = 4;
                    }
                    select = rnd;

                }else if(y == height -1){
                    select = rand.nextInt(4); // 0,1,2 and 3 available.

                }else {
                    select = rand.nextInt(5);// 0,1,2,3 and 4 available.
                }
            }

            switch (select) {
                case 0:             // links to itself.
                    gene[i] = i;
                    break;
                case 1:
                    gene[i] = i + 1; // links to the next node (X dir.)
                    break;
                case 2:
                    gene[i] = i - 1; // links to the previous node (X dir.)
                    break;
                case 3:
                    gene[i] = i - width; // links to the upper node (Y dir.)
                    break;
                case 4:
                    gene[i] = i + width; // links to the bottom node (Y dir.)
                    break;
            }

            x++;
        }
    }
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
