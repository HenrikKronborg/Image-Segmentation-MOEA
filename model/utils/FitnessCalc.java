package model.utils;

import model.Individual;

import java.awt.*;
import java.util.HashMap;

public class FitnessCalc {
    private ImageLoader img;

    public FitnessCalc() {

    }

    /*
     * Methods
     */
    public void generateFitness(Individual individual) {
        short[][] board = individual.getChromosone();

        double overallDev = 0.0;
        double conn = 0.0;

        HashMap<Integer,int[]> segments = new HashMap<>();

        for (int y=0; y < board.length; y++) {
            for(int x=0;  x <board[y].length; x++){
                int id = board[y][x];
                Color c = img.getPixelValue(x,y);

                if(segments.containsKey(id)){
                    int[] color =  segments.get(id);
                    color[0] += 1;
                    color[1] += c.getRed();
                    color[2] += c.getGreen();
                    color[3] += c.getBlue();
                }else{
                    segments.put(id,new int[]{1,c.getRed(),c.getGreen(),c.getBlue()});
                }

                if(x != ImageLoader.getWidth()-1) {
                    if(id != board[y][x+1])
                        conn += 1;

                    if(y != 0) {
                        if(id != board[y-1][x+1])
                            conn += 0.2;
                    }

                    if(y != ImageLoader.getHeight()-1) {
                        if(id != board[y+1][x+1])
                            conn += 0.166;
                    }
                }

                if(x != 0) {
                    if(id != board[y][x-1])
                        conn += 0.5;
                    if(y != 0) {
                        if(id != board[y-1][x-1])
                            conn += 0.143;
                    }

                    if(y!= ImageLoader.getHeight()-1) {
                        if(id != board[y+1][x-1])
                            conn += 0.125;
                    }
                }

                if(y!= 0) {
                    if(id != board[y-1][x])
                        conn += 0.333;
                }

                if(y != ImageLoader.getHeight()-1) {
                    if(id != board[y+1][x])
                        conn += 0.25;
                }
            }
        }
        for (int[] value : segments.values()) {
            value[1] = value[1]/value[0];
            value[2] = value[2]/value[0];
            value[3] = value[3]/value[0];
        }
        for (int y=0; y<board.length;y++) {
            for(int x=0; x<board[y].length;x++) {
                Color color = img.getPixelValue(x,y);
                int id =board[y][x];
                int[] c =  segments.get(id);
                overallDev += Math.sqrt(Math.pow(color.getRed() - c[1], 2) + Math.pow(color.getGreen() - c[2], 2) + Math.pow(color.getBlue() - c[3], 2))/1000;
            }
        }

        individual.setFitnessDeviation(overallDev);
        individual.setFitnessConnectivity(conn);
    }

    public void setImageLoader(ImageLoader img) {
        this.img = img;
    }
}
