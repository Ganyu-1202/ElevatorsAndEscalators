package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import java.util.LinkedHashSet;
import java.util.Set;

/** World-grid cells entirely outside the swept cabin volume. Never writes world blocks. */
public record FrameBlueprint(Set<BlockPos> cells, java.util.List<BlockPos> panels, java.util.List<BlockPos> supports) {
    public FrameBlueprint { cells=Set.copyOf(cells); panels=java.util.List.copyOf(panels);supports=java.util.List.copyOf(supports); }
    public enum Status { NONE, COMPLETE, PARTIAL }
    public static Status classify(int air,int full,int total) {
        return air==total?Status.NONE:full==total?Status.COMPLETE:Status.PARTIAL;
    }
    public static FrameBlueprint create(AABB travel,int[] offsets,Direction front) {
        int left=(int)Math.floor(travel.minX)-1,right=(int)Math.ceil(travel.maxX);
        int back=(int)Math.floor(travel.minZ)-1,forward=(int)Math.ceil(travel.maxZ);
        int bottom=(int)Math.floor(travel.minY)-1,roof=(int)Math.ceil(travel.maxY);
        Set<BlockPos> cells=new LinkedHashSet<>();
        for(int y=bottom;y<=roof;y++)for(int x:new int[]{left,right})for(int z:new int[]{back,forward})cells.add(new BlockPos(x,y,z));
        java.util.List<BlockPos> panels=new java.util.ArrayList<>(),supports=new java.util.ArrayList<>();
        for(int offset:offsets){
            // Place on the outside of a front corner post, not inside the doorway.
            BlockPos support=switch(front){
                case SOUTH -> new BlockPos(right,bottom+2+offset,forward);
                case NORTH -> new BlockPos(left,bottom+2+offset,back);
                case EAST -> new BlockPos(right,bottom+2+offset,back);
                default -> new BlockPos(left,bottom+2+offset,forward);
            };
            // For very low cabins the topmost support still lies within the continuous post.
            supports.add(support);panels.add(support.relative(front));
        }
        return new FrameBlueprint(cells,panels,supports);
    }
}
