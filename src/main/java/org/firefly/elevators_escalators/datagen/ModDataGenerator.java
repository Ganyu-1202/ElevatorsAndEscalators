package org.firefly.elevators_escalators.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.firefly.elevators_escalators.ElevatorsEscalators;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = ElevatorsEscalators.MODID)
public class ModDataGenerator
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // 添加提供器
        generator.addProvider(event.includeServer(), new ModLootTableProvider(packOutput, lookupProvider));

        generator.addProvider(event.includeServer(), new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModBlockStateProvider(packOutput, existingFileHelper));
    }
}
