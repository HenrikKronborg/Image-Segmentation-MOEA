package controller;

import model.Solution;
import model.functions.FitnessCalc;
import model.functions.ImageLoader;
import model.functions.Validators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MOEA {
    private static int popSize = 10; // Population size
    private static int numOffsprings = popSize; // Number of offsprings
    private static double mutationRate = 0.08; // Mutation rate
    private static double recombProbability = 0.7; // Used only for Generational. recombProbability of doing crossover, and 1-recombProbability of copying a parent
    private static int maxRuns = 100; // Maximum number of runs before termination
    private static int tournamentSize = 2; // Number of individuals to choose from population at random

    private static ArrayList<Solution> population;
    private static Solution bestSolution;

    public MOEA() {

    }

    public void run(ImageLoader image) {
        population = new ArrayList<>();

        while(population.size() < popSize) {
            population.add(new Solution());
        }
        System.out.println("Initialize population done. " + popSize + " random solutions found");

        // Calculate fitness value
        FitnessCalc fitness = new FitnessCalc();
        fitness.setImageLoader(image);
        for(Solution solution : population) {
            fitness.generateFitness(solution);
        }
        LinkedList<LinkedList<Solution>> linkedLists = fastNonDominatedSort();
        System.out.println(Validators.validateRank(linkedLists));
        printRank(linkedLists);
    }

    /*
     * Methods
     */
    public LinkedList<LinkedList<Solution>> fastNonDominatedSort() {
        LinkedList<LinkedList<Solution>> frontier = new LinkedList<>();

        for(Solution p : population) {
            for(Solution q : population) {
                if(p.dominates(q)) {
                    p.S.add(q);
                }
                else if(q.dominates(p)) {
                    p.n++;
                }
            }

            // If p belongs to the first front
            if(p.n == 0) {
                p.setRank(1);
                if(frontier.size()== 0) {
                    frontier.push(new LinkedList<>(Arrays.asList(p)));
                } else {
                    frontier.get(0).add(p);
                }
            }
        }

        int i = 0;
        while(frontier.size() > i) {
            LinkedList<Solution> Q = new LinkedList<>(); // Store members of the next
            for(Solution p : frontier.get(i)) {
                for(Solution q : p.S) {
                    q.n--;
                    if(q.n == 0) {
                        q.setRank(i+2);
                        Q.push(q);
                    }
                }
            }

            if(!Q.isEmpty()) {
                frontier.addLast(Q);
            }
            
            i++;
        }

        return frontier;
    }

    public void crowdingDistance() {
        int l = population.size();
        int[] crowdingValues = new int[100];

        for(int i = 0; i < l; i++) {

        }
    }

    public void printRank(LinkedList<LinkedList<Solution>> rankedPopulation){
        int rankInt = 0;
        for(List<Solution> rank : rankedPopulation) {
            rankInt++;
            System.out.println("\nRank: "+rankInt);
            for (Solution ind : rank) {
                System.out.println("<"+ind.getFitnessDeviation()+", "+ind.getFitnessConnectivity()+">");
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
