package org.firefly.elevators_escalators.fabric;

import net.fabricmc.api.ModInitializer;
import org.firefly.elevators_escalators.ElevatorsEscalators;

public final class ElevatorsEscalatorsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ElevatorsEscalators.init();
    }
}
