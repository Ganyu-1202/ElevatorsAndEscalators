package org.firefly.elevators_escalators.escalator;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

/** Modular half-rise steps: place ascending full blocks in a staircase. Redstone stops transport. */
public final class EscalatorBlock extends Block {
    public static final MapCodec<EscalatorBlock> CODEC = simpleCodec(EscalatorBlock::new);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty REVERSED=BooleanProperty.create("reversed");
    public EscalatorBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(REVERSED,false)); }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING,REVERSED); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) { return defaultBlockState().setValue(FACING,ctx.getHorizontalDirection()); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        VoxelShape base=Block.box(0,0,0,16,8,16);
        VoxelShape upper=switch(state.getValue(FACING)) {
            case NORTH -> Block.box(0,8,0,16,16,8);
            case SOUTH -> Block.box(0,8,8,16,16,16);
            case WEST -> Block.box(0,8,0,8,16,16);
            default -> Block.box(8,8,0,16,16,16);
        };
        return Shapes.or(base,upper);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isSpectator()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            BlockState next = state.cycle(REVERSED);
            level.setBlock(pos,next,3);
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(next.getValue(REVERSED) ? "message.elevators_escalators.escalator_down" : "message.elevators_escalators.escalator_up"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level,pos,state,entity);
        if (entity.isShiftKeyDown() || entity.isSpectator() || level.hasNeighborSignal(pos)) return;
        Direction direction=state.getValue(FACING); boolean reverse=state.getValue(REVERSED);
        double speed=reverse?-0.065:0.065;
        // Move horizontally and lift only when climbing an obstructing half-step.
        Vec3 move=new Vec3(direction.getStepX()*speed,0,direction.getStepZ()*speed);
        if (!reverse && !level.noCollision(entity,entity.getBoundingBox().move(move))) {
            if (level.noCollision(entity,entity.getBoundingBox().move(0,0.5,0))) entity.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(0,0.5,0));
        }
        entity.move(net.minecraft.world.entity.MoverType.SELF,move);
        entity.fallDistance=0;
    }
}
