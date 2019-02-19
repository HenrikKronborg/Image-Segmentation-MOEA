package model;

import java.util.ArrayList;

public class Solution {
    private Chromosome chromosome;
    private ArrayList<Segment> segments;
    private int rank;
    private double fitnessDeviation;
    private double fitnessConnectivity;
    private double fitnessEdge;

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
    public boolean dominates(Solution q) {
        // Check if this Solution dominates Solution q
        if(fitnessDeviation <= q.getFitnessDeviation() && fitnessConnectivity <= q.getFitnessConnectivity()) {
            return true;
        }

        return false;
        /*
        if (!(fitnessDeviation == q.getFitnessDeviation() && fitnessConnectivity == q.getFitnessConnectivity())) {
            // Check if solution is dominated
            if(fitnessDeviation >= q.getFitnessDeviation() && fitnessConnectivity >= q.getFitnessConnectivity()) {
                return false;
            }
            // Check if solution dominates
            else if(fitnessDeviation <= q.getFitnessDeviation() && fitnessConnectivity <= q.getFitnessConnectivity()) {
                return true;
            }
        }
        */
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
}
