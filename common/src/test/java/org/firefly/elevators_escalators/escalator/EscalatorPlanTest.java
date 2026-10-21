package org.firefly.elevators_escalators.escalator;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EscalatorPlanTest {
 @Test void exactDistanceRule(){assertEquals(512,EscalatorPlan.distance(BlockPos.ZERO,new BlockPos(256,256,0)));assertNull(EscalatorPlan.error(BlockPos.ZERO,new BlockPos(256,256,0)));assertNotNull(EscalatorPlan.error(BlockPos.ZERO,new BlockPos(257,256,0)));}
 @Test void distanceUsesHorizontalEuclideanNotManhattan(){assertEquals(12,EscalatorPlan.distance(BlockPos.ZERO,new BlockPos(3,7,4)));}
 @Test void rejectsVerticalFlatAndTooSteep(){assertNotNull(EscalatorPlan.error(BlockPos.ZERO,new BlockPos(0,4,0)));assertNotNull(EscalatorPlan.error(BlockPos.ZERO,new BlockPos(4,0,0)));assertNotNull(EscalatorPlan.error(BlockPos.ZERO,new BlockPos(4,5,0)));}
 @Test void endpointOrderDoesNotChangeUpDirection(){var a=new BlockPos(0,10,0);var b=new BlockPos(8,14,-3);assertEquals(EscalatorPlan.build(a,b),EscalatorPlan.build(b,a));}
 @Test void diagonalRasterHasNoGapsOrDuplicates(){var a=new BlockPos(-3,20,5);var b=new BlockPos(12,30,-10);var p=EscalatorPlan.build(a,b);assertEquals(a,p.getFirst().pos());assertEquals(b,p.getLast().pos());assertEquals(p.size(),p.stream().map(EscalatorPlan.Step::pos).distinct().count());for(int i=1;i<p.size();i++){var x=p.get(i-1).pos();var y=p.get(i).pos();assertEquals(1,Math.abs(x.getX()-y.getX())+Math.abs(x.getZ()-y.getZ()));assertTrue(y.getY()-x.getY()>=0&&y.getY()-x.getY()<=1);}}
 @Test void entranceIsFlatAndJoinsOriginalLowerEndpoint(){
  var a=new BlockPos(0,10,0);var b=new BlockPos(8,18,0);
  var original=EscalatorPlan.build(a,b);var result=EscalatorPlan.withEntrance(a,b);
  assertEquals(original.size()+1,result.size());
  assertFalse(result.getFirst().rises());
  assertEquals(a,result.getFirst().pos().relative(result.getFirst().up()));
  assertEquals(original,result.subList(1,result.size()));
  assertEquals(b,result.getLast().pos());
  assertEquals(result,EscalatorPlan.withEntrance(b,a));
 }
 @Test void entranceWorksInAllFourDirections(){
  for(var direction:net.minecraft.core.Direction.Plane.HORIZONTAL){
   var end=BlockPos.ZERO.relative(direction,4).above(4);
   var plan=EscalatorPlan.withEntrance(BlockPos.ZERO,end);
   assertEquals(BlockPos.ZERO.relative(direction.getOpposite()),plan.getFirst().pos());
   assertEquals(direction,plan.getFirst().up());
   assertEquals(EscalatorRamp.surface(1,false),EscalatorRamp.surface(0,plan.get(1).rises()));
  }
 }
 @Test void recommendationsRiseWithLengthAndStayBounded(){assertTrue(EscalatorPlan.recommendedSpeed(BlockPos.ZERO,new BlockPos(100,50,0))>EscalatorPlan.recommendedSpeed(BlockPos.ZERO,new BlockPos(4,2,0)));assertTrue(EscalatorPlan.validSpeed(50));assertTrue(EscalatorPlan.validSpeed(300));assertFalse(EscalatorPlan.validSpeed(301));}
}
