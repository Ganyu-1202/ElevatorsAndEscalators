package org.firefly.elevators_escalators.client;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import org.firefly.elevators_escalators.ModContent;

public final class ModClient {
    public static void init() { EntityRendererRegistry.register(ModContent.PLATFORM, ElevatorPlatformRenderer::new); registerScreens(); }
    public static void registerScreens() {
        dev.architectury.registry.menu.MenuRegistry.registerScreenFactory(ModContent.ESC_MENU.get(),EscalatorScreen::new);
        dev.architectury.registry.menu.MenuRegistry.registerScreenFactory(ModContent.ELEVATOR_MENU.get(),ElevatorScreen::new);
        dev.architectury.registry.menu.MenuRegistry.registerScreenFactory(ModContent.SETUP_MENU.get(),SetupScreen::new);
    }
    private ModClient() {}
}
