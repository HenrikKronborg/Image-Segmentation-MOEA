package model.segmentGeneration;

import java.util.LinkedList;
import java.util.List;

public class Node {

    private LinkedList<Node> parents = new LinkedList<>();


    private int value;
    private Node child;

    public Node(int value){

    }

    public void setChild(Node child) {
        this.child = child;
        child.parents.add(this);
    }


    public Node getChild() {
        return child;
    }

    public LinkedList<Node> getParents() {
        return parents;
    }

    public int getValue() {
        return value;
    }


}
