package org.firefly.elevators_escalators.elevator;

/** Shared numerical tolerances; never attach an entity jumping away from the deck. */
public final class DeckSupport {
    private DeckSupport() {}
    public static boolean supported(double feet, double deckTop, double velocityY) {
        return Double.isFinite(feet) && Double.isFinite(deckTop) && Double.isFinite(velocityY)
            && feet >= deckTop - 0.30 && feet <= deckTop + 0.12 && velocityY <= 0.08;
    }
    public static boolean recordable(double feet, double top, double velocityY) {
        // Server player positions lag behind the client-interpolated deck.
        return Double.isFinite(feet) && Double.isFinite(top) && Double.isFinite(velocityY)
            && feet>=top-0.65 && feet<=top+0.15 && velocityY<=0.08;
    }
    public static double carryDelta(double feet, double oldTop, double newTop) {
        double delta=newTop-oldTop;
        // A predicted player can already be above an ascending deck. Never snap
        // them down on the first interpolation updates just to remove that gap.
        return delta>=0 ? Math.max(0,newTop-Math.max(feet,oldTop)) : delta;
    }
}
