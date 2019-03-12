package controller;

import model.supportNodes.ThreadNode;
import model.Individual;
import model.supportNodes.Pixel;
import model.supportNodes.Position;
import model.utils.FitnessCalc;
import model.utils.ImageLoader;
import model.utils.Validators;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class MOEA {
    private static int popSize = 40; // Population size
    private static int numOffsprings = popSize; // Number of offsprings
    private static double mutationRate = 0.20; // Mutation rate
    private static int maxRuns = 5; // Maximum number of runs before termination
    private static int tournamentSize = 2; // Number of individuals to choose from population at random

    private ThreadNode ob;
    private static ArrayList<Individual> population;
    private static LinkedList<Individual> front;
    private int generation;
    private final int MINSEGMENTS = 3;
    private final int MAXSEGMENTS = 12;
    private final int PREFEGMENTS = 5;
    private FitnessCalc fitness;

    private ImageLoader image;
    private static Pixel[][] pixels = new Pixel[ImageLoader.getHeight()][ImageLoader.getWidth()];

    private final int N = 4;
    private Thread[] threads = new Thread[N];
    private CountDownLatch doneSignal = new CountDownLatch(N);

    public MOEA(ImageLoader loader) {
        this.image = loader;
        generatePixels();
    }

    private void initialPopulationThreads(String msg) {
        for(int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    int threshold = 5;
                    int added = 0;
                    int counter = 0;
                    System.out.println("thread started");
                    while(added < popSize/threads.length) {

                        Individual indv = new Individual();
                        indv.generateIndividual(threshold + 5 * Math.random(), fitness, MAXSEGMENTS, MINSEGMENTS);

                        if (indv.getNrSegments() >= MINSEGMENTS && indv.getNrSegments() <= MAXSEGMENTS){
                            population.add(indv);
                            added++;
                        }

                        if(indv.getNrSegments() < MINSEGMENTS) {
                            threshold = 5;
                        }else{
                            threshold += 3;
                        }

                        counter++;
                        if(counter > popSize*1.5){
                            System.out.println("Mayor problem in init pop");
                            break;
                        }
                    }
                    doneSignal.countDown();
                }
            });
            threads[i].start();
        }
    }

    private void crossoverThreads(String msg) {
        for(int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    int prod =0;
                    while (prod < numOffsprings/threads.length) {
                        Individual father = NSGAIItournament();
                        Individual mother = NSGAIItournament();
                        while (father.equals(mother)){
                            mother = NSGAIItournament();
                        }

                        for(Individual child : father.crossoverSize(mother,fitness,MAXSEGMENTS)) {
                            if(child != null){
                               if(child.getNrSegments() > PREFEGMENTS){
                                    child.mutateMerge(mutationRate,fitness,PREFEGMENTS);
                                }else if(child.getNrSegments() < PREFEGMENTS){
                                    //child.mutateSplit(mutationRate,fitness);
                                } else{
                                    if(Math.random() >= 0.5){
                                        //child.mutateSplit(mutationRate,fitness);
                                    }else{
                                        child.mutateMerge(mutationRate,fitness,PREFEGMENTS);
                                    }
                                }
                                if(child.getNrSegments() >= MINSEGMENTS && child.getNrSegments() <= MAXSEGMENTS) {
                                    population.add(child);
                                    prod++;
                                    System.out.println("Created child");
                                }else{
                                    System.out.println(child.getNrSegments());
                                    System.out.println("not: size");
                               }
                            }else{
                                System.out.println("not: null");
                            }
                        }
                    }
                    doneSignal.countDown();
                }

            });

            threads[i].start();
        }
    }

    public void run2() {
        fitness = new FitnessCalc();
        fitness.setImageLoader(image);
        population = new ArrayList<>();
        initialPopulationThreads("Thread crash initial population");
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Initialize population done. " + population.size() + " random solutions found");

        for(Individual individual : population) {
            fitness.generateFitness(individual);
        }

        LinkedList<LinkedList<Individual>> frontiers = fastNonDominatedSort();
        for(LinkedList<Individual> l : frontiers) {
            crowdingDistance(l);
        }

        doneSignal = new CountDownLatch(N);
        crossoverThreads("Thread crash crossover");
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LinkedList<Individual> children = new LinkedList<>();
        for(int i = popSize; i < population.size(); i++){
            children.add(population.get(i));
        }
        ob.setOb(children);
        ob.setGeneration(generation);
        ob.changed.set(true);
    }

    public void run() {
        fitness = new FitnessCalc();
        fitness.setImageLoader(image);

        population = new ArrayList<>();
        initialPopulationThreads("Thread crash initial population");
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Initialize population done. " + population.size() + " random solutions found");

        // Calculate fitness value
        for(Individual individual : population) {
            fitness.generateFitness(individual);
        }

        LinkedList<LinkedList<Individual>> frontiers = fastNonDominatedSort();
        for(LinkedList<Individual> l : frontiers) {
            crowdingDistance(l);
        }

        while(generation++ < maxRuns) {
            doneSignal = new CountDownLatch(N);
            crossoverThreads("Thread crash crossover");
            try {
                doneSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Sort and calculate crowding distance
            for(int i = popSize; i < population.size(); i++) {
                fitness.generateFitness(population.get(i));
            }

            frontiers = fastNonDominatedSort();
            for(LinkedList<Individual> l : frontiers) {
                crowdingDistance(l);
                for(Individual indv: l){
                    if(indv.getCrowdingDistance() == -1){
                        l.remove(l);
                    }

                }
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

            front = frontiers.get(0);
            ob.setOb(front);
            ob.setGeneration(generation);
            ob.changed.set(true);
       }
    }


    /*
     * Methods
     */
    private void generatePixels() {
        for(int y = 0; y < ImageLoader.getHeight(); y++) {
            for(int x = 0; x < ImageLoader.getWidth(); x++) {
                Pixel pixel = new Pixel(x, y, image.getPixelValue(new Position(x, y)));
                pixels[y][x] = pixel;
            }
        }
        findNeighbors();
    }

    private void joinThreads(String msg){
        for(int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.out.println(msg);
            }
        }
    }

    private void findNeighbors() {
        for(int y = 0; y < ImageLoader.getHeight(); y++) {
            for(int x = 0; x < ImageLoader.getWidth(); x++) {
                Pixel pixel = pixels[y][x];

                // Right
                if(y + 1 < ImageLoader.getHeight()) {
                    pixel.addNeighbor(pixels[y+1][x]);
                }

                // Left
                if(y - 1 >= 0) {
                    pixel.addNeighbor(pixels[y-1][x]);
                }

                // Bottom
                if(x + 1 < ImageLoader.getWidth()) {
                    pixel.addNeighbor(pixels[y][x+1]);
                }

                // Top
                if(x - 1 >= 0) {
                    pixel.addNeighbor(pixels[y][x-1]);
                }
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
    public static int getMaxRuns() { return maxRuns; }
    public void setMaxRuns(int maxRuns) { this.maxRuns = maxRuns; }
    public static int getTournamentSize() { return tournamentSize; }
    public void setTournamentSize(int tournamentSize) { this.tournamentSize = tournamentSize; }

    public void loadObservableList(ThreadNode ob) { this.ob = ob; }

    public static ArrayList<Individual> getPopulation() { return population; }
    public static LinkedList<Individual> getFront() { return front; }
    //public void loadObservableList(ArrayList<LinkedList<Individual>> ob) { this.ob = ob; }
    public int getGeneration() { return generation; }

    public static Pixel[][] getPixels() { return pixels; }
}
