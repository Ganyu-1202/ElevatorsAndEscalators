package org.firefly.elevators_escalators.escalator;
public final class EscalatorTransport {
 private EscalatorTransport(){}
 public static double liftForClearance(boolean halfClear,boolean fullClear){return halfClear?.5:fullClear?1:0;}
}
