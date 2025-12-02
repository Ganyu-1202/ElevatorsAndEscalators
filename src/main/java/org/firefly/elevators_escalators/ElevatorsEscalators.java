package org.firefly.elevators_escalators;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.firefly.elevators_escalators.client.render.HomeElevatorCarriageRenderer;

import java.util.Objects;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ElevatorsEscalators.MODID)
public class ElevatorsEscalators
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "elevators_escalators";
    // Directly reference a slf4j logger
    static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ElevatorsEscalators(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

    // Register the Deferred Register to the mod event bus so blocks get registered
    EnrollBlocks.BLOCKS.register(modEventBus);
    // Register the Deferred Register to the mod event bus so items get registered
    EnrollItems.ITEMS.register(modEventBus);
    // Register the Deferred Register to the mod event bus so tabs get registered
    EnrollItems.CREATIVE_MODE_TABS.register(modEventBus);
    // Register entities so DeferredHolder#get works during client setup
    EnrollEntities.ENTITIES.register(modEventBus);

        modEventBus.addListener(EnrollBlocks::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ElevatorsEscalators) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
        {
        LOGGER.info("DIRT BLOCK >> {}", Objects.requireNonNull(
            BuiltInRegistries.BLOCK.getKey(Objects.requireNonNull(Blocks.DIRT))));
        }

        LOGGER.info("{}{}", Config.magicNumberIntroduction, Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(() -> EntityRenderers.register(
            Objects.requireNonNull(EnrollEntities.HOME_ELEVATOR_CARRIAGE.get()),
            HomeElevatorCarriageRenderer::new));
        }

        @SubscribeEvent
        public static void registerBlockColors(RegisterColorHandlersEvent.Block event)
        {
            event.register((state, level, pos, tintIndex) ->
            {
                if (tintIndex != 0 || state == null)
                {
                    return 0xFFFFFF;
                }
        var assembledProperty = Objects.requireNonNull(ControllerBlock.ASSEMBLED);
        if (state.hasProperty(assembledProperty)
            && state.getValue(assembledProperty))
                {
                    return 0xFF3C3C;
                }
                return 0xFFFFFF;
            }, EnrollBlocks.MAIN_CONTROLLER_BLOCK.get());
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event)
        {
            event.register((stack, tintIndex) -> 0xFFFFFF,
                    EnrollBlocks.MAIN_CONTROLLER_BLOCK.get());
        }
    }
}
