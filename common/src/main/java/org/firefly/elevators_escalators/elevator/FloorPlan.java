package org.firefly.elevators_escalators.elevator;

import java.util.Arrays;

/** Offsets refer to deck bases relative to controller Y+1; at most 16 stops. */
public final class FloorPlan {
    private FloorPlan() {}
    public static int[] defaults(int height) {
        if (!Motion.validHeight(height)) throw new IllegalArgumentException("Invalid shaft height");
        return sanitize(java.util.stream.IntStream.iterate(0,v -> v<=height,v -> v+4).toArray(),height);
    }
    public static int[] load(int[] saved,int height) {
        int[] floors=sanitize(saved,height);
        // Earlier builds only supplied two endpoints. Upgrade those to useful
        // four-block defaults without replacing an existing custom floor table.
        return floors.length==2 ? defaults(height) : floors;
    }
    public static int[] sanitize(int[] values, int height) {
        if (!Motion.validHeight(height)) throw new IllegalArgumentException("Invalid shaft height");
        int[] interior=Arrays.stream(values).filter(v -> v>0 && v<height).distinct().sorted().limit(14).toArray();
        return java.util.stream.IntStream.concat(java.util.stream.IntStream.of(0),
            java.util.stream.IntStream.concat(Arrays.stream(interior),java.util.stream.IntStream.of(height))).toArray();
    }
    public static int[] add(int[] values,int offset,int height) {
        if (offset<0 || offset>height || values.length>=16 && Arrays.stream(values).noneMatch(v->v==offset))
            throw new IllegalArgumentException("Floor outside shaft or 16-stop limit reached");
        return sanitize(java.util.stream.IntStream.concat(Arrays.stream(values),java.util.stream.IntStream.of(offset)).toArray(),height);
    }
    public static int next(int[] values,int current,boolean reverse) {
        int index=Arrays.binarySearch(values,current);
        if (index<0) index=0;
        return values[Math.floorMod(index+(reverse?-1:1),values.length)];
    }
}
