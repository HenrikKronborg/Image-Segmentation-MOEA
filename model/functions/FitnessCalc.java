package model.functions;

import model.Position;
import model.Segment;

import java.awt.*;
import java.util.ArrayList;

public class FitnessCalc {
    private ImageLoader img;

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
        double overallDev = 0.0;
        for (Segment segment : segments) {
            overallDev += segmentDeviation(segment);
        }

        return overallDev;
    }

    private double segmentDeviation(Segment segment) {
        ArrayList<Position> pixels = segment.getPixels();
        Color[] color = new Color[pixels.size()];
        double red = 0.0;
        double blue = 0.0;
        double green = 0.0;

        for(int i=0; i < color.length; i++){
            Color temp = img.getPixelValue(pixels.get(i));
            color[i] = temp;

            red += temp.getRed();
            blue += temp.getBlue();
            green += temp.getGreen();
        }
        red    /= color.length;
        green /= color.length;
        blue /= color.length;


        double deviation = 0.0;
        for(Color c : color){
            deviation += Math.sqrt(Math.pow(c.getRed()-red,2)+Math.pow(c.getGreen()-green,2)+Math.pow(c.getBlue()-blue,2));
        }


        return deviation;
    }

    private void connectivity() {

    }

    private void edge() {

    }

    public void setImageLoader(ImageLoader img) {
        this.img = img;
    }


}
