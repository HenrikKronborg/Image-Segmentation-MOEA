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
        //LinkedList<LinkedList<Solution>> linkedLists = generateRank();
        //System.out.println(Validators.validateRank(linkedLists));
        //printRank(linkedLists);

        /*
        // Test dominates
        for(int i = 0; i < 2; i++) {
            System.out.println("d:" + population.get(i).getFitnessDeviation() + " || c: " + population.get(i).getFitnessConnectivity());
            if(i == 0)
                System.out.println(population.get(i).dominates(population.get(i+1)));
        }
        */

        for(LinkedList<Solution> list : fastNonDominatedSort()) {
            for(Solution solution : list) {
                System.out.println(solution.getFitnessDeviation());
            }
        }
    }

    /*
     * Methods
     */
    public LinkedList<LinkedList<Solution>> fastNonDominatedSort() {
        LinkedList<LinkedList<Solution>> frontier = new LinkedList<>();

        ArrayList<ArrayList<Solution>> sp = new ArrayList<>();
        sp.add(new ArrayList<>());

        int index = 0;
        for(Solution p : population) {
            //ArrayList<Solution> sp = new ArrayList<>(); // Set of solutions that the solution p dominates
            int np = 0; // Domination count

            for(Solution q : population) {
                if(p.dominates(q)) {
                    //sp.add(q);
                    sp.get(index).add(q);
                }
                else if(q.dominates(p)) {
                    np++;
                }
            }

            // If p belongs to the first front
            if(np == 0) {
                int pRank = 1;
                frontier.push(new LinkedList<>(Arrays.asList(p)));
            }
            index++;
        }

        int i = 0;
        while(!frontier.get(i).isEmpty()) {
            LinkedList<Solution> Q = new LinkedList<>(); // Store members of the next

            for(Solution p : frontier.get(i)) {
                // ?
                int nq = sp.get(i).size();

                for(Solution q : sp.get(i)) {
                    nq--;

                    if(nq == 0) {
                        int qRank = i++;
                        Q.push(q);
                    }
                }
            }

            i++;
            frontier.push(Q);
        }

        return frontier;
    }

    /*
    public LinkedList<LinkedList<Solution>> generateRank() {
        LinkedList<LinkedList<Solution>> ranks = new LinkedList<>();

        // Need a Solution to compare against
        ranks.push(new LinkedList<>(Arrays.asList(population.get(0))));

        printRank(ranks);
        for(int i = 1; i < population.size(); i++) {
            Solution solution = population.get(i);
            boolean isPlaced = false;

            for(int rank = 0; rank < ranks.size(); rank++) {
                boolean sameRank = true;
                boolean dominates = false;

                for(Solution rankedSolution : ranks.get(rank)) {
                    if (!(solution.getFitnessDeviation() == rankedSolution.getFitnessDeviation() && solution.getFitnessConnectivity() == rankedSolution.getFitnessConnectivity())) {
                        // Check if solution is dominated
                        if(solution.getFitnessDeviation() >= rankedSolution.getFitnessDeviation() && solution.getFitnessConnectivity() >= rankedSolution.getFitnessConnectivity()) {
                                sameRank = false;
                                break;
                        }
                        // Check if solution dominates
                        else if(solution.getFitnessDeviation() <= rankedSolution.getFitnessDeviation() && solution.getFitnessConnectivity() <= rankedSolution.getFitnessConnectivity()) {
                            sameRank = false;
                            dominates = true;
                            break;
                        }
                    }
                }

                if(dominates) {
                    ranks.add(rank, new LinkedList<>(Arrays.asList(solution)));
                    isPlaced = true;

                    for(int j = 0; j < ranks.get(rank+1).size(); j++) {
                        Solution rankedSolution = ranks.get(rank+1).get(j);

                        if(solution.getFitnessDeviation() > rankedSolution.getFitnessDeviation() || solution.getFitnessConnectivity() > rankedSolution.getFitnessConnectivity()
                                || solution.getFitnessDeviation() == rankedSolution.getFitnessDeviation() && solution.getFitnessConnectivity() == rankedSolution.getFitnessConnectivity()) {
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
            System.out.print("\nNew Iteration:");
            printRank(ranks);
        }

        return ranks;
    }
    */

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
