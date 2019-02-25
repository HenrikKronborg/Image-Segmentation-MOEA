package model.supportNodes;

import model.Individual;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadNode {
    private int generation;
    private LinkedList<Individual> ob = new LinkedList<>();

    public AtomicBoolean changed = new AtomicBoolean(false);

    public void setOb(LinkedList<Individual> ob){
        this.ob = ob;
    }
    public LinkedList<Individual> getOb(){
        return ob;
    }
    public int getGeneration() {
        return generation;
    }
    public void setGeneration(int generation) {
        this.generation = generation;
    }
}
