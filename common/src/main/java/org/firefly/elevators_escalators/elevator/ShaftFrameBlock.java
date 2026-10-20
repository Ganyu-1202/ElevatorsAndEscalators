package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

public final class ShaftFrameBlock extends Block {
    private static final VoxelShape SHAPE=Shapes.or(
        box(0,0,0,2,16,2),box(14,0,0,16,16,2),box(0,0,14,2,16,16),box(14,0,14,16,16,16),
        box(0,0,0,16,2,2),box(0,14,0,16,16,2),box(0,0,14,16,2,16),box(0,14,14,16,16,16),
        box(0,0,0,2,2,16),box(14,0,0,16,2,16),box(0,14,0,2,16,16),box(14,14,0,16,16,16));
    public ShaftFrameBlock(Properties properties) { super(properties); }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return SHAPE; }
}
