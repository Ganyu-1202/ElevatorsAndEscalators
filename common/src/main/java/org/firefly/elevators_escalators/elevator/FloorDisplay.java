package org.firefly.elevators_escalators.elevator;

public final class FloorDisplay {
    private FloorDisplay() {}
    public static int number(int[] stops,double offset) {
        int index=0;
        for(int i=0;i<stops.length;i++) if(offset>=stops[i]-.05) index=i;
        return index+1;
    }
}
