package org.firefly.elevators_escalators.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.firefly.elevators_escalators.EnrollBlocks;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider
{
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries)
    {
    super(output, Set.of(), List.of(
        new SubProviderEntry(ModBlockLootTables::new, Objects.requireNonNull(LootContextParamSets.BLOCK))), registries);
    }

    public static class ModBlockLootTables extends BlockLootSubProvider
    {
        public ModBlockLootTables(HolderLookup.Provider registries)
        {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate()
        {
            // 电梯门方块：使用门专用掉落表，避免上下两半各掉一次
            Block elevatorDoor = Objects.requireNonNull(EnrollBlocks.ELEVATOR_DOOR_BLOCK.get());
            this.add(elevatorDoor, Objects.requireNonNull(createDoorTable(elevatorDoor)));
            // 外侧控制面板方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.OUTER_CONTROL_PANEL_BLOCK.get()));
            // 内侧控制面板方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.INNER_CONTROL_PANEL_BLOCK.get()));
            // 主控制器方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.MAIN_CONTROLLER_BLOCK.get()));
            // 家用电梯控制方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_CONTROL_BLOCK.get()));
            // 家用电梯结构框架方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get()));
            // 商用电梯结构框架方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get()));
            // 工业电梯结构框架方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get()));
            // 消防电梯结构框架方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get()));
            // 电梯定位框架方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()));
            // 消防电梯定位框架方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get()));
            // 家用电梯轿厢方块
            this.dropSelf(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_CARRIAGE_BLOCK.get()));
        }

        @Override
        protected @Nonnull Iterable<Block> getKnownBlocks()
        {
            return Objects.requireNonNull(EnrollBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) Objects.requireNonNull(holder.value())).toList());
        }
    }
}
