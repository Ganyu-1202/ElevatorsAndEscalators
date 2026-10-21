package org.firefly.elevators_escalators.escalator;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EscalatorRampTest{
 @Test void risingSegmentJoinsNextBlockWithoutHeightGap(){assertEquals(1+EscalatorRamp.surface(0,true),EscalatorRamp.surface(1,true),1e-9);}
 @Test void flatSurfaceDoesNotRise(){assertEquals(.5,EscalatorRamp.surface(0,false));assertEquals(.5,EscalatorRamp.surface(1,false));}
 @Test void slopeFollowsAllDirections(){assertEquals(.8,EscalatorRamp.progress(.2,.8,Direction.SOUTH),1e-9);assertEquals(.2,EscalatorRamp.progress(.2,.8,Direction.NORTH),1e-9);assertEquals(.2,EscalatorRamp.progress(.2,.8,Direction.EAST),1e-9);assertEquals(.8,EscalatorRamp.progress(.2,.8,Direction.WEST),1e-9);}
 @Test void footprintStartsClimbingBeforeItsCentreLeavesLanding(){
  var path=EscalatorPlan.withEntrance(net.minecraft.core.BlockPos.ZERO,new net.minecraft.core.BlockPos(4,4,0));
  // Centre x=-.1 is still on the landing; its front reaches x=.2 on the incline.
  double height=EscalatorRamp.supportHeight(path,0,-.4,.2,.2,.8);
  assertEquals(.5+7.0/32,height,1e-9);
  assertTrue(height>.5);
 }
 @Test void supportTracksAllFourRampDirections(){
  for(var d:Direction.Plane.HORIZONTAL){
   var path=EscalatorPlan.withEntrance(net.minecraft.core.BlockPos.ZERO,net.minecraft.core.BlockPos.ZERO.relative(d,4).above(4));
   double x=.5-d.getStepX()*.6,z=.5-d.getStepZ()*.6;
   assertEquals(.5+7.0/32,EscalatorRamp.supportHeight(path,0,x-.3,z-.3,x+.3,z+.3),1e-9);
  }
 }
 @Test void footprintSettlesBackToFlatLanding(){
  var path=EscalatorPlan.withEntrance(net.minecraft.core.BlockPos.ZERO,new net.minecraft.core.BlockPos(4,4,0));
  assertEquals(.5,EscalatorRamp.supportHeight(path,1,-.8,.2,-.2,.8));
 }
 @Test void movementHasNoHalfBlockJump(){assertEquals(.05,EscalatorRamp.vertical(.05,true));assertEquals(-.05,EscalatorRamp.vertical(-.05,true));assertEquals(0,EscalatorRamp.vertical(.05,false));assertTrue(1.0/EscalatorRamp.SLICES<.04);}
}
