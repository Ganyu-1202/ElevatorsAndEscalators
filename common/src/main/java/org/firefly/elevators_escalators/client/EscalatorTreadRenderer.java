package org.firefly.elevators_escalators.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.firefly.elevators_escalators.escalator.*;

/** Continuous belt geometry: no per-slice block model dispatch or internal cube faces. */
public final class EscalatorTreadRenderer implements BlockEntityRenderer<EscalatorStepBlockEntity> {
    private static final ResourceLocation METAL = ResourceLocation.fromNamespaceAndPath("elevators_escalators","textures/block/escalator_metal.png");
    // Lightmap-only shading: no camera-relative entity diffuse light on the belt.
    private static final RenderType BELT = RenderType.create("escalator_belt",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,1536,
        false,false,RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(METAL,false,false))
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .createCompositeState(false));
    private static final ResourceLocation GLASS = ResourceLocation.withDefaultNamespace("textures/block/white_stained_glass.png");
    private static final RenderType SIDE_GLASS = RenderType.create("escalator_glass",DefaultVertexFormat.NEW_ENTITY,VertexFormat.Mode.QUADS,1536,
        false,true,RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(GLASS,false,false))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.CULL)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            // Keep LEQUAL depth testing: walls/rails still occlude glass. Glass cannot hide later rails.
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .createCompositeState(false));
    public EscalatorTreadRenderer(BlockEntityRendererProvider.Context context) {}

    // The incline reaches y=1.5 and its handrail y=2.41, outside the owning block.
    // Both vanilla and NeoForge must skip the default one-block frustum test.
    // Normal BER distance checks still apply; do not increase the view distance.
    @Override public boolean shouldRenderOffScreen(EscalatorStepBlockEntity be) { return true; }

    private static void vertex(VertexConsumer out, PoseStack.Pose pose, int light, float shade, float alpha,
                               double x, double y, double z, float u, float v, float nx, float ny, float nz) {
        out.addVertex(pose, (float)x, (float)y, (float)z).setColor(shade,shade,shade,alpha)
            .setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,nx,ny,nz);
    }
    private static void quad(VertexConsumer out, PoseStack.Pose pose, int light, float shade, float alpha,
                             double ax,double ay,double az, double bx,double by,double bz,
                             double cx,double cy,double cz, double dx,double dy,double dz) {
        double nx=(by-ay)*(cz-az)-(bz-az)*(cy-ay);
        double ny=(bz-az)*(cx-ax)-(bx-ax)*(cz-az);
        double nz=(bx-ax)*(cy-ay)-(by-ay)*(cx-ax);
        double length=Math.sqrt(nx*nx+ny*ny+nz*nz);
        if(length<1e-9)return;
        float x=(float)(nx/length),y=(float)(ny/length),z=(float)(nz/length);
        vertex(out,pose,light,shade,alpha,ax,ay,az,0,0,x,y,z);
        vertex(out,pose,light,shade,alpha,bx,by,bz,0,1,x,y,z);
        vertex(out,pose,light,shade,alpha,cx,cy,cz,1,1,x,y,z);
        vertex(out,pose,light,shade,alpha,dx,dy,dz,1,0,x,y,z);
    }
    // Six outward-facing quads; bottom/top can follow different inclines (wedge or rail).
    private static void prism(VertexConsumer out, PoseStack.Pose p,int light,float shade,float alpha,
                              double x0,double x1,double z0,double z1,double b0,double b1,double t0,double t1) {
        quad(out,p,light,shade,alpha,x0,t0,z0,x0,t1,z1,x1,t1,z1,x1,t0,z0);
        quad(out,p,light,shade,alpha,x0,b0,z0,x1,b0,z0,x1,b1,z1,x0,b1,z1);
        quad(out,p,light,shade,alpha,x0,b0,z0,x0,b1,z1,x0,t1,z1,x0,t0,z0);
        quad(out,p,light,shade,alpha,x1,b0,z0,x1,t0,z0,x1,t1,z1,x1,b1,z1);
        quad(out,p,light,shade,alpha,x0,b0,z0,x0,t0,z0,x1,t0,z0,x1,b0,z0);
        quad(out,p,light,shade,alpha,x0,b1,z1,x1,b1,z1,x1,t1,z1,x0,t1,z1);
    }
    @Override public void render(EscalatorStepBlockEntity be,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var state=be.getBlockState();
        if(!state.getValue(EscalatorBlock.ASSEMBLED))return;
        var controller=be.controller();
        double distance=controller==null?0:controller.animationDistance(partial);
        boolean rising=state.getValue(EscalatorBlock.RISING);
        double low=EscalatorRamp.surface(0,rising), high=EscalatorRamp.surface(1,rising);
        pose.pushPose();pose.translate(.5,0,.5);
        float angle=switch(state.getValue(EscalatorBlock.FACING)){case NORTH->180;case EAST->90;case WEST->-90;default->0;};
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));pose.translate(-.5,0,-.5);
        var p=pose.last();
        var solid=buffers.getBuffer(BELT);
        prism(solid,p,light,.7f,1,0,1,0,1,0,0,low-.02,high-.02);
        // Each moving tread is one inclined quad, including clipped treads at segment joins.
        for(var strip:TreadAnimation.strips(distance)) {
            double a=strip.from(),b=strip.to();
            double ya=EscalatorRamp.surface(a,rising),yb=EscalatorRamp.surface(b,rising);
            quad(solid,p,light,1,1,.055,ya,a,.055,yb,b,.945,yb,b,.945,ya,a);
        }
        // Slim dark rubber caps, with a 0.86-block clear central passage.
        for(int side=0;side<2;side++) {
            double x=side==0?.015:.93;
            prism(solid,p,light,.38f,1,x,x+.055,0,1,low+.85,high+.85,low+.91,high+.91);
        }
        var glass=buffers.getBuffer(SIDE_GLASS);
        for(int side=0;side<2;side++) {
            double x=side==0?.03:.945;
            prism(glass,p,light,1,.28f,x,x+.025,0,1,low+.02,high+.02,low+.85,high+.85);
        }
        pose.popPose();
    }
}
