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

    @Override
    public boolean equals(Object obj){
        if(obj instanceof Position){
            if(this.x == ((Position)obj).x && this.y == ((Position)obj).y){
                return true;
            }
        }
        return false;
    }
    @Override
    public int hashCode() {
        return Integer.hashCode(x) ^ Integer.hashCode(y);
    }
}
