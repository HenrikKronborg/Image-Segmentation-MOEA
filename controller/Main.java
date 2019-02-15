package controller;

import model.Chromosome;
import model.Segment;
import model.functions.FitnessCalc;
import model.functions.ImageLoader;

import java.awt.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String... args) {
        ImageLoader image = new ImageLoader();
        image.loadImage("test.jpg");

        Color[][] pixels = image.getPixels();

        /*
        for(Color[] i : pixels) {
            for(Color j : i) {
                System.out.println(j);
            }
            System.out.println();
        }

        System.out.println("\nTest Chrom:");
        int[] gene = new int[]{1,5,1,3,0,4,2,3,9,10,14,7,8,12,15,15};

        Chromosome c = new Chromosome(gene);

        List<Segment> segments = c.generatePhenotype();
        System.out.println(segments.size());
        */
        ArrayList<Segment> list = new ArrayList<>();
        Segment s = new Segment(image.getPixels());
        list.add(s);

        FitnessCalc f = new FitnessCalc();
        f.setImageLoader(image);
        System.out.println(Arrays.toString(f.generateFitness(list)));

    }
}
