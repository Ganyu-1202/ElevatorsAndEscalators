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

import java.util.Objects;
import javax.annotation.Nonnull;

public class EnrollItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(nn(Registries.CREATIVE_MODE_TAB), MODID);

    public static final DeferredItem<Item> MOD_ICON_ITEM = ITEMS.register("mod_icon", () ->
            new Item(nn(new Item.Properties().stacksTo(1))));

    public static final DeferredItem<Item> ELEVATOR_DOOR_ITEM = ITEMS.register("elevator_door", () ->
            new BlockItem(nn(EnrollBlocks.ELEVATOR_DOOR_BLOCK.get()), nn(new Item.Properties())));

    public static final DeferredItem<Item> OUTER_CONTROL_PANEL_ITEM = ITEMS.register("outer_control_panel", () ->
            new BlockItem(nn(EnrollBlocks.OUTER_CONTROL_PANEL_BLOCK.get()), nn(new Item.Properties())));
    public static final DeferredItem<Item> INNER_CONTROL_PANEL_ITEM = ITEMS.register("inner_control_panel", () ->
            new BlockItem(nn(EnrollBlocks.INNER_CONTROL_PANEL_BLOCK.get()), nn(new Item.Properties())));

    public static final DeferredItem<Item> MAIN_CONTROLLER_ITEM = ITEMS.register("main_controller", () ->
            new BlockItem(nn(EnrollBlocks.MAIN_CONTROLLER_BLOCK.get()), nn(new Item.Properties())));

    public static final DeferredItem<Item> HOME_ELEVATOR_FRAME_ITEM = ITEMS.register("home_elevator_frame", () ->
            new BlockItem(nn(EnrollBlocks.HOME_ELEVATOR_FRAME_BLOCK.get()), nn(new Item.Properties())));
    public static final DeferredItem<Item> COMMERCIAL_ELEVATOR_FRAME_ITEM = ITEMS.register("commercial_elevator_frame", () ->
            new BlockItem(nn(EnrollBlocks.COMMERCIAL_ELEVATOR_FRAME_BLOCK.get()), nn(new Item.Properties())));
    public static final DeferredItem<Item> INDUSTRIAL_ELEVATOR_FRAME_ITEM = ITEMS.register("industrial_elevator_frame", () ->
            new BlockItem(nn(EnrollBlocks.INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get()), nn(new Item.Properties())));
    public static final DeferredItem<Item> FIRE_FIGHTING_ELEVATOR_FRAME_ITEM = ITEMS.register("fire_fighting_elevator_frame", () ->
            new BlockItem(nn(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get()), nn(new Item.Properties())));
    public static final DeferredItem<Item> HOME_ELEVATOR_CARRIAGE_ITEM = ITEMS.register("home_elevator_carriage", () ->
            new HomeElevatorCarriageBlockItem(nn(EnrollBlocks.HOME_ELEVATOR_CARRIAGE_BLOCK.get()), nn(new Item.Properties().stacksTo(1))));

    public static final DeferredItem<Item> ELEVATOR_POSITION_FRAME_ITEM = ITEMS.register("elevator_position_frame", () ->
            new TipsBlockItem(nn(EnrollBlocks.ELEVATOR_POSITION_FRAME_BLOCK.get()), nn(new Item.Properties()),
                    "block.elevators_escalators.elevator_position_frame.tooltip"));
    public static final DeferredItem<Item> FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_ITEM = ITEMS.register("fire_fighting_elevator_position_frame", () ->
            new TipsBlockItem(nn(EnrollBlocks.FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get()), nn(new Item.Properties()),
                    "block.elevators_escalators.fire_fighting_elevator_position_frame.tooltip"));

    public static final DeferredItem<Item> ELEVATOR_MANUAL_ITEM = ITEMS.register("elevator_manual", () ->
            new HomeElevatorManualItem(nn(new Item.Properties().stacksTo(1))));

    public static final DeferredItem<Item> HOME_ELEVATOR_BLUEPRINT_ITEM = ITEMS.register("home_elevator_blueprint", () ->
            new HomeElevatorBlueprintItem(nn(new Item.Properties().stacksTo(1))));

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
                        @Nonnull
                        private static <T> T nn(T value)
        {
                return Objects.requireNonNull(value);
        }
}
