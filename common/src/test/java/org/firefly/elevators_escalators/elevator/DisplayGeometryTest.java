package org.firefly.elevators_escalators.elevator;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DisplayGeometryTest {
    private static double volume(CabinGeometry.Box b) { return (b.x2()-b.x1())*(b.y2()-b.y1())*(b.z2()-b.z1()); }
    private static double front(CabinGeometry.Box b,Direction facing) {
        return ((b.x1()+b.x2())*.5)*facing.getStepX()+((b.z1()+b.z2())*.5)*facing.getStepZ();
    }
    @Test void rotationPreservesHeightWidthDepthAndVolume() {
        for(var d:Direction.Plane.HORIZONTAL) {
            var b=DisplayGeometry.segment(.15,2.15,.018,.258,d);
            assertEquals(.258,b.y2()-b.y1(),1e-9);
            assertEquals(.018*.258*.012,volume(b),1e-12);
            assertEquals(.012,d.getAxis()==Direction.Axis.X?b.x2()-b.x1():b.z2()-b.z1(),1e-9);
        }
    }
    @Test void glyphsRemainInFrontOfHousingInEveryDirection() {
        for(var d:Direction.Plane.HORIZONTAL) {
            var h=DisplayGeometry.housing(d);
            for(var b:DisplayGeometry.glyphs(16,1,d)) {
                assertTrue(front(b,d)>front(h,d)+.01,d.name());
                assertTrue(b.y1()>=h.y1() && b.y2()<=h.y2(),d.name());
                double horizontal=d.getAxis()==Direction.Axis.X?(b.z2()+b.z1())*.5:(b.x2()+b.x1())*.5;
                assertTrue(Math.abs(horizontal)<.42,d.name());
            }
        }
    }
    @Test void localRightAxisRotatesInsteadOfMirroringGlyphOrder() {
        for(var d:Direction.Plane.HORIZONTAL) {
            var a=DisplayGeometry.segment(-.2,2,.02,.1,d);
            var b=DisplayGeometry.segment(.2,2,.02,.1,d);
            double dx=(b.x1()+b.x2()-a.x1()-a.x2())/2;
            double dz=(b.z1()+b.z2()-a.z1()-a.z2())/2;
            assertEquals(.4,dx*d.getStepZ()-dz*d.getStepX(),1e-9);
        }
    }
    @Test void everyFloorAndArrowHasValidNonzeroGeometry() {
        for(int f=1;f<=16;f++) for(int direction=-1;direction<=1;direction++) for(var d:Direction.Plane.HORIZONTAL)
            for(var b:DisplayGeometry.glyphs(f,direction,d)) assertTrue(volume(b)>0);
    }
}
