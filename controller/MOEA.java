package controller;

import model.Solution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class MOEA {
    private static int popSize = 100; // Population size
    private static int numOffsprings = popSize; // Number of offsprings
    private static double mutationRate = 0.08; // Mutation rate
    private static double recombProbability = 0.7; // Used only for Generational. recombProbability of doing crossover, and 1-recombProbability of copying a parent
    private static int maxRuns = 100; // Maximum number of runs before termination
    private static int tournamentSize = 2; // Number of individuals to choose from population at random

    private static ArrayList<Solution> population;
    private static Solution bestSolution;

    /*
     * Methods
     */
    public void generateRank() {
        LinkedList<LinkedList<Solution>> ranks = new LinkedList<>();

        // Need a Solution to compare against
        ranks.push(new LinkedList<>(Arrays.asList(population.get(0))));

        for(int i = 1; i < population.size(); i++) {
            Solution solution = population.get(i);
            boolean isPlaced = false;

            for(int rank = 0; rank < ranks.size(); rank++) {
                boolean sameRank = true;
                boolean dominates = false;

                for(Solution rankedSolution : ranks.get(rank)) {
                    // Check if solution is dominated
                    if(solution.getFitnessDeviation() <= rankedSolution.getFitnessDeviation() && solution.getFitnessConnectivity() <= rankedSolution.getFitnessConnectivity()) {
                        sameRank = false;
                        break;
                    }
                    // Check if solution dominates
                    else if(solution.getFitnessDeviation() > rankedSolution.getFitnessDeviation() && solution.getFitnessConnectivity() > rankedSolution.getFitnessConnectivity()) {
                        dominates = true;
                        sameRank = false;
                    }
                }

                if(dominates){
                    ranks.add(rank, new LinkedList<>(Arrays.asList(solution)));
                    isPlaced = true;

                    for(int j = 0; j < ranks.get(rank+1).size(); j++) {
                        Solution rankedSolution = ranks.get(rank+1).get(j);

                        if(solution.getFitnessDeviation() <= rankedSolution.getFitnessDeviation() || solution.getFitnessConnectivity() <= rankedSolution.getFitnessConnectivity()) {
                            ranks.get(rank).add(rankedSolution);
                            ranks.get(rank+1).remove(j);
                            j--;
                        }
                    }

                    break;
                }
                else if(sameRank) {
                    ranks.get(rank).push(solution);
                    isPlaced = true;
                    break;
                }
            }

            if(!isPlaced) {
                ranks.addLast(new LinkedList<Solution>(Arrays.asList(solution)));
            }
        }
    }

    /*
     * Getters and Setters
     */
    public static int getPopSize() { return popSize; }
    public void setPopSize(int popSize) { this.popSize = popSize; }
    public static int getNumOffsprings() { return numOffsprings; }
    public void setNumOffsprings(int numOffsprings) { this.numOffsprings = numOffsprings; }
    public static double getMutationRate() { return mutationRate; }
    public void setMutationRate(double mutationRate) { this.mutationRate = mutationRate; }
    public static double getRecombProbability() { return recombProbability; }
    public void setRecombProbability(double recombProbability) { this.recombProbability = recombProbability; }
    public static int getMaxRuns() { return maxRuns; }
    public void setMaxRuns(int maxRuns) { this.maxRuns = maxRuns; }
    public static int getTournamentSize() { return tournamentSize; }
    public void setTournamentSize(int tournamentSize) { this.tournamentSize = tournamentSize; }

    public static ArrayList<Solution> getPopulation() { return population; }
    public static Solution getBestSolution() { return bestSolution; }
}
