package controller;

import model.Individual;
import model.supportNodes.ThreadNode;
import model.utils.FitnessCalc;
import model.utils.ImageLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class WeightedSum implements GeneticAlgorithm {

    private ThreadNode ob;
    private static ArrayList<Individual> population;
    private int generation;
    private final int MINSEGMENTS = 3;
    private final int MAXSEGMENTS = 12;
    private final int PREFEGMENTS = 5;
    private FitnessCalc fitness;

    public static final double weightDeviation = 0;
    public static final double weightConnectivity = 1;


    private ImageLoader image;
    private final int N = 4;
    private Thread[] threads = new Thread[N];
    private CountDownLatch doneSignal = new CountDownLatch(N);

    public WeightedSum(ImageLoader loader) {
        image = loader;
        MOEA temp = new MOEA(loader); // Setting static variables, it is not used for evolution of individuals.
    }


    private void initialPopulationThreads() {
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

    private void crossoverThreads() {
        for(int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new Runnable() {
                public void run() {
                    int prod =0;
                    while (prod < numOffsprings/threads.length) {
                        Individual father = tournament();
                        Individual mother = tournament();
                        while (father.equals(mother)){
                            mother = tournament();
                        }

                        for(Individual child : father.crossoverSize(mother,fitness,MAXSEGMENTS)) {
                            if(child != null){
                                if(child.getNrSegments() > PREFEGMENTS){
                                    child.mutateMerge(mutationRate,fitness,PREFEGMENTS);
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


    public void run() {
        fitness = new FitnessCalc();
        fitness.setImageLoader(image);

        population = new ArrayList<>();
        initialPopulationThreads();
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

        // Sort on fitness
        population.sort(Individual::compareSumFitnessTo);

        // Run generations
        while(generation++ < maxRuns) {
            doneSignal = new CountDownLatch(N);
            crossoverThreads();
            try {
                doneSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Sort and calculate crowding distance
            for(int i = popSize; i < population.size(); i++) {
                fitness.generateFitness(population.get(i));
            }

            population.sort(Individual::compareSumFitnessTo);

            ArrayList<Individual> tempPopulation = new ArrayList<>();
            for(Individual ind : population) {
                if(tempPopulation.size() >= popSize) {
                    break;
                }
                tempPopulation.add(ind);
            }

            population = tempPopulation;
            LinkedList<Individual> best = new LinkedList<>();
            best.add(population.get(0));
            ob.setOb(best);
            ob.setGeneration(generation);

            ob.changed.set(true);
        }
    }

    public Individual tournament() {
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

        if(first.compareSumFitnessTo(second) > 0) {
            return first;
        } else {
            return second;
        }

    }

    @Override
    public void loadObservableList(ThreadNode ob) { this.ob = ob; }

}
