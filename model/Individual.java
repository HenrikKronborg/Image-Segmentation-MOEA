package model;

import controller.MOEA;
import model.supportNodes.Neighbor;
import model.supportNodes.Pixel;
import model.supportNodes.Position;
import model.utils.ImageLoader;

import java.util.*;

public class Individual {
    private Chromosome chromosome;
    private HashMap<Integer,Segment> segments;
    private int[][] shadow;
    private int rank;
    private double fitnessDeviation;
    private double fitnessConnectivity;
    private double fitnessEdge;
    private double crowdingDistance;

    public int n; // Number of dominating elements.
    public ArrayList<Individual> S = new ArrayList<>();
    Random r = new Random();

    public Individual(boolean MST) {
        if(MST) initalize();
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
        shadow = new int[ImageLoader.getHeight()][ImageLoader.getWidth()];
        // List of all pixels in the image
        ArrayList<Pixel> pixelsNodes = new ArrayList<>(ImageLoader.getHeight()*ImageLoader.getWidth());
        int unAssigned = ImageLoader.getHeight()*ImageLoader.getWidth();

        for(Pixel[] pixels : MOEA.getPixels()) {
            for(Pixel pixel : pixels){
                pixelsNodes.add(pixel);
            }
        }
        Collections.shuffle(pixelsNodes);
        segments = new HashMap<>();

        int segmentId = 1;
        for(Pixel root: pixelsNodes){
            if(shadow[root.getY()][root.getX()] == 0){
                Segment segment = new Segment();
                segment.setId(segmentId);
                segment.addTo(root);
                shadow[root.getY()][root.getX()] = segmentId;

                PriorityQueue<Neighbor> pQueue = new PriorityQueue<>();
                for(Neighbor n : root.getNeighbors()){
                    pQueue.add(n);
                }
                while (true){
                    Neighbor newNode = pQueue.poll();
                    if(shadow[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] == 0){
                        if(newNode.getDistance() < threshold){
                            segment.addTo(newNode.getNeighbor());
                            shadow[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] = segmentId;
                            for(Neighbor n : newNode.getNeighbor().getNeighbors()){
                                pQueue.add(n);
                            }
                        }else{
                            break;
                        }
                    }
                    if(pQueue.size() == 0){
                        break;
                    }
                }
                segments.put(segmentId,segment);
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

    public Individual[] crossoverAndMutate(Individual mother, double mutateRate){
        Individual[] children = crossover(mother);

        /*
        for(int i = 0; i< children.length; i++){

            // Perform Mutate on children.
            children[i].mutateFlip(mutateRate);

            // Add to return array.
            individuals[i]= new Individual(children[i]);
        }
        */

        return children;
    }

    public Individual[] crossover(Individual mother){

        int crossoverPointX = (int) (Math.random()*ImageLoader.getWidth());
        int crossoverPointY = (int) (Math.random()*ImageLoader.getHeight());

        boolean change = true;
        Individual[] children = {new Individual(false),new Individual(false)};

        for(Individual child : children){
            HashMap<Integer,Segment> newSegments = new HashMap<>();
            int[][] newShadow = new int[ImageLoader.getHeight()][ImageLoader.getWidth()];
            int segmentId = 1;

            for(int y = 0; y < ImageLoader.getHeight(); y++){
                for(int x = 0; x< ImageLoader.getWidth(); x++){
                    if(y == crossoverPointY && x == crossoverPointX){
                        change = !change;
                    }

                    if(newShadow[y][x] == 0){
                        int currentId;
                        Segment s;
                        if(change) {
                            currentId = mother.getShadow()[y][x];
                            s = mother.segments.get(currentId);
                        }else{
                            currentId = shadow[y][x];
                            s = segments.get(currentId);
                        }
                        Segment newSegment = new Segment();
                        for(Position p : s.getPixels()){
                            if(newShadow[p.getY()][p.getX()] == 0){
                                newSegment.addTo(p);
                                newShadow[p.getY()][p.getX()] = segmentId;
                            }
                            newSegments.put(segmentId,newSegment);
                            segmentId++;
                        }

                    }
                }
            }
            child.setSegments(newSegments);
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
    public Chromosome getChromosome() {
        return chromosome;
    }

    public void setChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }

    public ArrayList<Segment> getSegments() {
        return new ArrayList<>(segments.values());
    }

    public void setSegments(HashMap<Integer,Segment> segments) {
        this.segments = segments;
    }

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

    public int[][] getShadow() {
        return shadow;
    }

    public void setShadow(int[][] shadow) {
        this.shadow = shadow;
    }
}
