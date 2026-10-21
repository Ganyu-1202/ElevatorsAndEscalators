package org.firefly.elevators_escalators.escalator;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.firefly.elevators_escalators.ModContent;

/** Server-side support check, not a general fall-damage immunity. */
public final class EscalatorFallProtection {
    private EscalatorFallProtection(){}
    public static boolean safeMotion(double dy,float accumulated){
        return Double.isFinite(dy)&&dy>=-.35&&dy<=.1&&accumulated<=1;
    }
    public static void resetSupportedFall(Entity entity,double dy){
        if(!safeMotion(dy,entity.fallDistance)||entity.isSpectator()||entity.isPassenger())return;
        var level=entity.level();var box=entity.getBoundingBox();
        // Ramp geometry can extend 1.5 blocks above its owner. Inspect only loaded cells.
        for(int x=Mth.floor(box.minX+1e-6);x<=Mth.floor(box.maxX-1e-6);x++)
            for(int z=Mth.floor(box.minZ+1e-6);z<=Mth.floor(box.maxZ-1e-6);z++)
                for(int y=Mth.floor(entity.getY())-2;y<=Mth.floor(entity.getY());y++){
                    var pos=new BlockPos(x,y,z);
                    if(!level.hasChunkAt(pos))continue;
                    var state=level.getBlockState(pos);
                    if(!state.is(ModContent.ESCALATOR.get())||!state.getValue(EscalatorBlock.ASSEMBLED))continue;
                    var facing=state.getValue(EscalatorBlock.FACING);
                    double edge=switch(facing){
                        case NORTH->1-(box.minZ-z);case EAST->box.maxX-x;
                        case WEST->1-(box.minX-x);default->box.maxZ-z;
                    };
                    double slice=Math.ceil(Math.max(0,Math.min(1,edge))*EscalatorRamp.SLICES)/EscalatorRamp.SLICES;
                    double top=y+EscalatorRamp.surface(slice,state.getValue(EscalatorBlock.RISING));
                    // Require feet actually on the ramp, not merely in its surrounding volume.
                    if(Math.abs(entity.getY()-top)<=.08){entity.fallDistance=0;return;}
                }
    }
}
