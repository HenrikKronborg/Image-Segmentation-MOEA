package model;

import model.functions.ImageLoader;
import model.segmentGeneration.Node;

import java.util.*;

public class Chromosome {
    private int[] gene;

    private int width, height;

    private Random randGenerator = new Random();

    /*
     * Methods
     */

    public Chromosome() {
        this.width = ImageLoader.getWidth();
        this.height = ImageLoader.getHeight();
    }

    public Chromosome(int[] gene) {
        this.gene = gene;

        this.width = ImageLoader.getWidth();
        this.height = ImageLoader.getHeight();
    }

    /*
    Debug only
     */
    public Chromosome(int[] gene, int w, int h) {
        this.gene = gene;

        this.width = w;
        this.height = h;
    }

    public void generateRandomGene() {
        gene = new int[(width*height)];

        int x = 0;
        int y = 0;
        for(int i = 0; i < gene.length; i++) {
            if(x == width){
                x = 0;
                y++;
            }

            setSelectedGene(getRandomAvailableNode(x,y),i); // Gives the gene an random legal value.

            x++;
        }
    }
    public ArrayList<Segment> generatePhenotype() {
        Node[] nodes = new Node[gene.length];
        ArrayList<Node> notPlaced = new ArrayList<>();

        for(int i = 0;i < gene.length; i++) {
            nodes[i] = new Node(i);
            notPlaced.add(nodes[i]);
        }

        Node.generatePositions(nodes);

        for(int i = 0;i < gene.length;i++) {
            nodes[i].setChild(nodes[gene[i]]);
        }

        ArrayList<Segment> segments = new ArrayList<>();

        while (notPlaced.size() > 0) {
            Node toRemove = notPlaced.remove(0);

            HashSet<Node> list = generateSegment(toRemove, new HashSet<>());

            notPlaced.removeAll(list);

            segments.add(new Segment(new ArrayList<>(list)));
        }

        return segments;
    }

    private HashSet<Node> generateSegment(Node node, HashSet<Node> segment) {
        if(segment.contains(node)) {
            return segment;
        }
        segment.add(node);

        generateSegment(node.getChild(), segment);
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

    /*
     *  Below this point: Evolutionary algorithm methods
     *
     */
    public void mutateFlip(double mutationRate){
        int x = 0;
        int y = 0;
        for(int i = 0; i < gene.length; i++) {
            if(x == width){
                x = 0;
                y++;
            }

            if (Math.random() <= mutationRate) {
                setSelectedGene(getRandomAvailableNode(x,y),i);
            }
            x++;
        }
    }


    public Chromosome[] uniformCrossover(Chromosome mother) {
        if(mother.gene.length != gene.length){
            System.out.println("ERROR");
            return null;
        }

        Chromosome[] offsprings = new Chromosome[]{new Chromosome(new int[gene.length],width,height), new Chromosome(new int[gene.length],width,height)};

        for(int i = 0; i < gene.length; i++) {
            if(Math.random() < 0.5) {
                offsprings[0].gene[i]= gene[i];
                offsprings[1].gene[i]= mother.gene[i];
            }
            else {
                offsprings[0].gene[i]= mother.gene[i];
                offsprings[1].gene[i]= gene[i];
            }
        }

        return offsprings;
    }

    public Chromosome[] singlePointCrossover(Chromosome mother) {
        if(mother.gene.length != gene.length){
            System.out.println("ERROR");
            return null;
        }

        int crossoverPoint = (int) (Math.random()*(gene.length - 2));
        Chromosome[] offsprings = new Chromosome[]{new Chromosome(new int[gene.length]), new Chromosome(new int[gene.length])};


        for(int i = 0; i < gene.length; i++) {
            if(i <= crossoverPoint) {
                offsprings[0].gene[i]= gene[i];
                offsprings[1].gene[i]= mother.gene[i];
            }
            else {
                offsprings[0].gene[i]= mother.gene[i];
                offsprings[1].gene[i]= gene[i];
            }
        }

        return offsprings;
    }

    /*
     *  Private methods for intern logic.
     *
     */

    /**
        Available nodes to link explained:
        Method returns an int where the int represents a neighboring Node.
        Given a node N (where 0 represents N):

                            3
                       2    N   1
                            4

     Edges of the image (frame): A node that will be outside the image will not be returned.
     */
    private int getRandomAvailableNode(int x, int y){
        int select = 0;
        if(x == 0){
            if(y == 0){
                int rnd = randGenerator.nextInt(3); // 0,1 and 4 available.
                if(rnd == 2){
                    rnd = 4;
                }
                select = rnd;

            }else if(y == height -1){
                int rnd = randGenerator.nextInt(3); // 0,1 and 3 available.
                if(rnd == 2){
                    rnd = 3;
                }
                select = rnd;

            }else{
                int rnd = randGenerator.nextInt(4); // 0,1,3 and 4 available.
                if(rnd == 2){
                    rnd = 4;
                }
                select = rnd;
            }

        } else if(x == width -1){
            if(y == 0){
                int rnd = randGenerator.nextInt(3); // 0,2 and 4 available.
                if(rnd == 1){
                    rnd = 4;
                }
                select = rnd;

            }else if(y == height -1){
                int rnd = randGenerator.nextInt(3); // 0,2 and 3 available.
                if(rnd == 1){
                    rnd = 3;
                }
                select = rnd;

            }else{
                int rnd = randGenerator.nextInt(4); // 0,2,3 and 4 available.
                if(rnd == 1){
                    rnd = 4;
                }
                select = rnd;
            }
        }else{
            if(y == 0){
                int rnd = randGenerator.nextInt(4); // 0,1,2 and 4 available.
                if(rnd == 3){
                    rnd = 4;
                }
                select = rnd;

            }else if(y == height -1){
                select = randGenerator.nextInt(4); // 0,1,2 and 3 available.

            }else {
                select = randGenerator.nextInt(5);// 0,1,2,3 and 4 available.
            }
        }

        return select;
    }

    private void setSelectedGene(int select, int index){
        switch (select) {
            case 0: // links to itself.
                gene[index] = index;
                break;
            case 1:
                gene[index] = index + 1; // links to the next node (X dir.)
                break;
            case 2:
                gene[index] = index - 1; // links to the previous node (X dir.)
                break;
            case 3:
                gene[index] = index - width; // links to the upper node (Y dir.)
                break;
            case 4:
                gene[index] = index + width; // links to the bottom node (Y dir.)
                break;
        }
    }
}
