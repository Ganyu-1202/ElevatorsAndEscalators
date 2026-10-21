package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.firefly.elevators_escalators.ModContent;
import java.util.*;

public final class AutomaticShaft {
    private AutomaticShaft() {}
    public static BlockPos obstruction(Level level,BlockPos origin,int height,int[] floors,UUID owner) {
        return obstruction(level,origin,height,floors,owner,net.minecraft.core.Direction.SOUTH);
    }
    public static BlockPos obstruction(Level level,BlockPos origin,int height,int[] floors,UUID owner,net.minecraft.core.Direction facing) {
        for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++) for(int y=1;y<=height+4;y++) {
            BlockPos p=origin.offset(x,y,z);
            if(!available(level,p) || !level.getBlockState(p).getCollisionShape(level,p).isEmpty() || !level.getFluidState(p).isEmpty()) return p;
        }
        for(var cell:ShaftLayout.build(height,floors,facing)) {
            BlockPos p=origin.offset(cell.x(),cell.y(),cell.z());
            if(!available(level,p)) return p;
            if(level.getBlockState(p).isAir()) continue;
            if(owner!=null && level.getBlockEntity(p) instanceof ShaftFrameBlockEntity frame && frame.ownedBy(origin,owner)) continue;
            return p;
        }
        return null;
    }
    private static boolean available(Level level,BlockPos p) {
        return !level.isOutsideBuildHeight(p) && level.getWorldBorder().isWithinBounds(p) && level.hasChunkAt(p);
    }
    public static boolean rebuild(ElevatorControllerBlockEntity controller,int height,int[] floors) { return rebuild(controller,height,floors,controller.facing()); }
    public static boolean rebuild(ElevatorControllerBlockEntity controller,int height,int[] floors,net.minecraft.core.Direction facing) {
        if(!(controller.getLevel() instanceof ServerLevel level)) return false;
        BlockPos origin=controller.getBlockPos(); UUID owner=controller.shaftOwner();
        if(obstruction(level,origin,height,floors,owner,facing)!=null) return false;
        Set<BlockPos> wanted=new HashSet<>(); List<BlockPos> added=new ArrayList<>();
        for(var cell:ShaftLayout.build(height,floors,facing)) {
            BlockPos p=origin.offset(cell.x(),cell.y(),cell.z()); wanted.add(p);
            if(level.getBlockEntity(p) instanceof ShaftFrameBlockEntity existing && existing.ownedBy(origin,owner)) continue;
            boolean placed=level.setBlock(p,ModContent.FRAME.get().defaultBlockState().setValue(ShaftFrameBlock.AUTOMATIC,true),3);
            if(placed) added.add(p);
            if(!placed || !(level.getBlockEntity(p) instanceof ShaftFrameBlockEntity frame)) {
                for(BlockPos rollback:added) if(level.getBlockState(rollback).is(ModContent.FRAME.get())) level.removeBlock(rollback,false);
                return false;
            } else { frame.bind(origin,owner); }
        }
        for(BlockPos old:controller.shaftCells()) if(!wanted.contains(old)) removeOwned(level,old,origin,owner);
        controller.setShaftCells(wanted);
        return true;
    }
    public static void remove(ElevatorControllerBlockEntity controller) {
        if(!(controller.getLevel() instanceof ServerLevel level)) return;
        for(BlockPos p:controller.shaftCells()) {
            // Explicit teardown may cross chunk boundaries: load only recorded frame chunks.
            level.getChunk(p.getX()>>4,p.getZ()>>4);
            removeOwned(level,p,controller.getBlockPos(),controller.shaftOwner());
        }
        var platform=controller.platform(); if(platform!=null) platform.discard();
    }
    private static void removeOwned(ServerLevel level,BlockPos p,BlockPos origin,UUID owner) {
        if(level.getBlockState(p).is(ModContent.FRAME.get()) && level.getBlockEntity(p) instanceof ShaftFrameBlockEntity frame && frame.ownedBy(origin,owner)) level.removeBlock(p,false);
    }
}
