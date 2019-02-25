package model;

import controller.MOEA;
import model.supportNodes.Neighbor;
import model.supportNodes.Pixel;
import model.supportNodes.Position;
import model.utils.ImageLoader;

import java.util.*;

public class Individual {
    private short[][] shadow;
    private int rank;
    private double fitnessDeviation;
    private double fitnessConnectivity;
    private double fitnessEdge;
    private double crowdingDistance;

    public int n; // Number of dominating elements.
    public ArrayList<Individual> S = new ArrayList<>();
    Random r = new Random();

    public Individual(boolean MST) {
        if (MST) initalize();
    }


    public void initalize() {
        generateIndividual(10);

        /*
        chromosome = new Chromosome();
        chromosome.generateRandomGene();
        segments = chromosome.generatePhenotype();
        */
    }

    /*
     * Methods
     */
    // Minimum Spanning Tree (MST)
    public void generateIndividual(double threshold) {
        shadow = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];
        // List of all pixels in the image
        ArrayList<Pixel> pixelsNodes = new ArrayList<>(ImageLoader.getHeight() * ImageLoader.getWidth());
        int unAssigned = ImageLoader.getHeight() * ImageLoader.getWidth();

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

    public Individual[] crossoverAndMutate(Individual mother, double mutateRate) {
        /*Individual[] children = crossover(mother);


        for(int i = 0; i< children.length; i++){

            // Perform Mutate on children.
            children[i].mutateFlip(mutateRate);

            // Add to return array.
            individuals[i]= new Individual(children[i]);
        }
        */

        return null;
    }

    public Individual[] crossover(Individual mother) {

        int crossoverPointX = (int) (Math.random() * ImageLoader.getWidth());
        int crossoverPointY = (int) (Math.random() * ImageLoader.getHeight());

        Individual[] children = {new Individual(false), new Individual(false)};

        boolean childe2 = true;
        for (Individual child : children) {
            boolean change = false;
            short[][] newShadow = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];

            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    if (y == crossoverPointY && x == crossoverPointX) {
                        change = true;
                    }
                    short currentId;
                    if (change) {
                        currentId = mother.getShadow()[y][x];
                    } else {
                        currentId = shadow[y][x];
                    }

                    if (newShadow[y][x] == 0) {
                        newShadow[y][x] = currentId;
                    }
                }


            }
            child.setShadow(newShadow);
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
}
