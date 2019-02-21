package controller;

import model.Chromosome;
import model.Solution;
import model.functions.FitnessCalc;
import model.functions.ImageLoader;
import model.functions.Validators;

import java.util.*;

public class MOEA {
    private static int popSize = 10; // Population size
    private static int numOffsprings = popSize; // Number of offsprings
    private static double mutationRate = 0.08; // Mutation rate
    private static double recombProbability = 0.7; // Used only for Generational. recombProbability of doing crossover, and 1-recombProbability of copying a parent
    private static int maxRuns = 5; // Maximum number of runs before termination
    private static int tournamentSize = 2; // Number of individuals to choose from population at random

    private static ArrayList<Solution> population;
    private static LinkedList<Solution> front;
    private ArrayList<LinkedList<Solution>> ob;

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

        LinkedList<LinkedList<Solution>> frontiers = fastNonDominatedSort();
        for(LinkedList<Solution> l : frontiers) {
            crowdingDistance(l);
        }

        int generation = 1;
        while(generation++ <= maxRuns){
            while (population.size() < popSize + numOffsprings){
                Solution father = NSGAIItournament();
                Solution mother = NSGAIItournament();

                for(Solution child : father.crossoverAndMutate(mother,mutationRate)) {
                    population.add(child);
                }
            }

            // Sort and calculate crowding distance
            frontiers = fastNonDominatedSort();
            for(LinkedList<Solution> l : frontiers) {
                crowdingDistance(l);
            }

            ArrayList<Solution> tempPopulation = new ArrayList<>(popSize);
            for(LinkedList<Solution> l : frontiers){
                if(tempPopulation.size() >= popSize){
                    break;
                }
                if(l.size()+tempPopulation.size() <= popSize){
                    tempPopulation.addAll(l);
                }else{
                    l.sort((Solution a, Solution b)-> a.compareCrowdTo(b));
                    for(Solution s : l){
                        if(tempPopulation.size() <= popSize){
                            tempPopulation.add(s);
                        }else{
                            break;
                        }
                    }
                }
            }

            population = tempPopulation;

            front = frontiers.get(0);
            ob.add(front);

            //If memory becomes a problem...
            /*
            population.sort((Solution a, Solution b)-> a.getRank()-b.getRank());// Sort on rank
            int lastRank = 0;
            for(int i = 0; i < popSize; i++) {
                lastRank = population.get(i).getRank();
            }
            for(int i = popSize; i < population.size(); i++){
                if()
            }*/
        }
    }

    /*
     * Methods
     */
    public LinkedList<LinkedList<Solution>> fastNonDominatedSort() {
        LinkedList<LinkedList<Solution>> frontier = new LinkedList<>();

        for(Solution p : population) {
            p.S = new ArrayList<>();
            p.n = 0;

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

    public void crowdingDistance(LinkedList<Solution> I) {
        int l = I.size();

        I.sort((Solution a, Solution b)-> a.compareDeviationTo(b));
        Solution first = I.getFirst();
        Solution last = I.getLast();

        // If we Use 3D, be careful with MAX_Value, does not act as 
        first.setCrowdingDistance(Double.MAX_VALUE);
        last.setCrowdingDistance(Double.MAX_VALUE);

        for(int i =1; i < I.size()-1;i++){
            I.get(i).setCrowdingDistance((I.get(i+1).getFitnessDeviation()-I.get(i-1).getFitnessDeviation())/(last.getFitnessDeviation()-first.getFitnessDeviation()));
        }


        I.sort((Solution a, Solution b)-> a.compareConnectivityTo(b));
        first = I.getFirst();
        last = I.getLast();

        first.setCrowdingDistance(Double.MAX_VALUE);
        last.setCrowdingDistance(Double.MAX_VALUE);

        for(int i =1; i < I.size()-1;i++){
            I.get(i).addToCrowdingDistance((I.get(i+1).getFitnessConnectivity()-I.get(i-1).getFitnessConnectivity())/(last.getFitnessConnectivity()-first.getFitnessConnectivity()));
        }
    }

    public Solution NSGAIItournament() {
        Solution first, second;

        int randomIndex = (int) (Math.random()*popSize);
        first = population.get(randomIndex);
        while(true) {
            randomIndex = (int) (Math.random()*popSize);
            second = population.get(randomIndex);

            if(!second.equals(first)) {
                break;
            }
        }

        if(first.getRank() < second.getRank()){
            return first;
        }else if(first.getRank() > second.getRank()){
            return second;
        }

        if(first.getCrowdingDistance() > second.getCrowdingDistance()){
            return first;
        }

        return second;
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
    public static LinkedList<Solution> getFront() { return front; }
    public void loadObservableList(ArrayList<LinkedList<Solution>> ob) { this.ob = ob; }
}
