package controller;

import model.Solution;

import java.util.ArrayList;

public class GeneticAlgorithm {
    private static int popSize = 100; // Population size
    private static int numOffsprings = 10; // Number of offsprings
    private static boolean survival = true; // true=Elitism and false=Generational
    private static double mutationRate = 0.08; // Mutation rate
    private static double recombProbability = 0.7; // Used only for Generational. recombProbability of doing crossover, and 1-recombProbability of copying a parent
    private static int maxRuns = 100; // Maximum number of runs before termination
    private static int tournamentSize = 20; // Number of individuals to choose from population at random

    private static ArrayList<Solution> population;
    private static Solution bestSolution;


}
