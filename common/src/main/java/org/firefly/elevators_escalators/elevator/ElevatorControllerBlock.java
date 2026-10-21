package org.firefly.elevators_escalators.elevator;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class ElevatorControllerBlock extends BaseEntityBlock {
    public static final MapCodec<ElevatorControllerBlock> CODEC = simpleCodec(ElevatorControllerBlock::new);
    public ElevatorControllerBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level,BlockState state,net.minecraft.world.level.block.entity.BlockEntityType<T> type){
        return level.isClientSide?null:createTickerHelper(type,org.firefly.elevators_escalators.ModContent.CONTROLLER_BE.get(),(l,p,s,be)->be.previewTick());
    }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ElevatorControllerBlockEntity(pos, state); }
    @Override protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,net.minecraft.world.InteractionHand hand,BlockHitResult hit) {
        if (stack.getItem() instanceof CabinBlockItem || stack.getItem() instanceof ShaftFrameItem || stack.getItem() instanceof ElevatorRemoteItem)
            return net.minecraft.world.ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,net.minecraft.world.entity.LivingEntity placer,net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level,pos,state,placer,stack);
        if(!level.isClientSide && level.getBlockEntity(pos) instanceof ElevatorControllerBlockEntity controller) {
            if(placer instanceof Player player) {
                var look=player.getLookAngle();
                controller.setFacing(CabinGeometry.facingTowards(look.x,look.z));
            }

        }
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving) {
        if(!state.is(next.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof ElevatorControllerBlockEntity controller) controller.removeCabin();
        super.onRemove(state,level,pos,next,moving);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ElevatorControllerBlockEntity controller) controller.operate(player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
