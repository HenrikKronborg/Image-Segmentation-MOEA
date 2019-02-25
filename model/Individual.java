package model;

import controller.MOEA;
import model.supportNodes.Neighbor;
import model.supportNodes.Pixel;
import model.utils.ImageLoader;

import java.util.*;

public class Individual {
    private short[][] shadow;
    private int rank;
    private double fitnessDeviation;
    private double fitnessConnectivity;
    private double fitnessEdge;
    private double crowdingDistance;
    private int segments;

    public int n; // Number of dominating elements.
    public ArrayList<Individual> S = new ArrayList<>();
    Random r = new Random();

    public Individual(double threshold) {
        initialize(threshold);
    }
    public Individual() {
    }

    public void initialize(double threshold) {
        generateIndividual(threshold);
    }

    /*
     * Methods
     */
    // Minimum Spanning Tree (MST)
    public void generateIndividual(double threshold) {
        shadow = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];
        // List of all pixels in the image
        ArrayList<Pixel> pixelsNodes = new ArrayList<>(ImageLoader.getHeight() * ImageLoader.getWidth());

        for (Pixel[] pixels : MOEA.getPixels()) {
            for (Pixel pixel : pixels) {
                pixelsNodes.add(pixel);
            }
        }
        Collections.shuffle(pixelsNodes);

