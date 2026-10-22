package org.firefly.elevators_escalators.elevator;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Checks the real 1.21.1 injection contract, not a simulated entity tick loop. */
class ClientTickContractTest {
    private List<String> calls(String type,String method,String descriptor) throws Exception {
        var node=new ClassNode();
        try(var input=getClass().getResourceAsStream("/"+type+".class")) {
            assertNotNull(input);
            new ClassReader(input).accept(node,0);
        }
        var found=node.methods.stream().filter(m->m.name.equals(method)&&m.desc.equals(descriptor)).findFirst().orElseThrow();
        var calls=new ArrayList<String>();
        for(var instruction:found.instructions)if(instruction instanceof MethodInsnNode call)
            calls.add(call.owner+"."+call.name);
        return calls;
    }
    @Test void playerInterpolationOriginIsSavedBeforeOurEntityTickInjection() throws Exception {
        var calls=calls("net/minecraft/client/multiplayer/ClientLevel","tickNonPassenger","(Lnet/minecraft/world/entity/Entity;)V");
        int origin=calls.indexOf("net/minecraft/world/entity/Entity.setOldPosAndRot");
        int tick=calls.indexOf("net/minecraft/world/entity/Entity.tick");
        assertTrue(origin>=0&&tick>origin);
    }
    @Test void carryingBeforePlayerTickAlsoPrecedesPhysicsAndPositionPacket() throws Exception {
        var calls=calls("net/minecraft/client/player/LocalPlayer","tick","()V");
        int physics=calls.indexOf("net/minecraft/client/player/AbstractClientPlayer.tick");
        int packet=calls.indexOf("net/minecraft/client/player/LocalPlayer.sendPosition");
        assertTrue(physics>=0&&packet>physics);
    }
    @Test void clientOnlyMixinIsNotLoadedOnDedicatedServers() throws Exception {
        try(var input=getClass().getResourceAsStream("/elevators_escalators.mixins.json")) {
            assertNotNull(input);
            var json=com.google.gson.JsonParser.parseString(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            assertTrue(json.getAsJsonArray("client").toString().contains("ElevatorClientTickMixin"));
            assertFalse(json.getAsJsonArray("mixins").toString().contains("ElevatorClientTickMixin"));
        }
    }
}
