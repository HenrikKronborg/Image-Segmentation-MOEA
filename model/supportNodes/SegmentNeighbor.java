package model.supportNodes;

public class SegmentNeighbor implements Comparable<SegmentNeighbor>{
    private int id;
    private double distance; // Euclidean distance between colors

    public SegmentNeighbor(int id, double distance) {
        this.id = id;
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(SegmentNeighbor o) {
        double cmp = this.distance - o.distance;
        if(cmp > 0){
            return 1;
        } else if(cmp == 0){
            return 0;
        }
        return -1;
    }
}
