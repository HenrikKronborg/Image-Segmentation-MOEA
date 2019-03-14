package controller;

import model.Individual;
import model.supportNodes.Pixel;
import model.supportNodes.Position;
import model.supportNodes.ThreadNode;
import model.utils.FitnessCalc;
import model.utils.ImageLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class GeneticAlgorithm {
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


    private final int N = 4;
    private Thread[] threads = new Thread[N];
    private CountDownLatch doneSignal = new CountDownLatch(N);

    public GeneticAlgorithm(ImageLoader loader) {
        MOEA temp = new MOEA(loader); // Setting static variables, it is not used for evolution of individuals.
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




}
