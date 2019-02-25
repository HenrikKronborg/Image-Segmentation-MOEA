package model;

import controller.MOEA;
import model.supportNodes.Neighbor;
import model.supportNodes.Pixel;
import model.supportNodes.Position;
import model.utils.ImageLoader;

import java.util.*;

public class Individual {
    private Chromosome chromosome;
    private ArrayList<Segment> segments;
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

    public Individual(Chromosome chromosome){
        this.chromosome = chromosome;
        segments = chromosome.generatePhenotype();
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
        segments = new ArrayList<>();
        boolean[][] placed = new boolean[ImageLoader.getHeight()][ImageLoader.getWidth()];


        int segmentId = 0;
        for(Pixel root: pixelsNodes){
            if(!placed[root.getY()][root.getX()]){
                Segment segment = new Segment();
                segment.setId(segmentId);
                segment.addTo(root);
                shadow[root.getY()][root.getX()] = segmentId;

                placed[root.getY()][root.getX()] = true;
                PriorityQueue<Neighbor> pQueue = new PriorityQueue<>();
                for(Neighbor n : root.getNeighbors()){
                    pQueue.add(n);
                }
                while (true){
                    Neighbor newNode = pQueue.poll();
                    if(!placed[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()]){
                        if(newNode.getDistance() < threshold){
                            placed[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] = true;
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
                segmentId++;
                segments.add(segment);
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
        Chromosome[] children = chromosome.uniformCrossover(mother.chromosome);

        Individual[] individuals = new Individual[children.length];

        for(int i = 0; i< children.length; i++){

            // Perform Mutate on children.
            children[i].mutateFlip(mutateRate);

            // Add to return array.
            individuals[i]= new Individual(children[i]);
        }

        return individuals;
    }

    public Individual[] crossover(Individual mother){

        int crossoverPointX = (int) (Math.random()*ImageLoader.getWidth());
        int crossoverPointY = (int) (Math.random()*ImageLoader.getHeight());

        boolean change = true;
        Individual[] children = {new Individual(false),new Individual(false)};

        for(Individual child : children){
            ArrayList<Segment> newSegments = new ArrayList<>();
            boolean[][] placed = new boolean[ImageLoader.getHeight()][ImageLoader.getWidth()];

            for(int y = 0; y < ImageLoader.getHeight(); y++){
                for(int x = 0; x< ImageLoader.getWidth(); x++){
                    if(y == crossoverPointY && x == crossoverPointX){
                        change = !change;
                    }

                    if(!placed[y][x]){
                        int segmentLength;
                        if(change) {
                            segmentLength = mother.segments.size();
                        }else{
                            segmentLength = segments.size();
                        }
                        for(int i= 0; i < segmentLength; i++){
                            Segment s;

                            if(change) {
                                s = mother.segments.get(i);
                            }else{
                                s = segments.get(i);
                            }

                            if(s.contains(x,y)){
                                Segment newSegment = new Segment();
                                for(Position p : s.getPixels()){
                                    if(!placed[p.getY()][p.getX()]){
                                        newSegment.addTo(p);
                                        placed[p.getY()][p.getX()] = true;
                                    }
                                    newSegments.add(newSegment);
                                }
                            }
                        }
                    }
                }
            }
            child.setSegments(newSegments);
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
        return segments;
    }

    public void setSegments(ArrayList<Segment> segments) {
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
