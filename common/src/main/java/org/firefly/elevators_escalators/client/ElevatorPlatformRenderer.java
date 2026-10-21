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
    /** Rotate around Y, matching CabinGeometry exactly (pivot is deck centre). */
    private static double[] rot(double x,double z,net.minecraft.core.Direction d) {
        return switch(d) { case NORTH -> new double[]{-x,-z}; case EAST -> new double[]{z,-x}; case WEST -> new double[]{-z,x}; default -> new double[]{x,z}; };
    }
    private void seg(Block block,double x,double y,double w,double h,net.minecraft.core.Direction facing,PoseStack pose,MultiBufferSource buffers) {
        double[] p=rot(x,-1.337,facing);
        // Segment widths swap for east/west because local X becomes world Z.
        boolean swap=facing.getAxis()==net.minecraft.core.Direction.Axis.X;
        part(block,p[0],y,p[1],swap?h:w,swap?w:h,.012,pose,buffers,0xF000F0);
    }
    @Override public void render(ElevatorPlatformEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++)
            part(Blocks.IRON_BLOCK,x-.5,0,z-.5,1,.25,1,pose,buffers,light);
        var facing=entity.facing();
        for(var b:CabinGeometry.shell(entity.doorsClosed(),facing)) {
            Block material=b.y1()>=3?Blocks.IRON_BLOCK:Blocks.GLASS;
            part(material,b.x1(),b.y1(),b.z1(),b.x2()-b.x1(),b.y2()-b.y1(),b.z2()-b.z1(),pose,buffers,light);
        }
        part(Blocks.SEA_LANTERN,-.5,2.92,-.5,1,.08,1,pose,buffers,0xF000F0);
        double[][] corners={{-1.5,-1.5},{1.375,-1.5},{-1.5,1.375},{1.375,1.375}};
        for(double[] c:corners) { double[] p=rot(c[0],c[1],facing); part(Blocks.IRON_BLOCK,p[0],.25,p[1],.125,2.75,.125,pose,buffers,light); }
        // Back-wall housing, rotated with the cabin.
        double[] housing=rot(0,-1.374,facing);
        boolean swap=facing.getAxis()==net.minecraft.core.Direction.Axis.X;
        part(Blocks.BLACK_CONCRETE,housing[0]-.42,2.10,housing[1]-.0125,swap?.025:.84,.36,swap?.84:.025,pose,buffers,0xF000F0);
        String number=SegmentDisplay.floor(entity.currentFloor());
        double[][] bars={{.02,.24,.10,.018},{.12,.13,.018,.11},{.12,.02,.018,.11},{.02,0,.10,.018},{0,.02,.018,.11},{0,.13,.018,.11},{.02,.12,.10,.018}};
        double start=-.26;
        for(int i=0;i<number.length();i++) {
            int mask=SegmentDisplay.digit(number.charAt(i)-'0');
            for(int s=0;s<7;s++) if((mask&(1<<s))!=0) { double[] b=bars[s]; seg(Blocks.LIME_CONCRETE,start+i*.17+b[0],2.15+b[1],b[2],b[3],facing,pose,buffers); }
        }
        double fx=.12;
        seg(Blocks.LIME_CONCRETE,fx,2.15,.018,.258,facing,pose,buffers);
        seg(Blocks.LIME_CONCRETE,fx,2.39,.11,.018,facing,pose,buffers);
        seg(Blocks.LIME_CONCRETE,fx,2.27,.09,.018,facing,pose,buffers);
        int direction=entity.travelDirection();
        if(direction!=0) for(int i=0;i<3;i++) {
            double y=direction>0?2.34-i*.035:2.21+i*.035;
            seg(Blocks.LIME_CONCRETE,.30-i*.025,y,.025,.025,facing,pose,buffers);
            seg(Blocks.LIME_CONCRETE,.30+i*.025,y,.025,.025,facing,pose,buffers);
        }
        super.render(entity,yaw,partial,pose,buffers,light);
    }
}
