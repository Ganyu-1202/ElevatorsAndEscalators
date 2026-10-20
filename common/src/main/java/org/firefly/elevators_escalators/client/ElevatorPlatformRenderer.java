package org.firefly.elevators_escalators.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.firefly.elevators_escalators.elevator.CabinGeometry;
import org.firefly.elevators_escalators.elevator.ElevatorPlatformEntity;

public final class ElevatorPlatformRenderer extends EntityRenderer<ElevatorPlatformEntity> {
    public ElevatorPlatformRenderer(EntityRendererProvider.Context context) { super(context); shadowRadius=1.5F; }
    @Override public ResourceLocation getTextureLocation(ElevatorPlatformEntity entity) { return TextureAtlas.LOCATION_BLOCKS; }
    private void part(Block block,double x,double y,double z,double w,double h,double d,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose(); pose.translate(x,y,z); pose.scale((float)w,(float)h,(float)d);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(),pose,buffers,light,OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
    @Override public void render(ElevatorPlatformEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++)
            part(Blocks.IRON_BLOCK,x-.5,0,z-.5,1,.25,1,pose,buffers,light);
        for(var b:CabinGeometry.shell(entity.doorsClosed())) {
            Block material=b.y1()>=3?Blocks.IRON_BLOCK:Blocks.GLASS;
            part(material,b.x1(),b.y1(),b.z1(),b.x2()-b.x1(),b.y2()-b.y1(),b.z2()-b.z1(),pose,buffers,light);
        }
        // Visible emissive fixture; no fake claim of lighting surrounding entities.
        part(Blocks.SEA_LANTERN,-.5,2.92,-.5,1,.08,1,pose,buffers,0xF000F0);
        // Four slim steel corner uprights.
        for(double x:new double[]{-1.5,1.375}) for(double z:new double[]{-1.5,1.375})
            part(Blocks.IRON_BLOCK,x,.25,z,.125,2.75,.125,pose,buffers,light);
        // Back-wall display faces the cabin interior (+Z), full-bright text.
        pose.pushPose(); pose.translate(0,2.35,-1.365); pose.scale(.012F,-.012F,.012F);
        String label=entity.currentFloor()+"F"+(entity.travelDirection()>0?" ^":entity.travelDirection()<0?" v":"");
        var font=Minecraft.getInstance().font;
        font.drawInBatch(label,-font.width(label)/2F,0,0x66FFAA,false,pose.last().pose(),buffers,
            net.minecraft.client.gui.Font.DisplayMode.NORMAL,0xFF102020,0xF000F0);
        pose.popPose();
        super.render(entity,yaw,partial,pose,buffers,light);
    }
}
