package model;

import java.util.ArrayList;

public class Solution {
    private Chromosome chromosome;
    private ArrayList<Segment> segments;
    private int rank;
    private double fitnessDeviation;
    private double fitnessConnectivity;
    private double fitnessEdge;
    private double crowdingDistance;

    public int n; // Number of dominating elements.
    public ArrayList<Solution> S = new ArrayList<>();

    public Solution() {
        initalize();
    }

    public void initalize() {
        chromosome = new Chromosome();
        chromosome.generateRandomGene();
        segments = chromosome.generatePhenotype();
    }

    /*
     * Methods
     */
    public boolean dominates(Solution x) {
        // Check if the Solutions have the same fitness value
        if (!(fitnessDeviation == x.getFitnessDeviation() && fitnessConnectivity == x.getFitnessConnectivity())) {
            // Check if this Solution dominates a Solution x
            if (fitnessDeviation <= x.getFitnessDeviation() && fitnessConnectivity <= x.getFitnessConnectivity()) {
                return true;
            }
        }

        return false;
    }

    public Position[] findBorders() {
        return null;
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

    public ArrayList<Solution> getS() {
        return S;
    }

    public void setS(ArrayList<Solution> s) {
        S = s;
    }
}
