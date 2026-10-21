package org.firefly.elevators_escalators.escalator;
import net.minecraft.core.Direction;
public final class EscalatorRamp {
 public static final int SLICES=32;
 private EscalatorRamp(){}
 public static double progress(double x,double z,Direction d){return Math.max(0,Math.min(1,switch(d){case NORTH->1-z;case EAST->x;case WEST->1-x;default->z;}));}
 public static double surface(double progress,boolean rising){return .5+(rising?Math.max(0,Math.min(1,progress)):0);}
 public static double vertical(double horizontal,boolean rising){return rising?horizontal:0;}
 /** Highest collision slice under a footprint; match the cached 1/32 ramp shape. */
 public static double supportHeight(java.util.List<EscalatorPlan.Step> plan,int index,
                                    double minX,double minZ,double maxX,double maxZ){
  double result=Double.NEGATIVE_INFINITY;
  for(int i=Math.max(0,index-2);i<=Math.min(plan.size()-1,index+2);i++){
   var step=plan.get(i);var p=step.pos();
   if(maxX<=p.getX()+1e-7||minX>=p.getX()+1-1e-7||maxZ<=p.getZ()+1e-7||minZ>=p.getZ()+1-1e-7)continue;
   double edge=switch(step.up()){
    case NORTH->1-(minZ-p.getZ());case EAST->maxX-p.getX();
    case WEST->1-(minX-p.getX());default->maxZ-p.getZ();
   };
   double slice=Math.ceil(Math.max(0,Math.min(1,edge))*SLICES)/SLICES;
   result=Math.max(result,p.getY()+surface(slice,step.rises()));
  }
  return result;
 }
}
