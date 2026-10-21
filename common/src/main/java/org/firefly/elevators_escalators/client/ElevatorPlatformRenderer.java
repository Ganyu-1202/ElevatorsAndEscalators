package org.firefly.elevators_escalators.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.firefly.elevators_escalators.elevator.*;

public final class ElevatorPlatformRenderer extends EntityRenderer<ElevatorPlatformEntity> {
    public ElevatorPlatformRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius=1.5F; }
    @Override public ResourceLocation getTextureLocation(ElevatorPlatformEntity entity) { return TextureAtlas.LOCATION_BLOCKS; }
    private void part(Block block,double x,double y,double z,double w,double h,double d,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose(); pose.translate(x,y,z); pose.scale((float)w,(float)h,(float)d);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
    private void box(Block block,CabinGeometry.Box b,PoseStack pose,MultiBufferSource buffers,int light) {
        part(block,b.x1(),b.y1(),b.z1(),b.x2()-b.x1(),b.y2()-b.y1(),b.z2()-b.z1(),pose,buffers,light);
    }
    @Override public void render(ElevatorPlatformEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();
        var spec=entity.cabinSpec();
        boolean swap=entity.facing().getAxis()==net.minecraft.core.Direction.Axis.X;
        pose.scale((float)(swap?spec.sz():spec.sx()),(float)spec.sy(),(float)(swap?spec.sx():spec.sz()));
        box(Blocks.IRON_BLOCK,new CabinGeometry.Box(-1.5,0,-1.5,1.5,.25,1.5),pose,buffers,light);
        var facing=entity.facing();
        for(var b:CabinGeometry.shell(entity.doorsClosed(),facing))
            box(b.y1()>=3?Blocks.IRON_BLOCK:Blocks.GLASS,b,pose,buffers,light);
        part(Blocks.SEA_LANTERN,-.5,2.92,-.5,1,.08,1,pose,buffers,0xF000F0);
        for(double x:new double[]{-1.5,1.375}) for(double z:new double[]{-1.5,1.375})
            box(Blocks.IRON_BLOCK,CabinGeometry.rotate(new CabinGeometry.Box(x,.25,z,x+.125,3,z+.125),facing),pose,buffers,light);
        box(Blocks.BLACK_CONCRETE,DisplayGeometry.housing(facing),pose,buffers,0xF000F0);
        for(var segment:DisplayGeometry.glyphs(entity.currentFloor(),entity.travelDirection(),facing))
            box(Blocks.LIME_CONCRETE,segment,pose,buffers,0xF000F0);
        pose.popPose();
        super.render(entity,yaw,partial,pose,buffers,light);
    }
}
