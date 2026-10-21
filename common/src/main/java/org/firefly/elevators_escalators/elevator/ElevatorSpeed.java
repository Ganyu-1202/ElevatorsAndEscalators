package org.firefly.elevators_escalators.elevator;

/** Speed in blocks per second, fixed-point hundredths on the wire/save. */
public final class ElevatorSpeed {
    public static final int MIN=50,MAX=800;
    private ElevatorSpeed() {}
    public static int recommended(int floorCount) {
        // 1-3 floors: 2 blocks/s, then +0.25 per extra floor, capped at 8.
        return Math.min(MAX,200+Math.max(0,Math.min(256,floorCount)-3)*25);
    }
    public static boolean valid(int value) { return value>=MIN && value<=MAX; }
    public static double perTick(int value) { return value/2000.0; }
    public static int fromSlider(double fraction) { return Math.max(MIN,Math.min(MAX,MIN+(int)Math.round(fraction*(MAX-MIN)/25)*25)); }
}
