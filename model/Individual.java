package model;

import controller.MOEA;
import model.supportNodes.*;
import model.utils.FitnessCalc;
import model.utils.ImageLoader;
import model.utils.MutableShort;


import java.util.*;

public class Individual {
    private short[][] chromosone;
    private int nrSegments;

    private int rank;
    private double fitnessDeviation;
    private double fitnessConnectivity;
    private double crowdingDistance;

    public int n; // Number of dominating elements.
    public ArrayList<Individual> S = new ArrayList<>();
    Random r = new Random();

    public Individual() {

    }


    /*
     * Methods
     */
    // Minimum Spanning Tree (MST)
    public void generateIndividual(double threshold,FitnessCalc f,int MAX, int MIN) {
        chromosone = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];
        // List of all pixels in the image
        ArrayList<Pixel> pixelsNodes = new ArrayList<>(ImageLoader.getHeight() * ImageLoader.getWidth());

        for (Pixel[] pixels : MOEA.getPixels()) {
            for (Pixel pixel : pixels) {
                pixelsNodes.add(pixel);
            }
        }
        Collections.shuffle(pixelsNodes);

        short segmentId = 1;
        for (Pixel root : pixelsNodes) {
            if (chromosone[root.getY()][root.getX()] == 0) {
                chromosone[root.getY()][root.getX()] = segmentId;
                PriorityQueue<Neighbor> pQueue = new PriorityQueue<>();
                for (Neighbor n : root.getNeighbors()) {
                    pQueue.add(n);
                }
                while (true) {
                    Neighbor newNode = pQueue.poll();
                    if (chromosone[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] == 0) {
                        if (newNode.getDistance() < threshold) {
                            chromosone[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] = segmentId;
                            for (Neighbor n : newNode.getNeighbor().getNeighbors()) {
                                pQueue.add(n);
                            }
                        } else {
                            break;
                        }
                    }
                    if (pQueue.size() == 0) {
                        break;
                    }
                }
                segmentId++;
                if(segmentId == Short.MAX_VALUE ){
                    break;
                }
            }
        }

        nrSegments = repair(chromosone);
        if(nrSegments < MIN)
            return;

