package org.firefly.elevators_escalators.elevator;

import net.minecraft.world.level.block.Block;

/** A loose cabin item is an ordinary mineable block. Assembly consumes the item
 * and creates an invulnerable ElevatorPlatformEntity, not protected world blocks.
 */
public final class CabinBlock extends Block {
    public CabinBlock(Properties properties){super(properties);}
}
