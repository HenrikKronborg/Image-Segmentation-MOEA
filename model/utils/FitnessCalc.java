package model.utils;

import model.supportNodes.Position;
import model.Segment;
import model.Individual;

import javafx.scene.paint.Color;
import java.util.ArrayList;

public class FitnessCalc {
    private ImageLoader img;

    public FitnessCalc() {

    }

    /*
     * Methods
     */
    public double[] generateFitness(Individual individual) {
        double deviation = deviation(individual.getSegments());
        double connectivity = connectivity(individual.getSegments());

        individual.setFitnessDeviation(deviation);
        individual.setFitnessConnectivity(connectivity);

        return new double[]{deviation,connectivity};
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
        red   /= color.length;
        green /= color.length;
        blue  /= color.length;

        double deviation = 0.0;
        for(Color c : color){
            deviation += Math.sqrt(Math.pow(c.getRed()-red,2)+Math.pow(c.getGreen()-green,2)+Math.pow(c.getBlue()-blue,2));
        }

        return deviation;
    }

    private double connectivity(ArrayList<Segment> segments) {
        double conn = 0.0;
        for (Segment segment : segments) {
            conn += segmentConnectivity(segment);
        }

        return conn;
    }

    private double segmentConnectivity(Segment segment) {
        ArrayList<Position> pixels = segment.getPixels();
        double conn = 0.0;
        for(int i = 0; i < pixels.size(); i++) {
            Position pix = pixels.get(i);

            if(pix.getX() != ImageLoader.getWidth()-1) {
                if(!segment.contains(pix.getX()+1,pix.getY())) {
                    conn += 1;
                }

                if(pix.getY() != 0) {
                    if(!segment.contains(pix.getX()+1,pix.getY()-1)) {
                        conn += 0.2;
                    }
                }

                if(pix.getY() != ImageLoader.getHeight()-1) {
                    if(!segment.contains(pix.getX()+1,pix.getY()+1)) {
                        conn += 0.166;
                    }
                }
            }

            if(pix.getX() != 0) {
                if(!segment.contains(pix.getX()-1,pix.getY())) {
                    conn += 0.5;
                }
                if(pix.getY() != 0) {
                    if(!segment.contains(pix.getX()-1,pix.getY()-1)) {
                        conn += 0.143;
                    }
                }

                if(pix.getY() != ImageLoader.getHeight()-1) {
                    if(!segment.contains(pix.getX()-1,pix.getY()+1)) {
                        conn += 0.125;
                    }
                }
            }

            if(pix.getY() != 0) {
                if(!segment.contains(pix.getX(),pix.getY()-1)) {
                    conn += 0.333;
                }
            }

            if(pix.getY() != ImageLoader.getHeight()-1) {
                if(!segment.contains(pix.getX(),pix.getY()+1)) {
                    conn += 0.25;
                }
            }
        }
        return conn;
    }

    public void setImageLoader(ImageLoader img) {
        this.img = img;
    }
}
