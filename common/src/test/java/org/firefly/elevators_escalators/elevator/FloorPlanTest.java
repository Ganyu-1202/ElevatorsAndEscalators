package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FloorPlanTest {
    @Test void migratesOldTwoStopData() { assertArrayEquals(new int[]{0,8},FloorPlan.sanitize(new int[0],8)); }
    @Test void sortsDeduplicatesAndRejectsInvalidValues() { assertArrayEquals(new int[]{0,4,8},FloorPlan.sanitize(new int[]{8,4,4,-1,99},8)); }
    @Test void defaultEightBlockShaftIncludesMiddleStop() {
        assertArrayEquals(new int[]{0,4,8},FloorPlan.defaults(8));
        assertArrayEquals(new int[]{0,4,8},FloorPlan.load(new int[]{0,8},8));
        assertArrayEquals(new int[]{0,4,8},FloorPlan.load(new int[0],8));
    }
    @Test void migrationPreservesCustomStops() {
        assertArrayEquals(new int[]{0,3,8},FloorPlan.load(new int[]{0,3,8},8));
        assertArrayEquals(new int[]{0,4},FloorPlan.defaults(4));
        assertEquals(9,FloorPlan.defaults(32).length);
    }
    @Test void addsIntermediateFloor() { assertArrayEquals(new int[]{0,4,8},FloorPlan.add(new int[]{0,8},4,8)); }
    @Test void oversizedSavedDataKeepsBothEndpoints() {
        int[] p=FloorPlan.sanitize(java.util.stream.IntStream.range(0,33).toArray(),32);
        assertEquals(16,p.length); assertEquals(0,p[0]); assertEquals(32,p[p.length-1]);
    }
    @Test void cyclesBothDirections() { int[] p={0,4,8}; assertEquals(4,FloorPlan.next(p,0,false)); assertEquals(0,FloorPlan.next(p,8,false)); assertEquals(8,FloorPlan.next(p,0,true)); }
    @Test void rejectsOutOfRangeAndTooManyStops() {
        assertThrows(IllegalArgumentException.class,()->FloorPlan.add(new int[]{0,8},9,8));
        int[] p=java.util.stream.IntStream.range(0,16).toArray();
        assertThrows(IllegalArgumentException.class,()->FloorPlan.add(p,17,32));
    }
}
