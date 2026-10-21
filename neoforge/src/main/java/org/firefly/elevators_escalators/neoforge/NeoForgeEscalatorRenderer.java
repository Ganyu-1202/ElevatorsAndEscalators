package org.firefly.elevators_escalators.neoforge;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import org.firefly.elevators_escalators.client.EscalatorTreadRenderer;
import org.firefly.elevators_escalators.escalator.EscalatorStepBlockEntity;

/** NeoForge performs an additional BER bounds test, independent of shouldRenderOffScreen. */
public final class NeoForgeEscalatorRenderer extends EscalatorTreadRenderer {
    public NeoForgeEscalatorRenderer(BlockEntityRendererProvider.Context context){super(context);}
    @Override public AABB getRenderBoundingBox(EscalatorStepBlockEntity step){
        var p=step.getBlockPos();
        return new AABB(p.getX()-.01,p.getY()-.01,p.getZ()-.01,p.getX()+1.01,p.getY()+2.42,p.getZ()+1.01);
    }
}
