package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.Direction;
import java.util.ArrayList;
import java.util.List;

/** Base layout faces south (+Z); rotation maps it to the controller placer's direction. */
public final class CabinGeometry {
    public static final double TOP=3.25;
    public static final double THICKNESS=0.125;
    public record Box(double x1,double y1,double z1,double x2,double y2,double z2) {
        public boolean contains(double x,double y,double z) { return x>x1 && x<x2 && y>y1 && y<y2 && z>z1 && z<z2; }
    }
    private CabinGeometry() {}
    public static Direction facing(int ordinal) {
        // Must be the inverse of Direction#get2DDataValue: S=0, W=1, N=2, E=3.
        return Direction.from2DDataValue(Math.floorMod(ordinal,4));
    }
    /** Snap a horizontal vector to a cardinal direction; pitch is ignored. */
    public static Direction facingTowards(double dx,double dz) {
        // Door faces the placer, i.e. opposite to the horizontal look vector.
        return Math.abs(dx)>Math.abs(dz) ? (dx<0?Direction.EAST:Direction.WEST) : (dz<0?Direction.SOUTH:Direction.NORTH);
    }
    public static Box rotate(Box b,Direction facing) {
        return switch(facing) {
            case NORTH -> new Box(-b.x2(),b.y1(),-b.z2(),-b.x1(),b.y2(),-b.z1());
            case EAST -> new Box(b.z1(),b.y1(),-b.x2(),b.z2(),b.y2(),-b.x1());
            case WEST -> new Box(-b.z2(),b.y1(),b.x1(),-b.z1(),b.y2(),b.x2());
            default -> b;
        };
    }
    public static List<Box> shell(boolean closed) { return shell(closed,Direction.SOUTH); }
    public static List<Box> shell(boolean closed,Direction facing) {
        List<Box> boxes=new ArrayList<>();
        boxes.add(new Box(-1.5,.25,-1.5,-1.375,3.0,1.5));
        boxes.add(new Box(1.375,.25,-1.5,1.5,3.0,1.5));
        boxes.add(new Box(-1.375,.25,-1.5,1.375,3.0,-1.375));
        boxes.add(new Box(-1.5,3.0,-1.5,1.5,TOP,1.5));
        boxes.add(new Box(-1.375,.25,1.375,-1.0,3.0,1.5));
        boxes.add(new Box(1.0,.25,1.375,1.375,3.0,1.5));
        if (closed) boxes.add(new Box(-1.0,.25,1.375,1.0,3.0,1.5));
        return boxes.stream().map(b->rotate(b,facing)).toList();
    }
}
