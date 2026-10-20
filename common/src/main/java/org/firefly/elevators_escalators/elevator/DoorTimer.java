package org.firefly.elevators_escalators.elevator;

/** Count simulation ticks, not wall-clock time: 100 ticks is 5 seconds at 20 TPS. */
public final class DoorTimer {
    public static final int TIMEOUT=100;
    private int idle;
    public void reset() { idle=0; }
    public boolean tick(boolean open,boolean blocked) {
        if (!open || blocked) { reset(); return false; }
        idle=Math.min(TIMEOUT,idle+1);
        return idle>=TIMEOUT;
    }
}
