package model.functions;

import model.Segment;

import java.util.ArrayList;

public class FitnessCalc {
    public FitnessCalc() {

    }

    /*
     * Methods
     */
    public double[] generateFitness(ArrayList<Segment> segments) {
        double deviation = deviation(segments);

        return null;
    }

    private double deviation(ArrayList<Segment> segments) {

        //return Math.sqrt(Math.pow(d.getX() - c.getX(), 2) + Math.pow(d.getY() - c.getY(), 2));

        return 0;
    }

    private void connectivity() {

    }

    private void edge() {

    }
}
