package org.firefly.elevators_escalators.escalator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public final class EscalatorControllerBlock extends BaseEntityBlock {
 public static final MapCodec<EscalatorControllerBlock> CODEC=simpleCodec(EscalatorControllerBlock::new);
 public EscalatorControllerBlock(Properties p){super(p);}
 @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
 @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
 @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new EscalatorControllerBlockEntity(p,s);}
 @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return createTickerHelper(t,org.firefly.elevators_escalators.ModContent.ESC_CONTROLLER_BE.get(),(a,b,c,be)->be.tick());}
 @Override protected ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand h,BlockHitResult hit){return stack.getItem() instanceof EscalatorSurveyItem?ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION:ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p)instanceof EscalatorControllerBlockEntity be)EscalatorMenu.open(player,be);return InteractionResult.sidedSuccess(l.isClientSide);}
 @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&!l.isClientSide&&l.getBlockEntity(p)instanceof EscalatorControllerBlockEntity be)be.dismantle();super.onRemove(s,l,p,next,moving);}
}
