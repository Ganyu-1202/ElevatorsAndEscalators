package org.firefly.elevators_escalators.escalator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TreadAnimationTest {
 @Test void fourPanelsPerBlockAndThin(){assertEquals(4,TreadAnimation.strips(0).size());assertTrue(TreadAnimation.THICKNESS<.05);}
 @Test void anyPhaseHasBoundedClippedPanels(){for(int i=-100;i<=100;i++){var strips=TreadAnimation.strips(i*.013);double covered=0;for(var s:strips){assertTrue(s.from()>=0&&s.to()<=1&&s.to()>s.from());covered+=s.to()-s.from();}assertTrue(covered>.93);}}
 @Test void animationRepeatsAtOnePitch(){var a=TreadAnimation.strips(.05);var b=TreadAnimation.strips(.30);assertEquals(a.size(),b.size());for(int i=0;i<a.size();i++){assertEquals(a.get(i).from(),b.get(i).from(),1e-9);assertEquals(a.get(i).to(),b.get(i).to(),1e-9);}}
 @Test void reverseChangesPhaseAndStoppingKeepsSameGeometry(){assertNotEquals(TreadAnimation.strips(.04),TreadAnimation.strips(-.04));assertEquals(TreadAnimation.strips(1.25),TreadAnimation.strips(1.25));}
}
