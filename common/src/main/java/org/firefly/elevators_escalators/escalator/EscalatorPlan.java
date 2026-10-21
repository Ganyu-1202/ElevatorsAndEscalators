package org.firefly.elevators_escalators.escalator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.*;

/** One-block-wide cardinal grid route; diagonal endpoints use alternating cardinal runs. */
public final class EscalatorPlan {
    public record Step(BlockPos pos,Direction up,boolean rises) {}
    private EscalatorPlan() {}
    public static double distance(BlockPos a,BlockPos b){return Math.hypot((double)b.getX()-a.getX(),(double)b.getZ()-a.getZ())+Math.abs((double)b.getY()-a.getY());}
    public static String error(BlockPos a,BlockPos b){
        if(distance(a,b)>512.0000001)return "水平距离＋高度差不能超过512格 / Distance exceeds 512";
        long dx=Math.abs((long)b.getX()-a.getX()),dz=Math.abs((long)b.getZ()-a.getZ()),dy=Math.abs((long)b.getY()-a.getY());
        if(dx+dz==0)return "起终点必须有水平距离 / Horizontal span required";
        if(dy==0)return "自动扶梯需要高度差 / Height difference required";
        if(dy>dx+dz)return "坡度太陡：每水平步最多升高1格 / Slope too steep";
        return null;
    }
    public static List<Step> build(BlockPos first,BlockPos second){
        String err=error(first,second);if(err!=null)throw new IllegalArgumentException(err);
        BlockPos a=first.getY()<second.getY()?first:second,b=first.getY()<second.getY()?second:first;
        int dx=b.getX()-a.getX(),dz=b.getZ()-a.getZ(),nx=Math.abs(dx),nz=Math.abs(dz),n=nx+nz,dy=b.getY()-a.getY();
        List<BlockPos> points=new ArrayList<>();int x=a.getX(),z=a.getZ(),usedX=0,usedZ=0;
        points.add(a);
        for(int i=1;i<=n;i++){
            // Balanced Manhattan rasterization; each neighbor is horizontally face-adjacent.
            boolean moveX=usedX<nx&&(usedZ>=nz||(long)(usedX+1)*nz<=(long)(usedZ+1)*nx);
            if(moveX){x+=Integer.signum(dx);usedX++;}else{z+=Integer.signum(dz);usedZ++;}
            points.add(new BlockPos(x,a.getY()+(int)((long)i*dy/n),z));
        }
        List<Step> steps=new ArrayList<>();
        for(int i=0;i<points.size();i++){
            BlockPos p=points.get(i),q=points.get(Math.min(i+1,points.size()-1));
            Direction direction=i==points.size()-1?steps.get(i-1).up():Direction.fromDelta(q.getX()-p.getX(),0,q.getZ()-p.getZ());
            steps.add(new Step(p,direction,q.getY()>p.getY()));
        }
        return List.copyOf(steps);
    }
    /** Preserve selected endpoints and incline; reserve one extra cell before the lower endpoint. */
    public static List<Step> withEntrance(BlockPos first,BlockPos second){
        List<Step> incline=build(first,second);
        Step low=incline.getFirst();
        List<Step> result=new ArrayList<>(incline.size()+1);
        result.add(new Step(low.pos().relative(low.up().getOpposite()),low.up(),false));
        result.addAll(incline);
        return List.copyOf(result);
    }
    public static int recommendedSpeed(BlockPos a,BlockPos b){return Math.min(250,75+(int)Math.floor(distance(a,b)/16)*10);}
    public static boolean validSpeed(int hundredths){return hundredths>=50&&hundredths<=300;}
}
