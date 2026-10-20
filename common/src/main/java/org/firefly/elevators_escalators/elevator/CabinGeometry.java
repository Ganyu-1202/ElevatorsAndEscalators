package org.firefly.elevators_escalators.elevator;

import java.util.ArrayList;
import java.util.List;

/** Local coordinates relative to the existing 3x3 deck. Door faces south (+Z). */
public final class CabinGeometry {
    public static final double TOP=3.25;
    public static final double THICKNESS=0.125;
    public record Box(double x1,double y1,double z1,double x2,double y2,double z2) {
        public boolean contains(double x,double y,double z) { return x>x1 && x<x2 && y>y1 && y<y2 && z>z1 && z<z2; }
    }
    private CabinGeometry() {}
    public static List<Box> shell(boolean closed) {
        List<Box> boxes=new ArrayList<>();
        boxes.add(new Box(-1.5,.25,-1.5,-1.375,3.0,1.5));
        boxes.add(new Box(1.375,.25,-1.5,1.5,3.0,1.5));
        boxes.add(new Box(-1.375,.25,-1.5,1.375,3.0,-1.375));
        boxes.add(new Box(-1.5,3.0,-1.5,1.5,TOP,1.5));
        // Front jambs leave a two-block-wide clear doorway.
        boxes.add(new Box(-1.375,.25,1.375,-1.0,3.0,1.5));
        boxes.add(new Box(1.0,.25,1.375,1.375,3.0,1.5));
        if (closed) boxes.add(new Box(-1.0,.25,1.375,1.0,3.0,1.5));
        return List.copyOf(boxes);
    }
}
