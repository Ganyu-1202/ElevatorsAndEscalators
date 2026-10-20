package org.firefly.elevators_escalators.elevator;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ShaftLayout {
    public record Cell(int x,int y,int z) {}
    private ShaftLayout() {}
    public static Set<Cell> build(int height,int[] floors) {
        if(!Motion.validHeight(height)) throw new IllegalArgumentException("Invalid height");
        Set<Cell> cells=new LinkedHashSet<>();
        int roof=height+4;
        for(int y=0;y<=roof;y++) for(int x:new int[]{-2,2}) for(int z:new int[]{-2,2}) cells.add(new Cell(x,y,z));
        Set<Integer> rings=new LinkedHashSet<>(); rings.add(0); rings.add(roof);
        for(int floor:floors) if(floor>=0 && floor<=height) rings.add(floor+1);
        for(int y:rings) {
            for(int t=-2;t<=2;t++) {
                cells.add(new Cell(t,y,-2)); cells.add(new Cell(-2,y,t)); cells.add(new Cell(2,y,t));
                // Keep all south-side landing openings unobstructed, except roof ring.
                if(y==roof || Math.abs(t)==2) cells.add(new Cell(t,y,2));
            }
        }
        return Set.copyOf(cells);
    }
}
