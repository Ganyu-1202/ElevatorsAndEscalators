package org.firefly.elevators_escalators.escalator;
import java.util.ArrayList;
import java.util.List;
public final class TreadAnimation {
 public static final double PITCH=.25,GAP=.012,THICKNESS=.035;
 public record Strip(double from,double to){}
 private TreadAnimation(){}
 public static List<Strip> strips(double distance){
  double phase=distance-Math.floor(distance/PITCH)*PITCH;
  List<Strip> result=new ArrayList<>();
  for(int i=-1;i<=4;i++){
   double from=Math.max(0,i*PITCH+phase),to=Math.min(1,(i+1)*PITCH+phase-GAP);
   if(to>from)result.add(new Strip(from,to));
  }
  return List.copyOf(result);
 }
}
