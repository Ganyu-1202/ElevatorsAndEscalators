package org.firefly.elevators_escalators.elevator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SegmentDisplayTest {
    @Test void allDigitsHaveDistinctNonemptySegments() {
        java.util.Set<Integer> masks=new java.util.HashSet<>();
        for(int i=0;i<10;i++) { int mask=SegmentDisplay.digit(i); assertTrue(mask>0 && mask<128); assertTrue(masks.add(mask)); }
    }
    @Test void oneAndEightHaveExpectedSegments() { assertEquals(2,Integer.bitCount(SegmentDisplay.digit(1))); assertEquals(7,Integer.bitCount(SegmentDisplay.digit(8))); }
    @Test void supportsTwoDigitFloorsAndSafeDefault() { assertEquals("1",SegmentDisplay.floor(0)); assertEquals("16",SegmentDisplay.floor(16)); assertEquals("16",SegmentDisplay.floor(999)); }
    @Test void rejectsNonDigits() { assertThrows(IllegalArgumentException.class,()->SegmentDisplay.digit(10)); }
}
