package org.firefly.elevators_escalators.escalator;

/** Stateless world-time motion, shared by every loaded segment and both logical sides. */
public record EscalatorMotion(long epoch, double distance, double initialSpeed, double targetSpeed) {
    private static final double ACCELERATION = .003;
    public double speed(double time) {
        double dt=Math.max(0,time-epoch),delta=targetSpeed-initialSpeed;
        return initialSpeed+Math.copySign(Math.min(Math.abs(delta),dt*ACCELERATION),delta);
    }
    public double distanceAt(double time) {
        double dt=Math.max(0,time-epoch),ramp=Math.min(dt,Math.abs(targetSpeed-initialSpeed)/ACCELERATION);
        double acceleration=Math.signum(targetSpeed-initialSpeed)*ACCELERATION;
        return distance+initialSpeed*ramp+.5*acceleration*ramp*ramp+targetSpeed*(dt-ramp);
    }
    public EscalatorMotion retarget(long time,double target) {
        return new EscalatorMotion(time,distanceAt(time),speed(time),target);
    }
}
