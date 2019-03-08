package model;

import controller.MOEA;
import model.supportNodes.Neighbor;
import model.supportNodes.Pixel;
import model.supportNodes.SegmentNeighbor;
import model.supportNodes.SegmentNode;
import model.utils.FitnessCalc;
import model.utils.ImageLoader;

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

        double newThreshold = threshold*0.5;
        int lastNrSegments = nrSegments;
        while(nrSegments > MAX) {
            double diff = lastNrSegments - nrSegments;
            lastNrSegments = nrSegments;
            if (diff/(double)lastNrSegments < 0.1){
                newThreshold += 5 + Math.random();

            }else if(diff/(double)lastNrSegments < 0.2){
                newThreshold += 2 + Math.random();

            }else if(diff/(double)lastNrSegments < 0.3){
                newThreshold += 1 + Math.random();

            }else if(diff/(double)lastNrSegments < 0.4){
                newThreshold += Math.random();

            }
            cleanMerge(f, newThreshold, MAX);
            removeSmallSegments(0.1);

        }
        removeSmallSegments(0.05);
        System.out.println("Segment done! "+nrSegments);

    }

    private void cleanMerge(FitnessCalc f, double threshold, int MAX) {
        HashMap<Integer, SegmentNode> avgColor = f.generateAverageColor(this);
        HashMap<Integer,Integer> idTable = new HashMap<>();
        int segments = avgColor.size()-1;
        int segmentId = 1;

        for(int key : avgColor.keySet()){
            if(segments <= MAX){
                break;
            }
            if (!idTable.containsKey(key)) {
                SegmentNode root = avgColor.get(key);
                idTable.put(key,segmentId);
                PriorityQueue<SegmentNeighbor> pQueue = new PriorityQueue<>();
                for (int n : root.getNeighbors()) {
                    SegmentNode neighbor = avgColor.get(n);
                    double diff = Math.sqrt(Math.pow(root.getAvgRed() - neighbor.getAvgRed(), 2) + Math.pow(root.getAvgGreen() - neighbor.getAvgGreen(), 2) + Math.pow(root.getAvgBlue() - neighbor.getAvgBlue(), 2));
                    pQueue.add(new SegmentNeighbor(n,diff));
                }
                while (pQueue.size() > 0) {
                    SegmentNeighbor frontSeg = pQueue.poll();
                    if (!idTable.containsKey(frontSeg.getId())) {
                        if (frontSeg.getDistance() < threshold) {
                            idTable.put(frontSeg.getId(),segmentId);
                            SegmentNode frontNode = avgColor.get(frontSeg.getId());
                            segments--;
                            if(segments <= MAX){
                                break;
                            }
                            for (int n : frontNode.getNeighbors()) {
                                SegmentNode neighbor = avgColor.get(n);
                                double diff = Math.sqrt(Math.pow(frontNode.getAvgRed() - neighbor.getAvgRed(), 2) + Math.pow(frontNode.getAvgGreen() - neighbor.getAvgGreen(), 2) + Math.pow(frontNode.getAvgBlue() - neighbor.getAvgBlue(), 2));
                                pQueue.add(new SegmentNeighbor(n,diff));
                            }
                        } else {
                            break;
                        }
                    }
                }
                if(segmentId == Short.MAX_VALUE ){
                    System.out.println(":(");
                    break;
                }
                segmentId++;
            }
        }
        for (int y = 0; y < ImageLoader.getHeight(); y++) {
            for (int x = 0; x < ImageLoader.getWidth(); x++) {
                int id = chromosone[y][x];
                if(idTable.containsKey(id)){
                    chromosone[y][x] = idTable.get(id).shortValue();
                }else{
                    idTable.put(id,segmentId);
                    segmentId++;
                }
            }
        }
        nrSegments = segmentId-1;
    }

    public void removeSmallSegments(double thresholdProcent){
        ArrayList<Integer> pixelsInSegment = new ArrayList<>();

        for (int y = 0; y < ImageLoader.getHeight(); y++) {
            for (int x = 0; x < ImageLoader.getWidth(); x++) {
                int segmentId = chromosone[y][x];
                if(segmentId != 0){
                    while(pixelsInSegment.size() <= segmentId){
                        pixelsInSegment.add(0);
                    }
                    pixelsInSegment.set(segmentId,pixelsInSegment.get(segmentId)+1);
                }
            }
        }

        int avg = 0;
        for(int value : pixelsInSegment){
                avg += value;
        }
        avg /= pixelsInSegment.size();
        ArrayList<Integer> toMerge = new ArrayList<>();
        for(int i = 1; i < pixelsInSegment.size(); i++){
            if(pixelsInSegment.get(i) < (int)(avg*thresholdProcent)){
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
            nrSegments = pixelsInSegment.size()-1;
        }
    }
    private short cleanMerge(int maxsegments, FitnessCalc f) {
        HashMap<Integer, SegmentNode> avgColor = f.generateAverageColor(this);
        ArrayList<Double[]> toMerge = new ArrayList<>(avgColor.size());

        for(int key : avgColor.keySet()){
            double smallestDiff = Double.MAX_VALUE;

            SegmentNode node1 = avgColor.get(key);
            SegmentNode nodeSmall = null;
            Double keySmall = null;
            for(int neighbor : node1.getNeighbors()){
                SegmentNode node2 = avgColor.get(neighbor);

                double diff = Math.sqrt(Math.pow(node1.getAvgRed() - node2.getAvgRed(), 2) + Math.pow(node1.getAvgGreen() - node2.getAvgGreen(), 2) + Math.pow(node1.getAvgBlue() - node2.getAvgBlue(), 2));
                if(diff < smallestDiff){
                    //Merg the smallest segment in to the other.
                    nodeSmall = node2;
                    keySmall = (double)neighbor;
                    smallestDiff = diff;
                }
            }
            if(nodeSmall != null) {
                if (node1.getNrPixels() < nodeSmall.getNrPixels())
                    toMerge.add(new Double[]{(double) key, keySmall, smallestDiff});
                else if(node1.getNrPixels() == nodeSmall.getNrPixels()){
                    if(key < keySmall){
                        toMerge.add(new Double[]{(double) key, keySmall, smallestDiff});
                    }else{
                        toMerge.add(new Double[]{keySmall,(double)key,smallestDiff});
                    }
                }else
                    toMerge.add(new Double[]{keySmall,(double)key,smallestDiff});
            }
        }

        toMerge.sort(Comparator.comparing(a -> a[2]));
        int range = toMerge.size()-maxsegments;
        HashMap<Integer,Integer> idTable = new HashMap<>();
        int newId = 1;
        for (int y = 0; y < ImageLoader.getHeight(); y++) {
            for (int x = 0; x < ImageLoader.getWidth(); x++) {
                int id = chromosone[y][x];
                if(idTable.containsKey(id)){
                    chromosone[y][x] = idTable.get(id).shortValue();
                }else{
                    for (int i = 0; i < range; i++) {
                        int otherId = toMerge.get(i)[0].intValue();
                        if(id == otherId){
                            idTable.put(id,newId);
                            idTable.put(otherId,newId);
                            chromosone[y][x] = (short)newId;
                        }
                    }
                    newId++;

                }
            }
        }
        System.out.println("merge.");

        return (short)newId;




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
