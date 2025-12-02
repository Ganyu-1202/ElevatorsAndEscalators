package org.firefly.elevators_escalators.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.firefly.elevators_escalators.EnrollBlocks;
import org.firefly.elevators_escalators.ElevatorsEscalators;

import java.util.Objects;

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
        tag(Objects.requireNonNull(BlockTags.NEEDS_STONE_TOOL))
                .add(Objects.requireNonNull(EnrollBlocks.MAIN_CONTROLLER_BLOCK.get()));

        // 需要铁制工具
        tag(Objects.requireNonNull(BlockTags.NEEDS_IRON_TOOL))
                .add(Objects.requireNonNull(EnrollBlocks.ELEVATOR_DOOR_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_CONTROL_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get()));

        // 需要钻石制工具
        tag(Objects.requireNonNull(BlockTags.NEEDS_DIAMOND_TOOL))
                .add(Objects.requireNonNull(EnrollBlocks.INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get()));

        // 镐开采
        tag(Objects.requireNonNull(BlockTags.MINEABLE_WITH_PICKAXE))
                .add(Objects.requireNonNull(EnrollBlocks.ELEVATOR_DOOR_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.OUTER_CONTROL_PANEL_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.INNER_CONTROL_PANEL_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.MAIN_CONTROLLER_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_CONTROL_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()))
                .add(Objects.requireNonNull(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get()));
    }
}
