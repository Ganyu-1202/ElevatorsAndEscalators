package org.firefly.elevators_escalators.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.firefly.elevators_escalators.ElevatorsEscalators;
import org.firefly.elevators_escalators.ModContent;
import org.firefly.elevators_escalators.client.ElevatorPlatformRenderer;

@EventBusSubscriber(modid=ElevatorsEscalators.MOD_ID, value=Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(ModContent.ELEVATOR_MENU.get(),org.firefly.elevators_escalators.client.ElevatorScreen::new);
    }
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModContent.PLATFORM.get(), ElevatorPlatformRenderer::new);
        ElevatorsEscalators.LOGGER.info("Registered elevator platform renderer (NeoForge RegisterRenderers)");
    }
}
