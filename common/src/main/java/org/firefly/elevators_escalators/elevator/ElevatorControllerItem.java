package org.firefly.elevators_escalators.elevator;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;

public final class ElevatorControllerItem extends BlockItem {
    public ElevatorControllerItem(Block block,Properties properties) { super(block,properties); }
    @Override public InteractionResult place(BlockPlaceContext context) {
        var player=context.getPlayer(); var origin=context.getClickedPos(); var level=context.getLevel();
        var facing=player==null?net.minecraft.core.Direction.SOUTH
            :CabinGeometry.facingTowards(player.getLookAngle().x,player.getLookAngle().z);
        var blocked=AutomaticShaft.obstruction(level,origin,8,FloorPlan.defaults(8),null,facing);
        if(blocked==null && player!=null) {
            for(var cell:ShaftLayout.build(8,FloorPlan.defaults(8),facing)) {
                var pos=origin.offset(cell.x(),cell.y(),cell.z());
                if(!level.mayInteract(player,pos) || !player.mayUseItemAt(pos,context.getClickedFace(),context.getItemInHand())) { blocked=pos; break; }
            }
        }
        if(blocked!=null) {
            if(!level.isClientSide && player!=null) player.displayClientMessage(Component.translatable("message.elevators_escalators.auto_shaft_blocked",blocked.toShortString()),false);
            return InteractionResult.FAIL;
        }
        // ElevatorControllerBlock.setPlacedBy applies this same horizontal facing
        // and builds exactly once. Never duplicate construction here.
        return super.place(context);
    }
}
