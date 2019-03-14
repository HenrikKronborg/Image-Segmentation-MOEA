package controller;

import model.supportNodes.ThreadNode;

public interface GeneticAlgorithm {
    int popSize = 16; // Population size
    int numOffsprings = popSize; // Number of offsprings
    double mutationRate = 0.20; // Mutation rate
    int maxRuns = 20; // Maximum number of runs before termination
    int tournamentSize = 2; // Number of individuals to choose from population at random

    void loadObservableList(ThreadNode ob);

    void run();
}
