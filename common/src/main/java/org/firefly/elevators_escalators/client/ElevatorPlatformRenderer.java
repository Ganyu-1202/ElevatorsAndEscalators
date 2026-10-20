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
    private void digit(int value,double x,PoseStack pose,MultiBufferSource buffers) {
        int mask=org.firefly.elevators_escalators.elevator.SegmentDisplay.digit(value);
        double[][] bars={{.02,.24,.10,.018},{.12,.13,.018,.11},{.12,.02,.018,.11},
            {.02,0,.10,.018},{0,.02,.018,.11},{0,.13,.018,.11},{.02,.12,.10,.018}};
        for(int i=0;i<7;i++) if((mask & (1<<i))!=0) {
            double[] b=bars[i];
            part(Blocks.LIME_CONCRETE,x+b[0],2.15+b[1],-1.337,b[2],b[3],.012,pose,buffers,0xF000F0);
        }
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
        // Separate opaque housing from glyphs: the font background quad must not
        // compete with the text/transparent cabin wall in the depth buffer.
        part(Blocks.BLACK_CONCRETE,-.42,2.10,-1.374,.84,.36,.025,pose,buffers,0xF000F0);
        String number=org.firefly.elevators_escalators.elevator.SegmentDisplay.floor(entity.currentFloor());
        double start=-.26;
        // Viewed from inside (+Z looking north), world +X is screen-right.
        for(int i=0;i<number.length();i++) digit(number.charAt(i)-'0',start+i*.17,pose,buffers);
        // Fixed F label drawn from the same opaque, full-bright geometry.
        double fx=.12;
        part(Blocks.LIME_CONCRETE,fx,2.15,-1.337,.018,.258,.012,pose,buffers,0xF000F0);
        part(Blocks.LIME_CONCRETE,fx,2.39,-1.337,.11,.018,.012,pose,buffers,0xF000F0);
        part(Blocks.LIME_CONCRETE,fx,2.27,-1.337,.09,.018,.012,pose,buffers,0xF000F0);
        int direction=entity.travelDirection();
        if(direction!=0) {
            for(int i=0;i<3;i++) {
                double y=direction>0?2.34-i*.035:2.21+i*.035;
                part(Blocks.LIME_CONCRETE,.30-i*.025,y,-1.337,.025,.025,.012,pose,buffers,0xF000F0);
                part(Blocks.LIME_CONCRETE,.30+i*.025,y,-1.337,.025,.025,.012,pose,buffers,0xF000F0);
            }
        }
        super.render(entity,yaw,partial,pose,buffers,light);
    }
}
