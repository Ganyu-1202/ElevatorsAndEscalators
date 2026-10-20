package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FloorDisplayTest {
    @Test void identifiesStops() { int[] f={0,4,8}; assertEquals(1,FloorDisplay.number(f,0)); assertEquals(2,FloorDisplay.number(f,4)); assertEquals(3,FloorDisplay.number(f,8)); }
    @Test void displaysLowerPassedFloorBetweenStops() { assertEquals(2,FloorDisplay.number(new int[]{0,4,8},6)); }
    @Test void customFloorsUseOrdinalNumbers() { assertEquals(3,FloorDisplay.number(new int[]{0,3,7,12},7)); }
}
