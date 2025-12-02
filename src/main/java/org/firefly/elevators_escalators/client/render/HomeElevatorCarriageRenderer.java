package org.firefly.elevators_escalators.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.firefly.elevators_escalators.EnrollBlocks;
import org.firefly.elevators_escalators.entity.HomeElevatorCarriageEntity;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Renders the 2x2x2 carriage as a stack of the carriage block voxels.
 */
public class HomeElevatorCarriageRenderer extends EntityRenderer<HomeElevatorCarriageEntity>
{
    private final BlockRenderDispatcher blockRenderer;
    private final BlockState carriageState;

    public HomeElevatorCarriageRenderer(@Nonnull EntityRendererProvider.Context context)
    {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.carriageState = Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_CARRIAGE_BLOCK.get()).defaultBlockState();
    }

    @Override
    public void render(@Nonnull HomeElevatorCarriageEntity entity,
                       float entityYaw,
                       float partialTicks,
                       @Nonnull PoseStack poseStack,
                       @Nonnull MultiBufferSource buffer,
                       int packedLight)
    {
        poseStack.pushPose();
        // Center the 2x2 footprint on the entity's position (entity sits at center of the cabin)
        poseStack.translate(-1.0D, 0.0D, -1.0D);

        for (int y = 0; y < 2; y++)
        {
            for (int x = 0; x < 2; x++)
            {
                for (int z = 0; z < 2; z++)
                {
                    poseStack.pushPose();
                    poseStack.translate(x, y, z);
                    renderCarriageVoxel(poseStack, buffer, packedLight);
                    poseStack.popPose();
                }
            }
        }

        poseStack.popPose();
    }

    @SuppressWarnings("deprecation")
    private void renderCarriageVoxel(@Nonnull PoseStack poseStack,
                                     @Nonnull MultiBufferSource buffer,
                                     int packedLight)
    {
        blockRenderer.renderSingleBlock(Objects.requireNonNull(carriageState), poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @Nonnull ResourceLocation getTextureLocation(@Nonnull HomeElevatorCarriageEntity entity)
    {
        return Objects.requireNonNull(TextureAtlas.LOCATION_BLOCKS);
    }
}
