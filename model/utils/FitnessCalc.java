package model.utils;

import model.Individual;
import model.supportNodes.SegmentNode;
import model.supportNodes.SegmentNodeWithPos;

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

    public HashMap<Integer,Double> generateAverageDeviation(Individual individual){
        short[][] board = individual.getChromosone();

        HashMap<Integer,int[]> segments = new HashMap<>();
        HashMap<Integer,Double> avgSegmentDeviation = new HashMap<>();

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
                int id = board[y][x];
                int[] c =  segments.get(id);

                if(avgSegmentDeviation.containsKey(id)){
                    Double sum = avgSegmentDeviation.get(id);
                    sum += Math.sqrt(Math.pow(color.getRed() - c[1], 2) + Math.pow(color.getGreen() - c[2], 2) + Math.pow(color.getBlue() - c[3], 2))/1000;
                    avgSegmentDeviation.put(id, sum);
                }else{
                    avgSegmentDeviation.put(id, Math.sqrt(Math.pow(color.getRed() - c[1], 2) + Math.pow(color.getGreen() - c[2], 2) + Math.pow(color.getBlue() - c[3], 2))/1000);
                }
            }
        }
        for (int id : avgSegmentDeviation.keySet()) {
            Double sum = avgSegmentDeviation.get(id);
            sum = sum/segments.get(id)[0];
            avgSegmentDeviation.put(id,sum);
        }
        return avgSegmentDeviation;
    }

    public HashMap<Integer,SegmentNode> generateAverageColor(Individual individual){
        short[][] board = individual.getChromosone();

        HashMap<Integer,SegmentNode> segments = new HashMap<>();
        int lastId = 0;
        for (int y=0; y < board.length; y++) {
            for(int x=0;  x <board[y].length; x++){
                if(x == 0){
                    lastId = board[y][x];
                }

                int id = board[y][x];
                if(id == 0){
                    System.out.println("ERROR fitness");
                }else{
                    Color c = img.getPixelValue(x,y);
                    SegmentNode node;
                    if(segments.containsKey(id)){
                        node = segments.get(id);
                        node.addColor(c);
                    }else{
                        node =  new SegmentNode();
                        node.addColor(c);
                        node.setId((short)id);
                        segments.put(id,node);
                    }
                    if(lastId != id){
                        if(!node.getNeighbors().contains(lastId)){
                            node.getNeighbors().add(lastId);
                            SegmentNode prev = segments.get(lastId);
                            if(!prev.getNeighbors().contains(id)){
                                prev.getNeighbors().add(id);
                            }

                        }
                    }
                    if(y != 0){
                        int topId = board[y-1][x];
                        if(topId != id){
                            if(!node.getNeighbors().contains(topId)){
                                node.getNeighbors().add(topId);
                                SegmentNode prev = segments.get(topId);
                                if(prev == null){
                                    return null;
                                }else{
                                    if(!prev.getNeighbors().contains(id)){
                                        prev.getNeighbors().add(id);
                                    }
                                }
                            }
                        }
                    }
                    lastId = id;
                }
            }
        }
        for (SegmentNode node : segments.values()) {
            node.makeAvg();
        }
        return segments;
    }

    public HashMap<Integer, SegmentNodeWithPos> generateAverageColorWPos(Individual individual){
        short[][] board = individual.getChromosone();

        HashMap<Integer, SegmentNodeWithPos> segments = new HashMap<>();
        int lastId = 0;
        for (int y=0; y < board.length; y++) {
            for(int x=0;  x <board[y].length; x++){
                if(x == 0){
                    lastId = board[y][x];
                }

                int id = board[y][x];
                if(id == 0){
                    System.out.println("ERROR fitness");
                }else{
                    Color c = img.getPixelValue(x,y);
                    SegmentNodeWithPos node;
                    if(segments.containsKey(id)){
                        node = segments.get(id);
                        node.addColor(c);
                    }else{
                        node =  new SegmentNodeWithPos();
                        node.addColor(c);
                        node.setX(x);
                        node.setY(y);
                        node.setId((short)id);
                        segments.put(id,node);
                    }
                    if(lastId != id){
                        if(!node.getNeighbors().contains(lastId)){
                            node.getNeighbors().add(lastId);
                            SegmentNode prev = segments.get(lastId);
                            if(!prev.getNeighbors().contains(id)){
                                prev.getNeighbors().add(id);
                            }

                        }
                    }
                    if(y != 0){
                        int topId = board[y-1][x];
                        if(topId != id){
                            if(!node.getNeighbors().contains(topId)){
                                node.getNeighbors().add(topId);
                                SegmentNode prev = segments.get(topId);
                                if(!prev.getNeighbors().contains(id)){
                                    prev.getNeighbors().add(id);
                                }
                            }
                        }
                    }
                    lastId = id;
                }
            }
        }
        for (SegmentNode node : segments.values()) {
            node.makeAvg();
        }
        return segments;
    }
}
