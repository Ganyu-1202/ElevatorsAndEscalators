package org.firefly.elevators_escalators.elevator;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import static org.junit.jupiter.api.Assertions.*;

class CabinBlockTest {
    @Test void looseCabinInheritsOrdinaryMiningInsteadOfZeroProgress() throws Exception {
        var node=new ClassNode();
        try(var input=getClass().getResourceAsStream("/org/firefly/elevators_escalators/elevator/CabinBlock.class")){
            assertNotNull(input);new ClassReader(input).accept(node,0);
        }
        assertEquals("net/minecraft/world/level/block/Block",node.superName);
        assertFalse(node.methods.stream().anyMatch(m->m.name.equals("getDestroyProgress")));
    }
    @Test void entryPointDoesNotProtectLooseCabinViaBreakEvent() throws Exception {
        var node=new ClassNode();
        try(var input=getClass().getResourceAsStream("/org/firefly/elevators_escalators/ElevatorsEscalators.class")) {
            assertNotNull(input);new ClassReader(input).accept(node,0);
        }
        for(var method:node.methods) for(var instruction:method.instructions)
            if(instruction instanceof org.objectweb.asm.tree.FieldInsnNode field)
                assertFalse(field.owner.endsWith("/ModContent")&&field.name.equals("CABIN"));
    }
    @Test void looseCabinRetainsSelfDropLootTable() throws Exception {
        try(var input=getClass().getResourceAsStream("/data/elevators_escalators/loot_table/blocks/cabin.json")){
            assertNotNull(input);
            assertTrue(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).contains("elevators_escalators:cabin"));
        }
    }
}
