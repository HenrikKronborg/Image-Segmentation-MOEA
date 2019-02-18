package controller;

import model.Chromosome;
import model.Segment;
import model.functions.FitnessCalc;
import model.functions.ImageLoader;
import model.functions.Validators;

import java.awt.Color;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String... args) {
        ImageLoader image = new ImageLoader();
        image.loadImage("test4x4.jpg");

        MOEA algorithm = new MOEA();
        algorithm.run(image);

        /*

        System.out.println("\nTest Chrom:");
        //int[] gene = new int[]{1,5,1,3,0,4,2,3,9,10,14,7,8,12,15,15};

        Chromosome c = new Chromosome();
        c.generateRandomGene();
        ArrayList<Segment> segments = c.generatePhenotype();
        System.out.println("test:" + segments.size());

        FitnessCalc f = new FitnessCalc();
        f.setImageLoader(image);
        System.out.println(Arrays.toString(f.generateFitness(segments)));

        System.out.println("t");

        */
    }
}
