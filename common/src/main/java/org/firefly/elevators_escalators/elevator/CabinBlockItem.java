package org.firefly.elevators_escalators.elevator;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
public final class CabinBlockItem extends BlockItem {
    public CabinBlockItem(Block block,Properties properties){super(block,properties);}
    @Override public InteractionResult useOn(UseOnContext context){
        if(!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof ElevatorControllerBlockEntity c))return super.useOn(context);
        if(context.getLevel().isClientSide)return InteractionResult.SUCCESS;
        var p=context.getPlayer();if(p==null)return InteractionResult.FAIL;
        if(c.assemble(p)){if(!p.getAbilities().instabuild)context.getItemInHand().shrink(1);return InteractionResult.CONSUME;}
        return InteractionResult.FAIL;
    }
}
