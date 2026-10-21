package org.firefly.elevators_escalators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared entry point. Do not reference client-only classes here. */
public final class ElevatorsEscalators {
    public static final String MOD_ID = "elevators_escalators";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private ElevatorsEscalators() {}

    public static void init() {
        org.firefly.elevators_escalators.elevator.ElevatorLimits.load();
        ModContent.init();
        // Also cancel creative instant breaks; getDestroyProgress alone only blocks survival mining.
        dev.architectury.event.events.common.BlockEvent.BREAK.register((level,pos,state,player,xp) ->
            state.is(ModContent.CABIN.get())||state.is(ModContent.HALL.get())
                ||(state.is(ModContent.ESCALATOR.get())&&state.getValue(org.firefly.elevators_escalators.escalator.EscalatorBlock.ASSEMBLED))
                ? dev.architectury.event.EventResult.interruptFalse() : dev.architectury.event.EventResult.pass());
        dev.architectury.event.events.common.TickEvent.SERVER_LEVEL_POST.register(level ->
            org.firefly.elevators_escalators.escalator.EscalatorNetwork.get(level).tick(level));
        org.firefly.elevators_escalators.escalator.EscalatorPayload.register();
        org.firefly.elevators_escalators.elevator.SetupPayload.register();
        org.firefly.elevators_escalators.elevator.ControlPayload.register();
        dev.architectury.event.events.common.PlayerEvent.PLAYER_QUIT.register(player -> {
            for (var platform : player.level().getEntitiesOfClass(
                    org.firefly.elevators_escalators.elevator.ElevatorPlatformEntity.class,
                    player.getBoundingBox().inflate(0.2,0.8,0.2))) {
                platform.awaitReturningRider(player);
            }
        });
        LOGGER.info("Initializing Elevators and Escalators rewrite");
    }
}
