package org.firefly.elevators_escalators;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.firefly.elevators_escalators.elevator.*;
import org.firefly.elevators_escalators.escalator.EscalatorBlock;

public final class ModContent {
    private static final String ID = ElevatorsEscalators.MOD_ID;
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ID, Registries.BLOCK);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ID, Registries.ITEM);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ID, Registries.BLOCK_ENTITY_TYPE);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ID, Registries.ENTITY_TYPE);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS=DeferredRegister.create(ID,Registries.MENU);
    public static final RegistrySupplier<net.minecraft.world.inventory.MenuType<ElevatorMenu>> ELEVATOR_MENU=MENUS.register("elevator",() -> dev.architectury.registry.menu.MenuRegistry.ofExtended(ElevatorMenu::new));
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(ID, Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<ElevatorControllerBlock> CONTROLLER = BLOCKS.register("elevator_controller", () -> new ElevatorControllerBlock(BlockBehaviour.Properties.of().strength(3.5F).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<ShaftFrameBlock> FRAME = BLOCKS.register("shaft_frame", () -> new ShaftFrameBlock(BlockBehaviour.Properties.of().strength(3.5F).requiresCorrectToolForDrops().noOcclusion()));
    public static final RegistrySupplier<BlockItem> FRAME_ITEM = ITEMS.register("shaft_frame", () -> new ShaftFrameItem(FRAME.get(),new Item.Properties()));
    public static final RegistrySupplier<EscalatorBlock> ESCALATOR = BLOCKS.register("escalator", () -> new EscalatorBlock(BlockBehaviour.Properties.of().strength(3.5F).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<BlockEntityType<ElevatorControllerBlockEntity>> CONTROLLER_BE = BLOCK_ENTITIES.register("elevator_controller", () -> BlockEntityType.Builder.of(ElevatorControllerBlockEntity::new, CONTROLLER.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<ShaftFrameBlockEntity>> FRAME_BE=BLOCK_ENTITIES.register("shaft_frame",() -> BlockEntityType.Builder.of(ShaftFrameBlockEntity::new,FRAME.get()).build(null));
    public static final RegistrySupplier<EntityType<ElevatorPlatformEntity>> PLATFORM = ENTITIES.register("elevator_platform", () -> EntityType.Builder.<ElevatorPlatformEntity>of(ElevatorPlatformEntity::new, MobCategory.MISC).sized(3F, 0.25F).clientTrackingRange(10).updateInterval(1).fireImmune().build(ID + ":elevator_platform"));
    public static final RegistrySupplier<BlockItem> CONTROLLER_ITEM = ITEMS.register("elevator_controller", () -> new ElevatorControllerItem(CONTROLLER.get(), new Item.Properties()));
    public static final RegistrySupplier<BlockItem> ESCALATOR_ITEM = ITEMS.register("escalator", () -> new BlockItem(ESCALATOR.get(), new Item.Properties()));
    public static final RegistrySupplier<Item> REMOTE = ITEMS.register("elevator_remote", () -> new ElevatorRemoteItem(new Item.Properties().stacksTo(1)));
    public static final RegistrySupplier<CreativeModeTab> TAB = TABS.register("transport", () -> CreativeTabRegistry.create(b -> b.title(Component.translatable("itemGroup.elevators_escalators")).icon(() -> new ItemStack(CONTROLLER_ITEM.get())).displayItems((parameters, output) -> {
        output.accept(CONTROLLER_ITEM.get());
        output.accept(FRAME_ITEM.get());
        output.accept(ESCALATOR_ITEM.get());
        output.accept(REMOTE.get());
    })));
    public static void init() { BLOCKS.register(); ITEMS.register(); BLOCK_ENTITIES.register(); ENTITIES.register(); MENUS.register(); TABS.register(); }
    private ModContent() {}
}
