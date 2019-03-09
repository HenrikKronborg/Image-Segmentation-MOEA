package model.utils;

public class MutableShort {
    private short a;

    public MutableShort(short a) {
        this.a = a;
    }

    public short getValue() {
        return a;
    }

    public void setValue(short a) {
        this.a = a;
    }

    @Override
    public boolean equals(Object anObject){
        if(anObject instanceof MutableShort){
            return ((MutableShort) anObject).a == a;
        }
        return false;
    }

}
