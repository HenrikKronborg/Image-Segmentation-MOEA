package controller;

import model.Individual;
import model.Pixel;
import model.utils.FitnessCalc;
import model.utils.ImageLoader;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MOEA {
    private static int popSize = 5; // Population size
    private static int numOffsprings = popSize; // Number of offsprings
    private static double mutationRate = 0.02; // Mutation rate
    private static double recombProbability = 0.8; // Used only for Generational. recombProbability of doing crossover, and 1-recombProbability of copying a parent
    private static int maxRuns = 5; // Maximum number of runs before termination
    private static int tournamentSize = 2; // Number of individuals to choose from population at random

    private static ArrayList<Individual> population;
    private static LinkedList<Individual> front;
    private ArrayList<LinkedList<Individual>> ob;
    private AtomicInteger generation = new AtomicInteger(0);

    private ImageLoader image;
    private Pixel[][] pixels;

    public MOEA(ImageLoader loader) {
        this.image = loader;
    }

    public void run() {
        population = new ArrayList<>();

        while(population.size() < popSize) {
            population.add(new Individual());
        }

        System.out.println("Initialize population done. " + popSize + " random solutions found");

        // Calculate fitness value
        FitnessCalc fitness = new FitnessCalc();
        fitness.setImageLoader(image);
        for(Individual solution : population) {
            fitness.generateFitness(solution);
            
        }

        LinkedList<LinkedList<Individual>> frontiers = fastNonDominatedSort();
        for(LinkedList<Individual> l : frontiers) {
            crowdingDistance(l);
        }

        while(generation.get() < maxRuns) {
            while (population.size() < popSize + numOffsprings) {
                Individual father = NSGAIItournament();
                Individual mother = NSGAIItournament();

                for(Individual child : father.crossoverAndMutate(mother,mutationRate)) {
                    population.add(child);
                }
            }

            // Sort and calculate crowding distance
            frontiers = fastNonDominatedSort();
            for(LinkedList<Individual> l : frontiers) {
                crowdingDistance(l);
            }

            ArrayList<Individual> tempPopulation = new ArrayList<>(popSize);
            for(LinkedList<Individual> l : frontiers) {
                if(tempPopulation.size() >= popSize) {
                    break;
                }
                if(l.size()+tempPopulation.size() <= popSize) {
                    tempPopulation.addAll(l);
                }else{
                    l.sort((Individual a, Individual b)-> a.compareCrowdTo(b));
                    for(Individual s : l) {
                        if(tempPopulation.size() <= popSize) {
                            tempPopulation.add(s);
                        } else {
                            break;
                        }
                    }
                }
            }

            population = tempPopulation;

            System.out.println(generation);

            front = frontiers.get(0);
            ob.add(front);

            generation.getAndIncrement();
            //If memory becomes a problem...
            /*
            population.sort((Individual a, Individual b)-> a.getRank()-b.getRank());// Sort on rank
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
    private void findNeighbors() {
        for(int x = 0; x < ImageLoader.getWidth(); x++) {
            for(int y = 0; y < ImageLoader.getHeight(); y++) {

            }
        }
    }

    public LinkedList<LinkedList<Individual>> fastNonDominatedSort() {
        LinkedList<LinkedList<Individual>> frontier = new LinkedList<>();

        for(Individual p : population) {
            p.S = new ArrayList<>();
            p.n = 0;

            for(Individual q : population) {
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
            LinkedList<Individual> Q = new LinkedList<>(); // Store members of the next
            for(Individual p : frontier.get(i)) {
                for(Individual q : p.S) {
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

    public void crowdingDistance(LinkedList<Individual> I) {
        I.sort((Individual a, Individual b)-> a.compareDeviationTo(b));
        Individual first = I.getFirst();
        Individual last = I.getLast();

        // If we Use 3D, be careful with MAX_Value, does not act as 
        first.setCrowdingDistance(Double.MAX_VALUE);
        last.setCrowdingDistance(Double.MAX_VALUE);

        for(int i =1; i < I.size()-1;i++){
            I.get(i).setCrowdingDistance((I.get(i+1).getFitnessDeviation()-I.get(i-1).getFitnessDeviation())/(last.getFitnessDeviation()-first.getFitnessDeviation()));
        }


        I.sort((Individual a, Individual b)-> a.compareConnectivityTo(b));
        first = I.getFirst();
        last = I.getLast();

        first.setCrowdingDistance(Double.MAX_VALUE);
        last.setCrowdingDistance(Double.MAX_VALUE);

        for(int i =1; i < I.size()-1;i++){
            I.get(i).addToCrowdingDistance((I.get(i+1).getFitnessConnectivity()-I.get(i-1).getFitnessConnectivity())/(last.getFitnessConnectivity()-first.getFitnessConnectivity()));
        }
    }

    public Individual NSGAIItournament() {
        Individual first, second;

        int randomIndex = (int) (Math.random()*popSize);
        first = population.get(randomIndex);
        while(true) {
            randomIndex = (int) (Math.random()*popSize);
            second = population.get(randomIndex);

            if(!second.equals(first)) {
                break;
            }
        }

        if(first.getRank() < second.getRank()) {
            return first;
        } else if(first.getRank() > second.getRank()) {
            return second;
        }

        if(first.getCrowdingDistance() > second.getCrowdingDistance()) {
            return first;
        }

        return second;
    }

    public void printRank(LinkedList<LinkedList<Individual>> rankedPopulation) {
        int rankInt = 0;
        for(List<Individual> rank : rankedPopulation) {
            rankInt++;
            System.out.println("\nRank: "+rankInt);
            for (Individual ind : rank) {
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

    public static ArrayList<Individual> getPopulation() { return population; }
    public static LinkedList<Individual> getFront() { return front; }
    public void loadObservableList(ArrayList<LinkedList<Individual>> ob) { this.ob = ob; }
    public AtomicInteger getGeneration() { return generation; }
}
