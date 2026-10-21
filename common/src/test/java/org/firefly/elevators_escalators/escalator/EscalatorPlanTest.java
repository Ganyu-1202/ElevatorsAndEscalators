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
 @Test void recommendationsRiseWithLengthAndStayBounded(){assertTrue(EscalatorPlan.recommendedSpeed(BlockPos.ZERO,new BlockPos(100,50,0))>EscalatorPlan.recommendedSpeed(BlockPos.ZERO,new BlockPos(4,2,0)));assertTrue(EscalatorPlan.validSpeed(50));assertTrue(EscalatorPlan.validSpeed(300));assertFalse(EscalatorPlan.validSpeed(301));}
}
