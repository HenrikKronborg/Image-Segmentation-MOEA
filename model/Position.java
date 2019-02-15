package model;

public class Position {
    protected int x = 0; // X coordinate
    protected int y = 0; // Y coordinate

    public Position(){

    }
    public Position(int x,int y){
        this.x = x;
        this.y = y;
    }

    /*
     * Methods
     */
    public static double distanceTo(Position d, Position c){
        return Math.sqrt(Math.pow(d.getX() - c.getX(), 2) + Math.pow(d.getY() - c.getY(), 2));
    }

    /*
     * Getters and Setters
     */
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
}
