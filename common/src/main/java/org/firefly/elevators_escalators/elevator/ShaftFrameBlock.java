package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

public final class ShaftFrameBlock extends Block implements net.minecraft.world.level.block.EntityBlock {
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty AUTOMATIC=net.minecraft.world.level.block.state.properties.BooleanProperty.create("automatic");
    private static final VoxelShape SHAPE=Shapes.or(
        box(0,0,0,1,16,1),box(15,0,0,16,16,1),box(0,0,15,1,16,16),box(15,0,15,16,16,16),
        box(0,0,0,16,1,1),box(0,15,0,16,16,1),box(0,0,15,16,1,16),box(0,15,15,16,16,16),
        box(0,0,0,1,1,16),box(15,0,0,16,1,16),box(0,15,0,1,16,16),box(15,15,0,16,16,16));
    public ShaftFrameBlock(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(AUTOMATIC,false)); }
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block,BlockState> builder) { builder.add(AUTOMATIC); }
    @Override public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new ShaftFrameBlockEntity(pos,state); }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return SHAPE; }
}
