package org.firefly.elevators_escalators;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.firefly.elevators_escalators.elevator.ElevatorManualItem;
import org.firefly.elevators_escalators.elevator.HomeElevatorBlueprintItem;
import org.firefly.elevators_escalators.elevator.HomeElevatorCarriageBlockItem;

import static org.firefly.elevators_escalators.ElevatorsEscalators.*;

import java.util.Objects;

public class EnrollItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Objects.requireNonNull(Registries.CREATIVE_MODE_TAB), MODID);

    public static final DeferredItem<Item> MOD_ICON_ITEM = ITEMS.register("mod_icon", () ->
            new Item(Objects.requireNonNull(new Item.Properties().stacksTo(1))));

    public static final DeferredItem<Item> ELEVATOR_DOOR_ITEM = ITEMS.register("elevator_door", () ->
            new BlockItem(Objects.requireNonNull(EnrollBlocks.ELEVATOR_DOOR_BLOCK.get()), new Item.Properties()));

    public static final DeferredItem<Item> OUTER_CONTROL_PANEL_ITEM = ITEMS.register("outer_control_panel", () ->
            new BlockItem(Objects.requireNonNull(EnrollBlocks.OUTER_CONTROL_PANEL_BLOCK.get()), new Item.Properties()));
    public static final DeferredItem<Item> INNER_CONTROL_PANEL_ITEM = ITEMS.register("inner_control_panel", () ->
            new BlockItem(Objects.requireNonNull(EnrollBlocks.INNER_CONTROL_PANEL_BLOCK.get()), new Item.Properties()));

    public static final DeferredItem<Item> MAIN_CONTROLLER_ITEM = ITEMS.register("main_controller", () ->
            new BlockItem(Objects.requireNonNull(EnrollBlocks.MAIN_CONTROLLER_BLOCK.get()), new Item.Properties()));

    public static final DeferredItem<Item> HOME_ELEVATOR_FRAME_ITEM = ITEMS.register("home_elevator_frame", () ->
            new BlockItem(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get()), new Item.Properties()));
    public static final DeferredItem<Item> HOME_ELEVATOR_CONTROL_BLOCK_ITEM = ITEMS.register("home_elevator_control_block", () ->
            new BlockItem(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_CONTROL_BLOCK.get()), new Item.Properties()));
    public static final DeferredItem<Item> COMMERCIAL_ELEVATOR_FRAME_ITEM = ITEMS.register("commercial_elevator_frame", () ->
            new BlockItem(Objects.requireNonNull(EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get()), new Item.Properties()));
    public static final DeferredItem<Item> INDUSTRIAL_ELEVATOR_FRAME_ITEM = ITEMS.register("industrial_elevator_frame", () ->
            new BlockItem(Objects.requireNonNull(EnrollBlocks.INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get()), new Item.Properties()));
    public static final DeferredItem<Item> FIRE_FIGHTING_ELEVATOR_FRAME_ITEM = ITEMS.register("fire_fighting_elevator_frame", () ->
            new BlockItem(Objects.requireNonNull(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get()), new Item.Properties()));
    public static final DeferredItem<Item> HOME_ELEVATOR_CARRIAGE_ITEM = ITEMS.register("home_elevator_carriage", () ->
            new HomeElevatorCarriageBlockItem(Objects.requireNonNull(EnrollBlocks.HOME_ELEVATOR_CARRIAGE_BLOCK.get()), Objects.requireNonNull(new Item.Properties().stacksTo(1))));

    public static final DeferredItem<Item> ELEVATOR_POSITION_FRAME_ITEM = ITEMS.register("elevator_position_frame", () ->
            new TipsBlockItem(Objects.requireNonNull(EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()), new Item.Properties(),
                    "block.elevators_escalators.elevator_position_frame.tooltip"));
    public static final DeferredItem<Item> FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_ITEM = ITEMS.register("fire_fighting_elevator_position_frame", () ->
            new TipsBlockItem(Objects.requireNonNull(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get()), new Item.Properties(),
                    "block.elevators_escalators.fire_fighting_elevator_position_frame.tooltip"));

    public static final DeferredItem<Item> ELEVATOR_MANUAL_ITEM = ITEMS.register("elevator_manual", () ->
            new ElevatorManualItem(Objects.requireNonNull(new Item.Properties().stacksTo(1))));

    public static final DeferredItem<Item> HOME_ELEVATOR_BLUEPRINT_ITEM = ITEMS.register("home_elevator_blueprint", () ->
            new HomeElevatorBlueprintItem(Objects.requireNonNull(new Item.Properties().stacksTo(1))));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ELEVATORS_TAB = CREATIVE_MODE_TABS.register("elevators_tab", () ->
            CreativeModeTab.builder()
                    .title(Objects.requireNonNull(Component.translatable("creative_tabs.elevators_escalators.tab_title")))
                    .icon(() -> MOD_ICON_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) ->
                    {
                        output.accept(Objects.requireNonNull(ELEVATOR_DOOR_ITEM.get()));
                        output.accept(Objects.requireNonNull(OUTER_CONTROL_PANEL_ITEM.get()));
                        output.accept(Objects.requireNonNull(INNER_CONTROL_PANEL_ITEM.get()));
                        output.accept(Objects.requireNonNull(MAIN_CONTROLLER_ITEM.get()));
                        output.accept(Objects.requireNonNull(HOME_ELEVATOR_CONTROL_BLOCK_ITEM.get()));
                        output.accept(Objects.requireNonNull(HOME_ELEVATOR_FRAME_ITEM.get()));
                        output.accept(Objects.requireNonNull(COMMERCIAL_ELEVATOR_FRAME_ITEM.get()));
                        output.accept(Objects.requireNonNull(INDUSTRIAL_ELEVATOR_FRAME_ITEM.get()));
                        output.accept(Objects.requireNonNull(FIRE_FIGHTING_ELEVATOR_FRAME_ITEM.get()));
                        output.accept(Objects.requireNonNull(ELEVATOR_POSITION_FRAME_ITEM.get()));
                        output.accept(Objects.requireNonNull(FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_ITEM.get()));
                        output.accept(Objects.requireNonNull(ELEVATOR_MANUAL_ITEM.get()));
                        output.accept(Objects.requireNonNull(HOME_ELEVATOR_BLUEPRINT_ITEM.get()));
                        output.accept(Objects.requireNonNull(HOME_ELEVATOR_CARRIAGE_ITEM.get()));
                    })
                    .build()
    );
}
