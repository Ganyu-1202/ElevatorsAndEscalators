package org.firefly.elevators_escalators.fabric;
import net.fabricmc.api.ClientModInitializer;
import org.firefly.elevators_escalators.client.ModClient;
public final class ElevatorsEscalatorsFabricClient implements ClientModInitializer {
    @Override public void onInitializeClient() { ModClient.init(); }
}
