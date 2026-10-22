package org.firefly.elevators_escalators.elevator;

/** Server-side support history, independent of client interpolation and trip length. */
public final class RiderSupport {
    private RiderSupport() {}
    public record Sample(double feet, double surface, int tick, boolean roof) {}

    public static boolean follows(Sample previous, double feet, double surface, double velocityY, int tick) {
        if (previous == null || !Double.isFinite(feet) || !Double.isFinite(surface)
                || !Double.isFinite(velocityY) || velocityY > .08) return false;
        int age = tick - previous.tick();
        // Six ticks covers position interpolation plus packet/tick ordering at 8 blocks/s.
        // Bound both the absolute separation and drift relative to the moving surface.
        return age >= 0 && age <= 6 && Math.abs(feet - surface) <= 2.7
                && Math.abs((feet - previous.feet()) - (surface - previous.surface())) <= .65;
    }
}
