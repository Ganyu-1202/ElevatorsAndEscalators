package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Cabinet components are managed through the elevator controller, not mined individually. */
public final class CabinBlock extends Block {
    public CabinBlock(Properties properties){super(properties);}
    @Override protected float getDestroyProgress(BlockState state,Player player,BlockGetter level,BlockPos pos){return 0;}
}
