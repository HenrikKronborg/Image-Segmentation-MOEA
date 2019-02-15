package model.segmentGeneration;

import java.util.LinkedList;
import java.util.List;

public class Pending {
    int pending;
    List<Integer> queue = new LinkedList<>();

    public Pending(int pending, int waiting){
        this.pending = pending;
        queue.add(waiting);
    }

    public void add(int waiting){
        queue.add(waiting);
    }

}
