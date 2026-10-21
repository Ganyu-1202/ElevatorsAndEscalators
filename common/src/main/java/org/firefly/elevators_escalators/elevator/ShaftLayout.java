package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.Direction;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ShaftLayout {
    public record Cell(int x,int y,int z) {}
    private ShaftLayout() {}
    public static Set<Cell> build(int height,int[] floors) { return build(height,floors,Direction.SOUTH); }
    public static Set<Cell> build(int height,int[] floors,Direction facing) {
        if(!Motion.validHeight(height)) throw new IllegalArgumentException("Invalid height");
        Set<Cell> cells=new LinkedHashSet<>();
        int roof=height+4;
        for(int y=0;y<=roof;y++) for(int x:new int[]{-2,2}) for(int z:new int[]{-2,2}) cells.add(new Cell(x,y,z));
        Set<Integer> rings=new LinkedHashSet<>(); rings.add(0); rings.add(roof);
        for(int floor:floors) if(floor>=0 && floor<=height) rings.add(floor+1);
        for(int y:rings) {
            for(int t=-2;t<=2;t++) {
                if(Math.abs(t)==2) { for(int x:new int[]{-2,2}) for(int z:new int[]{-2,2}) cells.add(new Cell(x,y,z)); continue; }
                if(y==roof) { cells.add(new Cell(t,y,-2)); cells.add(new Cell(t,y,2)); cells.add(new Cell(-2,y,t)); cells.add(new Cell(2,y,t)); continue; }
                // Mid-span floor beams only, leaving the placer-facing landing clear.
                if(facing!=Direction.NORTH) cells.add(new Cell(t,y,-2));
                if(facing!=Direction.SOUTH) cells.add(new Cell(t,y,2));
                if(facing!=Direction.WEST) cells.add(new Cell(-2,y,t));
                if(facing!=Direction.EAST) cells.add(new Cell(2,y,t));
            }
        }
        return Set.copyOf(cells);
    }
}
