package org.firefly.elevators_escalators;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static org.firefly.elevators_escalators.ElevatorsEscalators.*;

public class EnrollItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<Item> MOD_ICON_ITEM = ITEMS.register("mod_icon", () ->
            new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ELEVATOR_DOOR_ITEM = ITEMS.register("elevator_door", () ->
            new BlockItem(EnrollBlocks.ELEVATOR_DOOR_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<Item> OUTER_CONTROL_PANEL_ITEM = ITEMS.register("outer_control_panel", () ->
            new BlockItem(EnrollBlocks.OUTER_CONTROL_PANEL_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<Item> INNER_CONTROL_PANEL_ITEM = ITEMS.register("inner_control_panel", () ->
            new BlockItem(EnrollBlocks.INNER_CONTROL_PANEL_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<Item> MAIN_CONTROLLER_ITEM = ITEMS.register("main_controller", () ->
            new BlockItem(EnrollBlocks.MAIN_CONTROLLER_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<Item> HOME_ELEVATOR_FRAME_ITEM = ITEMS.register("home_elevator_frame", () ->
            new BlockItem(EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<Item> COMMERCIAL_ELEVATOR_FRAME_ITEM = ITEMS.register("commercial_elevator_frame", () ->
            new BlockItem(EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<Item> INDUSTRIAL_ELEVATOR_FRAME_ITEM = ITEMS.register("industrial_elevator_frame", () ->
            new BlockItem(EnrollBlocks.INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<Item> FIRE_FIGHTING_ELEVATOR_FRAME_ITEM = ITEMS.register("fire_fighting_elevator_frame", () ->
            new BlockItem(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get(), new Item.Properties()));

    public static final DeferredItem<Item> ELEVATOR_POSITION_FRAME_ITEM = ITEMS.register("elevator_position_frame", () ->
            new TipsBlockItem(EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get(), new Item.Properties(),
                    "block.elevators_escalators.elevator_position_frame.tooltip"));
    public static final DeferredItem<Item> FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_ITEM = ITEMS.register("fire_fighting_elevator_position_frame", () ->
            new TipsBlockItem(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get(), new Item.Properties(),
                    "block.elevators_escalators.fire_fighting_elevator_position_frame.tooltip"));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ELEVATORS_TAB = CREATIVE_MODE_TABS.register("elevators_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("creative_tabs.elevators_escalators.tab_title"))
                    .icon(() -> MOD_ICON_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) ->
                    {
                        output.accept(ELEVATOR_DOOR_ITEM.get());
                        output.accept(OUTER_CONTROL_PANEL_ITEM.get());
                        output.accept(INNER_CONTROL_PANEL_ITEM.get());
                        output.accept(MAIN_CONTROLLER_ITEM.get());
                        output.accept(HOME_ELEVATOR_FRAME_ITEM.get());
                        output.accept(COMMERCIAL_ELEVATOR_FRAME_ITEM.get());
                        output.accept(INDUSTRIAL_ELEVATOR_FRAME_ITEM.get());
                        output.accept(FIRE_FIGHTING_ELEVATOR_FRAME_ITEM.get());
                        output.accept(ELEVATOR_POSITION_FRAME_ITEM.get());
                        output.accept(FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_ITEM.get());
                    })
                    .build()
    );
}
