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
    private double fitnessEdge;
    private double crowdingDistance;

    public int n; // Number of dominating elements.
    public ArrayList<Individual> S = new ArrayList<>();
    Random r = new Random();

    public Individual(int segments) {
        generateIndividualSmart(segments);
    }

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
        if(avgColor.size() <= MAX + 1){
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

    public void generateIndividual(int segments) {
        chromosone = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];
        // List of all pixels in the image
        ArrayList<Pixel> pixelsNodes = new ArrayList<>(ImageLoader.getHeight() * ImageLoader.getWidth());

        for (Pixel[] pixels : MOEA.getPixels()) {
            for (Pixel pixel : pixels) {
                pixelsNodes.add(pixel);
            }
        }
        Collections.shuffle(pixelsNodes);
        Pixel[] roots =  new Pixel[segments];
        PriorityQueue<Neighbor> pQueue = new PriorityQueue<>();

        for(int i = 0; i < roots.length; i++){
            Pixel root = pixelsNodes.get(i);
            roots[i] = root;
            chromosone[root.getY()][root.getX()] = (short) (i+1);
            pQueue.addAll(root.getNeighbors());
        }
        while (pQueue.size() != 0){
            Neighbor newNode = pQueue.poll();
            if (chromosone[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] == 0) {
                chromosone[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] = chromosone[newNode.getPixel().getY()][newNode.getPixel().getX()];
                pQueue.addAll(newNode.getNeighbor().getNeighbors());
            }
        }

        nrSegments = segments;

    }

    public void generateIndividualSmart(int segments) {
        int[] pixelsInSegment = new int[segments+1];
        chromosone = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];
        // List of all pixels in the image
        ArrayList<Pixel> pixelsNodes = new ArrayList<>(ImageLoader.getHeight() * ImageLoader.getWidth());

        for (Pixel[] pixels : MOEA.getPixels()) {
            for (Pixel pixel : pixels) {
                pixelsNodes.add(pixel);
            }
        }
        Collections.shuffle(pixelsNodes);
        Pixel[] roots =  new Pixel[segments];
        PriorityQueue<Neighbor> pQueue = new PriorityQueue<>();

        for(int i = 0; i < roots.length; i++){
            Pixel root = pixelsNodes.get(i);
            roots[i] = root;
            chromosone[root.getY()][root.getX()] = (short) (i+1);
            pixelsInSegment[(i+1)] = 1;
            pQueue.addAll(root.getNeighbors());
        }
        while (pQueue.size() != 0){
            Neighbor newNode = pQueue.poll();
            if (chromosone[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] == 0) {
                short segment = chromosone[newNode.getPixel().getY()][newNode.getPixel().getX()];
                chromosone[newNode.getNeighbor().getY()][newNode.getNeighbor().getX()] = segment;
                pQueue.addAll(newNode.getNeighbor().getNeighbors());
                pixelsInSegment[(segment)] += 1;
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
            if(pixelsInSegment[i] < (int)(max*0.05)){
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
            nrSegments = repair(chromosone);
        }else{
            nrSegments = segments;
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

    public void mutateMerge(double mutateProb, FitnessCalc f){
        if(mutateProb > Math.random()){
            HashMap<Integer, SegmentNode> avgColor = f.generateAverageColor(this);// N, R, G, B, Neighbors
            Double smallestDiff = Double.MAX_VALUE;
            int toMerge = 0;
            for(int key : avgColor.keySet()){
                for(int key2 : avgColor.keySet()){
                    if(key != key2){
                        SegmentNode node1 = avgColor.get(key);
                        if(node1.getNeighbors().contains(key2)){
                            SegmentNode node2 = avgColor.get(key2);
                            double diff = Math.sqrt(Math.pow(node1.getAvgRed() - node2.getAvgRed(), 2) + Math.pow(node1.getAvgGreen() - node2.getAvgGreen(), 2) + Math.pow(node1.getAvgBlue() - node2.getAvgBlue(), 2));
                            if(diff < smallestDiff){
                                //Merg the smallest segment in to the other.
                                if(node1.getNrPixels() < node2.getNrPixels())
                                    toMerge = key;
                                else
                                    toMerge = key2;
                                smallestDiff = diff;
                            }
                        }
                    }
                }

            }

            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    int id = chromosone[y][x];
                    if(id == toMerge){
                        chromosone[y][x] = 0;
                    }
                }
            }

            nrSegments = repair(chromosone);
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

    public Individual[] crossover(Individual mother) {

        int crossoverPointX = (int) (Math.random() * ImageLoader.getWidth());
        int crossoverPointY = (int) (Math.random() * ImageLoader.getHeight());

        Individual[] children = {new Individual(), new Individual()};

        for (int i = 0; i < children.length;i++) {
            boolean change = false;
            HashMap<Integer,Integer> fatherTable = new HashMap<>();
            int segmentId = 1;
            short[][] newShadow = new short[ImageLoader.getHeight()][ImageLoader.getWidth()];
            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    if (y == crossoverPointY && x == crossoverPointX) {
                        change = true;
                    }
                    int currentId;
                    boolean toPlace = true;
                    if (i == 0) {
                        currentId = chromosone[y][x];
                    } else {
                        currentId = mother.getChromosone()[y][x];
                    }

                    currentId = translate(fatherTable,change,currentId,segmentId);
                    segmentId = fatherTable.size();

                    if(toPlace){
                        newShadow[y][x] = (short)currentId;
                    }

                }
            }
            change = false;
            HashMap<Integer,Integer> motherTable = new HashMap<>();
            ArrayList<Integer> checkForRepair = new ArrayList<>();

            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    if (newShadow[y][x] == 0) {
                        int currentId;
                        if (i == 0) {
                            currentId = mother.getChromosone()[y][x];
                        } else {
                            currentId = chromosone[y][x];
                        }

                        currentId = translate(motherTable,change,currentId,segmentId);
                        segmentId = fatherTable.size()+motherTable.size();

                        newShadow[y][x] = (short)currentId;
                    }else{
                        if(!checkForRepair.contains((int)mother.getChromosone()[y][x]))
                            checkForRepair.add((int)mother.getChromosone()[y][x]);
                    }

                }
            }

            children[i].nrSegments = fatherTable.size()+motherTable.size();

            for(int j = 0; j <checkForRepair.size();j++){
                Integer temp = motherTable.get(checkForRepair.get(j));
                if(temp != null){
                    checkForRepair.set(j, temp);
                }else{
                    checkForRepair.set(j,0);
                }
            }
            boolean repair = false;
            for (int y = 0; y < ImageLoader.getHeight(); y++) {
                for (int x = 0; x < ImageLoader.getWidth(); x++) {
                    int temp = newShadow[y][x];
                    if(checkForRepair.contains(temp)){
                        newShadow[y][x] = 0;
                        repair = true;
                    }
                }
            }

            if(repair){
                children[i].nrSegments = repair(newShadow); // Corrects and returns nr of segments.
            }
            children[i].setChromosone(newShadow);
        }

        return children;
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
            listOfSegments.get(i).setRank(i);
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
                sChrom[currPixel.getY()][currPixel.getX()] = segmentId;
            }
            for(Neighbor n : currPixel.getNeighbors()){
                if(currBoard[n.getNeighbor().getY()][n.getNeighbor().getX()] == currId){
                    if(sChrom[n.getNeighbor().getY()][n.getNeighbor().getX()] == 0){
                        placeQueue.add(n.getNeighbor());
                    }else{
                        cantPlaceQueue.add(n.getNeighbor());
                    }
                    explored[n.getNeighbor().getY()][n.getNeighbor().getX()] = node.getRank();
                }
            }
            if(cantPlaceQueue.isEmpty() && placeQueue.isEmpty()){
                System.out.println("noo");

                for(Neighbor n : currPixel.getNeighbors()){
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
                    sChrom[y][x] = segmentId;
                    Pixel currPixel = pixels[y][x];
                    LinkedList<Pixel> placeQueue = new LinkedList<>();
                    for(Neighbor n : currPixel.getNeighbors()){
                        if(sChrom[n.getNeighbor().getY()][n.getNeighbor().getX()] == 0){
                            sChrom[n.getNeighbor().getY()][n.getNeighbor().getX()] = segmentId;
                            placeQueue.add(n.getNeighbor());
                        }
                    }
                    segmentId++;
                }
                //smallPri.nrSegments = 7;
                //return new Individual[]{smallPri};
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

    private int translate(HashMap<Integer,Integer> translate, boolean change,int currentId, int segmentId){
        if(change){
            if(translate.containsKey(currentId)){
                currentId = translate.get(currentId);
            }else {
                return 0;
            }
        }else{
            if(translate.containsKey(currentId)){
                currentId = translate.get(currentId);
            }else{
                translate.put(currentId, segmentId);
                currentId = translate.get(currentId);
            }
        }
        return currentId;

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

    public int compareEdgeTo(Individual other) {
        double cmp = this.fitnessEdge - other.fitnessEdge;
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

    /**
     Available nodes to link explained:
     Method returns an int where the int represents a neighboring Node.
     Given a node N (where 0 represents N):
          3
     2    N   1
          4
     Edges of the image (frame): A node that will be outside the image will not be returned.
     */
    private int getRandomAvailableNode(int x, int y){
        int select = 0;
        if(x == 0) {
            if(y == 0) {
                int rnd = r.nextInt(3); // 0,1 and 4 available.
                if(rnd == 2) {
                    rnd = 4;
                }
                select = rnd;

            } else if(y == chromosone.length -1) {
                int rnd = r.nextInt(3); // 0,1 and 3 available.
                if(rnd == 2) {
                    rnd = 3;
                }
                select = rnd;

            } else {
                int rnd = r.nextInt(4); // 0,1,3 and 4 available.
                if(rnd == 2) {
                    rnd = 4;
                }
                select = rnd;
            }
        } else if(x == chromosone[0].length -1) {
            if(y == 0) {
                int rnd = r.nextInt(3); // 0,2 and 4 available.
                if(rnd == 1) {
                    rnd = 4;
                }
                select = rnd;

            } else if(y == chromosone.length -1) {
                int rnd = r.nextInt(3); // 0,2 and 3 available.
                if(rnd == 1){
                    rnd = 3;
                }
                select = rnd;

            } else {
                int rnd = r.nextInt(4); // 0,2,3 and 4 available.
                if(rnd == 1) {
                    rnd = 4;
                }
                select = rnd;
            }
        } else {
            if(y == 0) {
                int rnd = r.nextInt(4); // 0,1,2 and 4 available.
                if(rnd == 3) {
                    rnd = 4;
                }
                select = rnd;

            } else if(y == chromosone.length -1) {
                select = r.nextInt(4); // 0,1,2 and 3 available.

            } else {
                select = r.nextInt(5);// 0,1,2,3 and 4 available.
            }
        }

        return select;
    }

    /*
     * Getters and Setters
     */

    public double getFitnessEdge() {
        return fitnessEdge;
    }

    public void setFitnessEdge(double fitnessEdge) {
        this.fitnessEdge = fitnessEdge;
    }

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
