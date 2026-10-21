package org.firefly.elevators_escalators.elevator;

/** Bits: top, upper-right, lower-right, bottom, lower-left, upper-left, middle. */
public final class SegmentDisplay {
    private static final int[] DIGITS={0x3F,0x06,0x5B,0x4F,0x66,0x6D,0x7D,0x07,0x7F,0x6F};
    private SegmentDisplay() {}
    public static int digit(int value) {
        if(value<0 || value>9) throw new IllegalArgumentException("Not a decimal digit");
        return DIGITS[value];
    }
    public static String floor(int number) { return Integer.toString(Math.max(-32767,Math.min(32767,number))); }
}
