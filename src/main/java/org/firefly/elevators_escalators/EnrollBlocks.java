package org.firefly.elevators_escalators;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.*;

import static org.firefly.elevators_escalators.ElevatorsEscalators.*;

import javax.annotation.Nonnull;

public class EnrollBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 注册名为 电梯门 的方块
    public static final DeferredBlock<Block> ELEVATOR_DOOR_BLOCK = BLOCKS.register("elevator_door", () ->
            new DoorBlock(BlockSetType.IRON, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(8.0f, 16.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops() // 需要正确工具才会掉落
                    .lightLevel((state) -> 0)
            )
            {
                @Override
                public @Nonnull InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult hitResult)
                {
                    // 不处理右键开门，交给其它系统（比如多方块结构）
                    return InteractionResult.PASS;
                }
            }
    );

    // 注册名为 外侧控制面板 的方块
    public static final DeferredBlock<Block> OUTER_CONTROL_PANEL_BLOCK = BLOCKS.register("outer_control_panel", () ->
            new WallPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.5f, 4.0f)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 0)
            )
    );
    // 注册名为 内侧控制面板 的方块
    public static final DeferredBlock<Block> INNER_CONTROL_PANEL_BLOCK = BLOCKS.register("inner_control_panel", () ->
            new WallPanelBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.5f, 4.0f)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 0)
            )
    );

    // 注册名为 主控制器 的方块
    public static final DeferredBlock<Block> MAIN_CONTROLLER_BLOCK = BLOCKS.register("main_controller", () ->
            new ControllerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.5f, 5.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> 0)
            )
    );

    // 注册名为 家用电梯结构框架 的方块
    public static final DeferredBlock<Block> HOME_ELEVATOR_FRAME_BLOCK = BLOCKS.register("home_elevator_frame", () ->
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(12.0f, 30.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> 0)
            )
    );
    // 注册名为 商业电梯结构框架 的方块
    public static final DeferredBlock<Block> COMMERCIAL_ELEVATOR_FRAME_BLOCK = BLOCKS.register("commercial_elevator_frame", () ->
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(16.0f, 35.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> 0)
            )
    );
    // 注册名为 工业电梯结构框架 的方块
    public static final DeferredBlock<Block> INDUSTRIAL_ELEVATOR_FRAME_BLOCK = BLOCKS.register("industrial_elevator_frame", () ->
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(64.0f, 55.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> 0)
            )
    );
    // 注册名为 消防电梯结构框架 的方块
    public static final DeferredBlock<Block> FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK = BLOCKS.register("fire_fighting_elevator_frame", () ->
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(96.0f, 90.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> 0)
            )
    );
    // 注册名为 电梯定位框架 的方块
    public static final DeferredBlock<Block> ELEVATOR_POSITION_FRAME_BLOCK = BLOCKS.register("elevator_position_frame", () ->
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(10.0f, 20.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> 0)
            )
    );
    // 注册名为 消防电梯定位框架 的方块
    public static final DeferredBlock<Block> FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK = BLOCKS.register("fire_fighting_elevator_position_frame", () ->
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(32.0f, 50.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel((state) -> 0)
            )
    );
    // 注册名为 家用电梯轿厢 的方块（用于激活结构）
    public static final DeferredBlock<Block> HOME_ELEVATOR_CARRIAGE_BLOCK = BLOCKS.register("home_elevator_carriage", () ->
            new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(8.0f, 16.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel((state) -> 0)
            )
    );

    static void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Common setup for Elevators and Escalators mod.");
        LOGGER.info("FMLLoader version: {}", FMLLoader.versionInfo());
        LOGGER.info("FMLCommonSetupEvent: {}", event);

        LOGGER.info("ELEVATOR_DOOR_BLOCK in mineable/pickaxe: {}", ELEVATOR_DOOR_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("ELEVATOR_DOOR_BLOCK in needs_iron_tool: {}", ELEVATOR_DOOR_BLOCK.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL));
        LOGGER.info("OUTER_CONTROL_PANEL_BLOCK in mineable/pickaxe: {}", OUTER_CONTROL_PANEL_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("INNER_CONTROL_PANEL_BLOCK in mineable/pickaxe: {}", INNER_CONTROL_PANEL_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("MAIN_CONTROLLER_BLOCK in mineable/pickaxe: {}", MAIN_CONTROLLER_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("MAIN_CONTROLLER_BLOCK in needs_stone_tool: {}", MAIN_CONTROLLER_BLOCK.get().defaultBlockState().is(BlockTags.NEEDS_STONE_TOOL));
        LOGGER.info("HOME_ELEVATOR_FRAME_BLOCK in mineable/pickaxe: {}", HOME_ELEVATOR_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("HOME_ELEVATOR_FRAME_BLOCK in needs_iron_tool: {}", HOME_ELEVATOR_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL));
        LOGGER.info("COMMERCIAL_ELEVATOR_FRAME_BLOCK in mineable/pickaxe: {}", COMMERCIAL_ELEVATOR_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("COMMERCIAL_ELEVATOR_FRAME_BLOCK in needs_iron_tool: {}", COMMERCIAL_ELEVATOR_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL));
        LOGGER.info("INDUSTRIAL_ELEVATOR_FRAME_BLOCK in mineable/pickaxe: {}", INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("INDUSTRIAL_ELEVATOR_FRAME_BLOCK in needs_diamond_tool: {}", INDUSTRIAL_ELEVATOR_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.NEEDS_DIAMOND_TOOL));
        LOGGER.info("FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK in mineable/pickaxe: {}", FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK in needs_diamond_tool: {}", FIRE_FIGHTING_ELEVATOR_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.NEEDS_DIAMOND_TOOL));
        LOGGER.info("ELEVATOR_POSITION_FRAME_BLOCK in mineable/pickaxe: {}", ELEVATOR_POSITION_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("ELEVATOR_POSITION_FRAME_BLOCK in needs_iron_tool: {}", ELEVATOR_POSITION_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL));
        LOGGER.info("FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK in mineable/pickaxe: {}", FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE));
        LOGGER.info("FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK in needs_diamond_tool: {}", FIRE_FIGHTING_ELEVATOR_POSITION_FRAME_BLOCK.get().defaultBlockState().is(BlockTags.NEEDS_DIAMOND_TOOL));
    }
}
