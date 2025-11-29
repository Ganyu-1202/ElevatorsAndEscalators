package org.firefly.elevators_escalators.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.firefly.elevators_escalators.EnrollBlocks;
import org.firefly.elevators_escalators.ElevatorsEscalators;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider
{
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, ElevatorsEscalators.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@javax.annotation.Nonnull HolderLookup.Provider provider)
    {
        // 需要石制工具
        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(EnrollBlocks.MAIN_CONTROLLER_BLOCK.get());

        // 需要铁制工具
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(EnrollBlocks.ELEVATOR_DOOR_BLOCK.get());

        // 镐开采
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(EnrollBlocks.ELEVATOR_DOOR_BLOCK.get())
                .add(EnrollBlocks.OUTER_CONTROL_PANEL_BLOCK.get())
                .add(EnrollBlocks.INNER_CONTROL_PANEL_BLOCK.get())
                .add(EnrollBlocks.MAIN_CONTROLLER_BLOCK.get());
    }
}