        int segments = (int)((double)(MAX-MIN)*Math.random()+MIN);
        while (cleanMergeSmallFirst(segments,f)){ }
        removeSmallSegments(0.1);
        System.out.println("Segment done! "+nrSegments);

    }

    private boolean cleanMergeSmallFirst(int MAX,FitnessCalc f) {
        HashMap<Integer, SegmentNode> avgColor = f.generateAverageColor(this);
        nrSegments = avgColor.size();
        if(avgColor.size() <= MAX){
            return false;
        }

        HashMap<Integer,MutableShort> idTable = new HashMap<>();
        ArrayList<SegmentNode> listOfSegments = new ArrayList<SegmentNode>(idTable.size());

        listOfSegments.addAll(avgColor.values());
        listOfSegments.sort(Comparator.comparing(a -> a.getNrPixels()));
        double threshold = listOfSegments.get(listOfSegments.size()-MAX).getNrPixels();
        for(int i = 0; i < listOfSegments.size()-MAX; i++ ){
            SegmentNode root = listOfSegments.get(i);

            double bestFit = Double.MAX_VALUE;
            Integer bestNode = -1;

            for(int neig : root.getNeighbors()){
                SegmentNode neighbor = avgColor.get(neig);
                if(neighbor.getNrPixels() < threshold){
                    double diff = Math.sqrt(Math.pow(root.getAvgRed() - neighbor.getAvgRed(), 2) + Math.pow(root.getAvgGreen() - neighbor.getAvgGreen(), 2) + Math.pow(root.getAvgBlue() - neighbor.getAvgBlue(), 2));
                    if(diff < bestFit){
                        bestFit = diff;
                        bestNode = (int) neighbor.getId();
                    }
                }
            }
            if(bestNode == -1){
                for(int neig : root.getNeighbors()) {
                    SegmentNode neighbor = avgColor.get(neig);
                    double diff = Math.sqrt(Math.pow(root.getAvgRed() - neighbor.getAvgRed(), 2) + Math.pow(root.getAvgGreen() - neighbor.getAvgGreen(), 2) + Math.pow(root.getAvgBlue() - neighbor.getAvgBlue(), 2));
                    if (diff < bestFit) {
                        bestFit = diff;
                        bestNode = (int) neighbor.getId();
                    }
                }
            }
            if(bestNode == -1){
                System.out.println("oops?");
            }else{
                if(idTable.containsKey(bestNode)){
                    if(idTable.containsKey((int)root.getId())){
                        idTable.get((int)root.getId()).setValue(idTable.get(bestNode).getValue());
                    }else{
                        idTable.put((int)root.getId(), idTable.get(bestNode));
                    }
                }else{
                    if(idTable.containsKey((int)root.getId())){
                        idTable.put(bestNode, idTable.get((int)root.getId()));
                    }else {
                        MutableShort m = new MutableShort(root.getId());

                        idTable.put((int)root.getId(), m);
                        idTable.put(bestNode, m);
                    }
                }
            }
        }

        for (int y = 0; y < ImageLoader.getHeight(); y++) {
            for (int x = 0; x < ImageLoader.getWidth(); x++) {
                int id = chromosone[y][x];
                if(idTable.containsKey(id)){
                    chromosone[y][x] = idTable.get(id).getValue();
                }
            }
        }

        return true;
    }

    public void removeSmallSegments(double thresholdPrecent){
        HashMap<Integer,Integer> pixelsInSegment = getSegmentSizes();

        long avg = 0;
        for(Integer value : pixelsInSegment.values()){
                avg += value;
        }
        avg /= pixelsInSegment.size();
        if(avg < -1){
            System.out.println("ERROR avg.");
        }

        ArrayList<Integer> toMerge = new ArrayList<>();
        for(Integer key : pixelsInSegment.keySet()){
            if(pixelsInSegment.get(key) < (int)(avg*thresholdPrecent)){
                toMerge.add(key);
            }
        }
        if(toMerge.size() != 0) {
            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    int id = chromosone[y][x];
                    if (toMerge.contains(id)) {
                        chromosone[y][x] = 0;
                    }
                }
            }
            nrSegments = repair(chromosone);
        }else{
            nrSegments = pixelsInSegment.size();
        }
    }

    public boolean dominates(Individual x) {
        // Check if the Solutions have the same fitness value
        if (!(fitnessDeviation == x.getFitnessDeviation() && fitnessConnectivity == x.getFitnessConnectivity())) {
            // Check if this Individual dominates a Individual x
            if (fitnessDeviation <= x.getFitnessDeviation() && fitnessConnectivity <= x.getFitnessConnectivity()) {
                return true;
            }
        }

        return false;
    }

    public void mutateMerge(double mutateProb, FitnessCalc f,int PREFEGMENTS){

        HashMap<Integer, SegmentNode> avgColor = f.generateAverageColor(this);

        HashMap<Integer,MutableShort> idTable = new HashMap<>();
        ArrayList<SegmentNode> listOfSegments = new ArrayList<SegmentNode>(idTable.size());

        listOfSegments.addAll(avgColor.values());
        listOfSegments.sort(Comparator.comparing(a -> a.getNrPixels()));

        double threshold = listOfSegments.get(listOfSegments.size()-PREFEGMENTS).getNrPixels();
        int extraRemoved = 0;
        if(PREFEGMENTS+1 < listOfSegments.size()){
            if(threshold > listOfSegments.get(listOfSegments.size()-PREFEGMENTS-1).getNrPixels()*0.8){
                extraRemoved++;
                threshold = listOfSegments.get(listOfSegments.size()-PREFEGMENTS-1).getNrPixels();
                if(PREFEGMENTS+2 < listOfSegments.size()){
                    if(threshold > listOfSegments.get(listOfSegments.size()-PREFEGMENTS-2).getNrPixels()*0.8) {
                        extraRemoved++;
                        threshold = listOfSegments.get(listOfSegments.size()-PREFEGMENTS-2).getNrPixels();
                    }
                }
            }
        }
        int MAXDeletes = 3;
        int deleted = 0;
        for(int i = 0; i < listOfSegments.size()-PREFEGMENTS-extraRemoved; i++ ){
            if(deleted < MAXDeletes && mutateProb < Math.random()){
                deleted++;
                nrSegments--;

                SegmentNode root = listOfSegments.get(i);
                double bestFit = Double.MAX_VALUE;
                Integer bestNode = -1;

                for(int neig : root.getNeighbors()){
                    SegmentNode neighbor = avgColor.get(neig);
                    if(neighbor.getNrPixels() < threshold){
                        double diff = Math.sqrt(Math.pow(root.getAvgRed() - neighbor.getAvgRed(), 2) + Math.pow(root.getAvgGreen() - neighbor.getAvgGreen(), 2) + Math.pow(root.getAvgBlue() - neighbor.getAvgBlue(), 2));
                        if(diff < bestFit){
                            bestFit = diff;
                            bestNode = (int) neighbor.getId();
                        }
                    }
                }
                if(bestNode == -1){
                    for(int neig : root.getNeighbors()) {
                        SegmentNode neighbor = avgColor.get(neig);
                        double diff = Math.sqrt(Math.pow(root.getAvgRed() - neighbor.getAvgRed(), 2) + Math.pow(root.getAvgGreen() - neighbor.getAvgGreen(), 2) + Math.pow(root.getAvgBlue() - neighbor.getAvgBlue(), 2));
                        if (diff < bestFit) {
                            bestFit = diff;
                            bestNode = (int) neighbor.getId();
                        }
                    }
                }
                if(bestNode == -1){
                    System.out.println("oops?");
                }else{
                    if(idTable.containsKey(bestNode)){
                        if(idTable.containsKey((int)root.getId())){
                            idTable.get((int)root.getId()).setValue(idTable.get(bestNode).getValue());
                        }else{
                            idTable.put((int)root.getId(), idTable.get(bestNode));
                        }
                    }else{
                        if(idTable.containsKey((int)root.getId())){
                            idTable.put(bestNode, idTable.get((int)root.getId()));
                        }else {
                            MutableShort m = new MutableShort(root.getId());

                            idTable.put((int)root.getId(), m);
                            idTable.put(bestNode, m);
                        }
                    }
                }
            }
        }

        for (int y = 0; y < ImageLoader.getHeight(); y++) {
            for (int x = 0; x < ImageLoader.getWidth(); x++) {
                int id = chromosone[y][x];
                if(idTable.containsKey(id)){
                    chromosone[y][x] = idTable.get(id).getValue();
                }
            }
        }


    }

    public void mutateSplit(double mutateProb, FitnessCalc f){
        if(mutateProb > Math.random()){
            HashMap<Integer, Double> integerDoubleHashMap = f.generateAverageDeviation(this);
            double maxDev = -1;
            int segmentId = 0;
            for(Integer key :integerDoubleHashMap.keySet()){
                if(integerDoubleHashMap.get(key) > maxDev){
                    maxDev = integerDoubleHashMap.get(key);
                    segmentId = key;
                }
            }
            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    int id = chromosone[y][x];
                    if(id == segmentId){
                        chromosone[y][x] = 0;
                    }
                }
            }

        }else{
            return;
        }

        nrSegments = repairSplit(chromosone);
    }

    public Individual[] crossoverSize(Individual mother, FitnessCalc f, int MAX) {
        Pixel[][] pixels = MOEA.getPixels();
        short[][] mChrom = mother.getChromosone();

        Individual smallPri = new Individual();
        Individual bigPri = new Individual();
        short[][] sChrom = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];
        //short[][] bChrom = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];


        //Sort mother and father on segments size.
        HashMap<Integer, SegmentNodeWhitPos> avgColorFather = f.generateAverageColorWPos(this);
        HashMap<Integer, SegmentNodeWhitPos> avgColorMother = f.generateAverageColorWPos(mother);


        ArrayList<SegmentNodeWhitPos> listOfSegments = new ArrayList<>(avgColorFather.size()+avgColorMother.size());

        listOfSegments.addAll(avgColorFather.values());
        listOfSegments.forEach(a-> a.setF(true));   // Sets that all current nodes are from father.

        listOfSegments.addAll(avgColorMother.values());

        listOfSegments.sort(Comparator.comparing(SegmentNode::getNrPixels)); // Sort on size.

        for(int i = 0; i < listOfSegments.size(); i++){
            listOfSegments.get(i).setRank(i+1);
        }

        int[][] explored = new int[ImageLoader.getHeight()][ImageLoader.getWidth()];
        // Small first.
        short segmentId = 1;
        for(SegmentNodeWhitPos node : listOfSegments){

            short currId = node.getId();
            Pixel currPixel =  pixels[node.getY()][node.getX()];
            short[][] currBoard;
            if(node.isF()){
                currBoard = chromosone;
            }else{
                currBoard = mChrom;
            }


            LinkedList<Pixel> cantPlaceQueue = new LinkedList<>();
            LinkedList<Pixel> placeQueue = new LinkedList<>();

            explored[currPixel.getY()][currPixel.getX()] = node.getRank();

            if(sChrom[currPixel.getY()][currPixel.getX()] == 0){
                placeQueue.add(currPixel);
            }else{
                cantPlaceQueue.add(currPixel);
            }

            while (!cantPlaceQueue.isEmpty() || !placeQueue.isEmpty()){ // Steps: 1: Mark as explored. 2: AddNeighbors. 2.a: only if not explored.
                boolean wasPlaced = false;
                while (!placeQueue.isEmpty()){
                    currPixel = placeQueue.poll();

                    for(Neighbor n : currPixel.getNeighbors()){
                        if(explored[n.getNeighbor().getY()][n.getNeighbor().getX()] != node.getRank()){
                            if(currBoard[n.getNeighbor().getY()][n.getNeighbor().getX()] == currId){
                                if(sChrom[n.getNeighbor().getY()][n.getNeighbor().getX()] == 0){
                                    placeQueue.add(n.getNeighbor());
                                }else{
                                    cantPlaceQueue.add(n.getNeighbor());
                                }
                                explored[n.getNeighbor().getY()][n.getNeighbor().getX()] = node.getRank();
                            }
                        }
                    }
                    if(sChrom[currPixel.getY()][currPixel.getX()] == 0){
                        sChrom[currPixel.getY()][currPixel.getX()] = segmentId;
                        wasPlaced = true;
                    }
                }
                if (wasPlaced) {
                    segmentId++;
                }

                if(!cantPlaceQueue.isEmpty()){
                    currPixel = cantPlaceQueue.poll();
                    for(Neighbor n : currPixel.getNeighbors()){
                        if(explored[n.getNeighbor().getY()][n.getNeighbor().getX()] != node.getRank()){
                            if(currBoard[n.getNeighbor().getY()][n.getNeighbor().getX()] == currId){
                                if(sChrom[n.getNeighbor().getY()][n.getNeighbor().getX()] == 0){
                                    placeQueue.add(n.getNeighbor());
                                }else{
                                    cantPlaceQueue.add(n.getNeighbor());
                                }
                                explored[n.getNeighbor().getY()][n.getNeighbor().getX()] = node.getRank();
                            }
                        }
                    }
                }
            }

        }

        smallPri.setChromosone(sChrom);
        for (int y = 0; y < ImageLoader.getHeight(); y++) {
            for (int x = 0; x < ImageLoader.getWidth(); x++) {
                int id = sChrom[y][x];
                if (id == 0) {
                    System.out.println("Was 0");
                    sChrom[y][x] = segmentId;
                    Pixel currPixel = pixels[y][x];
                    LinkedList<Pixel> placeQueue = new LinkedList<>();
                    for(Neighbor n : currPixel.getNeighbors()){
                        if(sChrom[n.getNeighbor().getY()][n.getNeighbor().getX()] == 0){
                            sChrom[n.getNeighbor().getY()][n.getNeighbor().getX()] = segmentId;
                            placeQueue.add(n.getNeighbor());
                        }
                    }
                    while (!placeQueue.isEmpty()){
                        currPixel = placeQueue.pop();

                        for(Neighbor n : currPixel.getNeighbors()){
                            if(sChrom[n.getNeighbor().getY()][n.getNeighbor().getX()] == 0){
                                sChrom[n.getNeighbor().getY()][n.getNeighbor().getX()] = segmentId;
                                placeQueue.add(n.getNeighbor());
                            }
                        }
                    }
                    segmentId++;
                    if(sChrom[y][x] == 0)
                        System.out.println("whoops");
                }
            }
        }

        HashMap<Integer,Integer> nrOfSegments = smallPri.getSegmentSizes();
        long avg = 0;
        byte nrOfSmall = 0;
        for(Integer value : nrOfSegments.values()){
            avg += value;
        }
        avg /= nrOfSegments.size();
        if(avg < -1){
            System.out.println("ERROR avg.");
        }
        for(Integer value : nrOfSegments.values()){
            if(value < avg *0.05){
                nrOfSmall += (byte)1;
            }
        }
        if(nrOfSegments.size() - nrOfSmall > MAX){
            while(smallPri.cleanMergeSmallFirst(MAX,f));

        }else{
            while (smallPri.cleanMergeSmallFirst(nrOfSegments.size()-nrOfSmall,f));

        }

        //bigPri.setChromosone(bChrom);
        //return new Individual[]{smallPri, bigPri};
        return new Individual[]{smallPri};
    }

    public HashMap<Integer,Integer> getSegmentSizes() {
        HashMap<Integer,Integer> pixelsInSegment = new HashMap<>();

        for (int y = 0; y < ImageLoader.getHeight(); y++) {
            for (int x = 0; x < ImageLoader.getWidth(); x++) {
                int segmentId = chromosone[y][x];
                if(segmentId != 0){
                    Integer pixels = pixelsInSegment.getOrDefault(segmentId,null);
                    if(pixels == null){
                        pixelsInSegment.put(segmentId,1);
                    }else{
                        pixelsInSegment.replace(segmentId,pixels+1);
                    }
                }
            }
        }
        return pixelsInSegment;
    }

    private int repair(short[][]  shadow){
        Pixel[][] pixels = MOEA.getPixels();
        PriorityQueue<Neighbor> pQueue = new PriorityQueue<>();
        ArrayList<Integer> segments = new ArrayList<>();

        for (int y = 0; y < ImageLoader.getHeight(); y++) {
            for (int x = 0; x < ImageLoader.getWidth(); x++) {
                if(shadow[y][x] != 0){
                    for(Neighbor p :pixels[y][x].getNeighbors()){
                        if(shadow[p.getNeighbor().getY()][p.getNeighbor().getX()] == 0){
                            pQueue.add(p);
                        }
                    }
                    if(!segments.contains((int)shadow[y][x])){
                        segments.add((int)shadow[y][x]);
                    }
                }
            }
        }

        while (pQueue.size() != 0){
            Neighbor newNode = pQueue.poll();
            if (shadow[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] == 0) {
                shadow[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] = shadow[newNode.getPixel().getY()][newNode.getPixel().getX()];
                pQueue.addAll(newNode.getNeighbor().getNeighbors());
            }
        }
        return segments.size();
    }

    private int repairSplit(short[][]  shadow){
        ArrayList<Pixel> pixelsNodes = new ArrayList<>(ImageLoader.getHeight() * ImageLoader.getWidth());
        int toAdd = 2;
        int added = 0;

        int[] pixelsInSegment = new int[nrSegments+toAdd + 1];
        for (Pixel[] pixels : MOEA.getPixels()) {
            for (Pixel pixel : pixels) {
                pixelsNodes.add(pixel);
            }
        }
        Collections.shuffle(pixelsNodes);
        PriorityQueue<Neighbor> pQueue = new PriorityQueue<>();

        for(int i = 0; i < pixelsNodes.size(); i++){
            if(added >= toAdd){
                break;
            }
            Pixel root = pixelsNodes.get(i);
            //roots[i] = root;
            if(shadow[root.getY()][root.getX()] == 0){
                shadow[root.getY()][root.getX()] = (short) (nrSegments+added+1);
                pixelsInSegment[(nrSegments+added+1)] = 1;
                pQueue.addAll(root.getNeighbors());
                added++;
            }
        }

        ArrayList<Integer> segments = new ArrayList<>();
        while (pQueue.size() != 0){
            Neighbor newNode = pQueue.poll();
            if (chromosone[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] == 0) {
                short segment = chromosone[newNode.getPixel().getY()][newNode.getPixel().getX()];
                chromosone[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] = segment;
                pQueue.addAll(newNode.getNeighbor().getNeighbors());
                pixelsInSegment[segment] += 1;
            }
        }

        int max = -1;
        for(int value : pixelsInSegment){
            if(value > max){
                max = value;
            }
        }
        ArrayList<Integer> toMerge = new ArrayList<>();
        for(int i = 1; i < pixelsInSegment.length; i++){
            if(pixelsInSegment[i] != 0 && pixelsInSegment[i] < (int)(max*0.2)){
                toMerge.add(i);
            }
        }
        if(toMerge.size() != 0) {
            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    int id = chromosone[y][x];
                    if (toMerge.contains(id)) {
                        chromosone[y][x] = 0;
                    }
                }
            }
        }
        return repair(chromosone);
    }

    /*
     * Compares
     */
    public int compareDeviationTo(Individual other) {
        double cmp = this.fitnessDeviation - other.fitnessDeviation;
        if(cmp > 0){
            return 1;
        } else if(cmp == 0){
            return 0;
        }
        return -1;
    }

    public int compareConnectivityTo(Individual other) {
        double cmp = this.fitnessConnectivity - other.fitnessConnectivity;
        if(cmp > 0){
            return 1;
        } else if(cmp == 0){
            return 0;
        }
        return -1;
    }

    public int compareCrowdTo(Individual other) {
        double cmp = other.crowdingDistance - this.crowdingDistance;
        if(cmp > 0){
            return 1;
        } else if(cmp == 0){
            return 0;
        }
        return -1;
    }

    /*
     * Getters and Setters
     */
    public double getFitnessConnectivity() {
        return fitnessConnectivity;
    }

    public void setFitnessConnectivity(double fitnessConnectivity) {
        this.fitnessConnectivity = fitnessConnectivity;
    }

    public double getFitnessDeviation() {
        return fitnessDeviation;
    }

    public void setFitnessDeviation(double fitnessDeviation) {
        this.fitnessDeviation = fitnessDeviation;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public ArrayList<Individual> getS() {
        return S;
    }

    public void setS(ArrayList<Individual> s) {
        S = s;
    }

    public double getCrowdingDistance() {
        return crowdingDistance;
    }

    public void setCrowdingDistance(double crowdingDistance) {
        this.crowdingDistance = crowdingDistance;
    }

    public void addToCrowdingDistance(double crowdingDistance) {
        if(this.crowdingDistance != Double.MAX_VALUE)
            this.crowdingDistance += crowdingDistance;
        else{
            //System.out.println("ERROR? crowding");
            this.crowdingDistance = -1;
        }
    }

    public short[][] getChromosone() {
        return chromosone;
    }

    public void setChromosone(short[][] chromosone) {
        this.chromosone = chromosone;
    }

    public int getNrSegments() {
        return nrSegments;
    }

}
