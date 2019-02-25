package model.supportNodes;

import model.Solution;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadNode {
    private LinkedList<Solution> ob = new LinkedList<>();

    public AtomicBoolean changed = new AtomicBoolean(false);

    public void setOb(LinkedList<Solution> ob){
        this.ob = ob;
    }
    public LinkedList<Solution> getOb(){
        return ob;

    }

}
