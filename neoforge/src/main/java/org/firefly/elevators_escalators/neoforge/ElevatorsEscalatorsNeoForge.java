package org.firefly.elevators_escalators.neoforge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.firefly.elevators_escalators.ElevatorsEscalators;
@Mod(ElevatorsEscalators.MOD_ID)
public final class ElevatorsEscalatorsNeoForge {
    public ElevatorsEscalatorsNeoForge(IEventBus bus) {
        ElevatorsEscalators.init();
    }
}
