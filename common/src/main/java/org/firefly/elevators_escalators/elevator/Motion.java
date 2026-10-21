package org.firefly.elevators_escalators.elevator;

/** Deterministic, loader-independent movement math. Units: blocks per tick. */
public final class Motion {
    public static final double SPEED = 0.10;
    private Motion() {}
    public static double step(double current, double target, double maxStep) {
        if (!Double.isFinite(current) || !Double.isFinite(target) || !Double.isFinite(maxStep) || maxStep <= 0)
            throw new IllegalArgumentException("Motion requires finite values and a positive speed");
        double distance = target - current;
        return current + Math.copySign(Math.min(Math.abs(distance), maxStep), distance);
    }
    public static double approachSpeed(double currentSpeed, double remaining) { return approachSpeed(currentSpeed,remaining,SPEED); }
    public static double approachSpeed(double currentSpeed,double remaining,double maximumSpeed) {
        if (!Double.isFinite(currentSpeed) || !Double.isFinite(remaining) || !Double.isFinite(maximumSpeed) || maximumSpeed<=0) throw new IllegalArgumentException("Non-finite motion");
        if (Math.abs(remaining)<1e-6) return 0;
        if (Math.signum(currentSpeed)!=Math.signum(remaining)) currentSpeed=0;
        double desired=Math.copySign(Math.min(maximumSpeed,Math.sqrt(2*0.01*Math.abs(remaining))),remaining);
        return currentSpeed+Math.max(-0.01,Math.min(0.01,desired-currentSpeed));
    }
    public static boolean validHeight(int height) { return height >= 4 && height <= 32; }
    public static int nextHeight(int height) { return height >= 32 ? 4 : height + 4; }
}