        short segmentId = 1;
        for (Pixel root : pixelsNodes) {
            if (shadow[root.getY()][root.getX()] == 0) {
                shadow[root.getY()][root.getX()] = segmentId;

                PriorityQueue<Neighbor> pQueue = new PriorityQueue<>();
                for (Neighbor n : root.getNeighbors()) {
                    pQueue.add(n);
                }
                while (true) {
                    Neighbor newNode = pQueue.poll();
                    if (shadow[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] == 0) {
                        if (newNode.getDistance() < threshold) {
                            shadow[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] = segmentId;
                            for (Neighbor n : newNode.getNeighbor().getNeighbors()) {
                                pQueue.add(n);
                            }
                        } else {
                            break;
                        }
                    }
                    if (pQueue.size() == 0) {
                        break;
                    }
                }
                segmentId++;
            }
        }
        segments = segmentId-1;

    }

    public boolean dominates(Individual x) {
        // Check if the Solutions have the same fitness value
        if (!(fitnessDeviation == x.getFitnessDeviation() && fitnessConnectivity == x.getFitnessConnectivity())) {
            // Check if this Individual dominates a Individual x
            if (fitnessDeviation <= x.getFitnessDeviation() && fitnessConnectivity <= x.getFitnessConnectivity()) {
                return true;
            }
        }

        return false;
    }

    public void mutate(double mutateProb){

        for (int y = 0; y < ImageLoader.getHeight(); y++) {
            for (int x = 0; x < ImageLoader.getWidth(); x++) {
                if(Math.random() < mutateProb){
                    int dir = getRandomAvailableNode(x,y);

                    switch (dir) {
                        case 0: // links to itself.
                            break;
                        case 1:
                            shadow[y][x] = shadow[y][x+1]; // links to the next node (X dir.)
                            break;
                        case 2:
                            shadow[y][x] = shadow[y][x-1]; // links to the previous node (X dir.)
                            break;
                        case 3:
                            shadow[y][x]= shadow[y-1][x]; // links to the upper node (Y dir.)
                            break;
                        case 4:
                            shadow[y][x] = shadow[y+1][x]; // links to the bottom node (Y dir.)
                            break;
                    }
                }
            }
        }
    }
    public Individual[] crossover(Individual mother) {

        int crossoverPointX = (int) (Math.random() * ImageLoader.getWidth());
        int crossoverPointY = (int) (Math.random() * ImageLoader.getHeight());

        Individual[] children = {new Individual(), new Individual()};

        for (int i = 0; i < children.length;i++) {
            boolean change = false;
            short[][] newShadow = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];
            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    if (y == crossoverPointY && x == crossoverPointX) {
                        change = true;
                    }
                    short currentId;
                    if (i == 1) {
                        currentId = (short)(mother.getShadow()[y][x]);
                    } else {
                        currentId = shadow[y][x];
                    }
                    newShadow[y][x] = currentId;

                }
            }
            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    if (y == crossoverPointY && x == crossoverPointX) {
                        change = true;
                    }
                    short currentId;
                    if (i == 0) {
                        currentId = (short)(mother.getShadow()[y][x]);
                    } else {
                        currentId = shadow[y][x];
                    }

                    if (newShadow[y][x] == 0) {
                        newShadow[y][x] = currentId;
                    }
                }
            }
            children[i].setShadow(newShadow);
        }

        return children;
    }


        /*
     * Compares
     */
    public int compareDeviationTo(Individual other) {
        double cmp = this.fitnessDeviation - other.fitnessDeviation;
        if(cmp > 0){
            return 1;
        } else if(cmp == 0){
            return 0;
        }
        return -1;
    }

    public int compareConnectivityTo(Individual other) {
        double cmp = this.fitnessConnectivity - other.fitnessConnectivity;
        if(cmp > 0){
            return 1;
        } else if(cmp == 0){
            return 0;
        }
        return -1;
    }

    public int compareEdgeTo(Individual other) {
        double cmp = this.fitnessEdge - other.fitnessEdge;
        if(cmp > 0){
            return 1;
        } else if(cmp == 0){
            return 0;
        }
        return -1;
    }
    public int compareCrowdTo(Individual other) {
        double cmp = other.crowdingDistance - this.crowdingDistance;
        if(cmp > 0){
            return 1;
        } else if(cmp == 0){
            return 0;
        }
        return -1;
    }

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
        if(x == 0) {
            if(y == 0) {
                int rnd = r.nextInt(3); // 0,1 and 4 available.
                if(rnd == 2) {
                    rnd = 4;
                }
                select = rnd;

            } else if(y == shadow.length -1) {
                int rnd = r.nextInt(3); // 0,1 and 3 available.
                if(rnd == 2) {
                    rnd = 3;
                }
                select = rnd;

            } else {
                int rnd = r.nextInt(4); // 0,1,3 and 4 available.
                if(rnd == 2) {
                    rnd = 4;
                }
                select = rnd;
            }
        } else if(x == shadow[0].length -1) {
            if(y == 0) {
                int rnd = r.nextInt(3); // 0,2 and 4 available.
                if(rnd == 1) {
                    rnd = 4;
                }
                select = rnd;

            } else if(y == shadow.length -1) {
                int rnd = r.nextInt(3); // 0,2 and 3 available.
                if(rnd == 1){
                    rnd = 3;
                }
                select = rnd;

            } else {
                int rnd = r.nextInt(4); // 0,2,3 and 4 available.
                if(rnd == 1) {
                    rnd = 4;
                }
                select = rnd;
            }
        } else {
            if(y == 0) {
                int rnd = r.nextInt(4); // 0,1,2 and 4 available.
                if(rnd == 3) {
                    rnd = 4;
                }
                select = rnd;

            } else if(y == shadow.length -1) {
                select = r.nextInt(4); // 0,1,2 and 3 available.

            } else {
                select = r.nextInt(5);// 0,1,2,3 and 4 available.
            }
        }

        return select;
    }

    /*
     * Getters and Setters
     */

    public double getFitnessEdge() {
        return fitnessEdge;
    }

    public void setFitnessEdge(double fitnessEdge) {
        this.fitnessEdge = fitnessEdge;
    }

    public double getFitnessConnectivity() {
        return fitnessConnectivity;
    }

    public void setFitnessConnectivity(double fitnessConnectivity) {
        this.fitnessConnectivity = fitnessConnectivity;
    }

    public double getFitnessDeviation() {
        return fitnessDeviation;
    }

    public void setFitnessDeviation(double fitnessDeviation) {
        this.fitnessDeviation = fitnessDeviation;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public ArrayList<Individual> getS() {
        return S;
    }

    public void setS(ArrayList<Individual> s) {
        S = s;
    }

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    public void addToCrowdingDistance(double crowdingDistance) {
        if(this.crowdingDistance != Double.MAX_VALUE)
            this.crowdingDistance += crowdingDistance;
        else
            System.out.println("ERROR?");
    }

    public short[][] getShadow() {
        return shadow;
    }

    public void setShadow(short[][] shadow) {
        this.shadow = shadow;
    }

    public int getSegments() {
        return segments;
    }
}
