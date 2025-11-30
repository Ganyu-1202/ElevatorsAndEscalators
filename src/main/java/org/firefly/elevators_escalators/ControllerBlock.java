package org.firefly.elevators_escalators;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import org.firefly.elevators_escalators.elevator.ShaftScanner;
import org.firefly.elevators_escalators.entity.HomeElevatorCarriageEntity;

public class ControllerBlock extends Block
{
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ControllerBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving)
    {
        if (level.isClientSide) return;

        // 模式包含 C，因此以控制器自身为锚点去匹配
        var match = EnrollEntities.HOME_ELEVATOR_PATTERN.find(level, pos);
        if (match == null) return;

        // 计算4x4底面包围盒，得到几何中心
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, floorY = 0;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int x = 0; x < 4; x++)
        {
            for (int z = 0; z < 4; z++)
            {
                var biw = match.getBlock(x, 0, z); // y=0 为地板层
                BlockPos p = biw.getPos();
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minZ = Math.min(minZ, p.getZ());
                maxZ = Math.max(maxZ, p.getZ());
                floorY = p.getY();
            }
        }
        double centerX = (minX + maxX) / 2.0 + 0.5;
        double centerZ = (minZ + maxZ) / 2.0 + 0.5;
        double centerY = floorY + 1.0; // 轿厢地板上 1 格

        // 可选：清空匹配到的 4x4x4 结构与控制器，真正“转为实体”
        for (int y = 0; y < 5; y++)
        { // 包含控制器层
            for (int x = 0; x < 4; x++)
            {
                for (int z = 0; z < 4; z++)
                {
                    BlockPos p = match.getBlock(x, y, z).getPos();
                    level.removeBlock(p, false);
                }
            }
        }

        // 生成轿厢实体
        var type = EnrollEntities.HOME_ELEVATOR_CARRIAGE.get();
        var entity = new org.firefly.elevators_escalators.entity.HomeElevatorCarriageEntity(type, level);
        entity.moveTo(centerX, centerY, centerZ, 0f, 0f);
        level.addFreshEntity(entity);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        if (level.isClientSide) return;

        // 用控制器为锚点匹配你的多方块模式
        BlockPattern.BlockPatternMatch match = EnrollEntities.HOME_ELEVATOR_PATTERN.find(level, pos);
        if (match == null)
        {
            // 失败不提示
            return;
        }

        // 计算 4x4 地板层中心与高度（y=0 为地板层）
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE, floorY = 0;
        for (int x = 0; x < 4; x++)
        {
            for (int z = 0; z < 4; z++)
            {
                BlockPos p = match.getBlock(x, 0, z).getPos();
                minX = Math.min(minX, p.getX());
                maxX = Math.max(maxX, p.getX());
                minZ = Math.min(minZ, p.getZ());
                maxZ = Math.max(maxZ, p.getZ());
                floorY = p.getY();
            }
        }
        double cx = (minX + maxX) / 2.0 + 0.5;
        double cz = (minZ + maxZ) / 2.0 + 0.5;
        double cy = floorY + 1.0;

        // 可选：清空结构（含控制器层），真正“转为实体”
        for (int y = 0; y < 5; y++)
        { // 0..3 结构层 + 4 控制器层
            for (int x = 0; x < 4; x++)
            {
                for (int z = 0; z < 4; z++)
                {
                    BlockPos p = match.getBlock(x, y, z).getPos();
                    level.removeBlock(p, false);
                }
            }
        }

        // 生成轿厢实体
        var type = EnrollEntities.HOME_ELEVATOR_CARRIAGE.get();
        var entity = new org.firefly.elevators_escalators.entity.HomeElevatorCarriageEntity(type, level);
        entity.moveTo(cx, cy, cz, 0f, 0f);
        level.addFreshEntity(entity);

        // 给放置者提示（仅成功时）
        if (placer instanceof Player player)
        {
            player.displayClientMessage(Component.literal("成功建造电梯轿厢！"), false);
        }
    }
}
