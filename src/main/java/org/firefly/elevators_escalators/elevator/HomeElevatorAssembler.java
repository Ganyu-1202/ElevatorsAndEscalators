package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.firefly.elevators_escalators.EnrollBlocks;
import org.firefly.elevators_escalators.EnrollEntities;
import org.firefly.elevators_escalators.entity.HomeElevatorCarriageEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class HomeElevatorAssembler
{
    private static final int MAX_FLOORS = 32;

    private HomeElevatorAssembler()
    {
    }

    public static boolean tryAssemble(@Nonnull Level level,
                                      @Nonnull BlockPos controllerPos,
                                      @Nonnull Direction facing,
                                      @Nullable Player player)
    {
        ShaftScanner.ScanResult scanResult = ShaftScanner.scan(level, controllerPos, facing);
        List<Integer> floorYs = scanResult.floorYs();
        if (floorYs.isEmpty())
        {
            notifyPlayer(player, "结构未完成：请先使用主控制器确认结构无误。", true);
            return false;
        }

        if (floorYs.size() > MAX_FLOORS)
        {
            notifyPlayer(player, "楼层数量超过最大支持的 " + MAX_FLOORS + " 层。", true);
            return false;
        }

        if (!scanResult.topLayerHasController())
        {
            notifyPlayer(player, "最上层框架需要放置主控制器才能装配轿厢。", true);
            return false;
        }

        for (int fy : floorYs)
        {
            if (!ShaftScanner.floorHasDoor(level, controllerPos, facing, fy))
            {
                notifyPlayer(player, "每一层都需要至少一扇电梯门。", true);
                return false;
            }
        }

        assemble(level, controllerPos, scanResult);
        notifyPlayer(player, "成功装配家用电梯轿厢！", false);
        return true;
    }

    private static void assemble(Level level, BlockPos controllerPos, ShaftScanner.ScanResult scanResult)
    {
        BlockPos innerOrigin = controllerPos.below(4);
        int minX = innerOrigin.getX();
        int maxX = innerOrigin.getX() + ShaftScanner.INNER_WIDTH - 1;
        int minZ = innerOrigin.getZ();
        int maxZ = innerOrigin.getZ() + ShaftScanner.INNER_DEPTH - 1;
        double centerX = (minX + maxX) / 2.0 + 0.5;
        double centerZ = (minZ + maxZ) / 2.0 + 0.5;
        double centerY = scanResult.baseFloorY() + 1.0;

        int minFloor = scanResult.floorYs().get(0);
        int maxFloor = scanResult.floorYs().get(scanResult.floorYs().size() - 1);
        int outerMinX = innerOrigin.getX() - ShaftScanner.FRAME_MARGIN;
        int outerMinZ = innerOrigin.getZ() - ShaftScanner.FRAME_MARGIN;
        int outerMaxX = innerOrigin.getX() + ShaftScanner.INNER_WIDTH - 1 + ShaftScanner.FRAME_MARGIN;
        int outerMaxZ = innerOrigin.getZ() + ShaftScanner.INNER_DEPTH - 1 + ShaftScanner.FRAME_MARGIN;

        clearVolume(level, minFloor, maxFloor, outerMinX, outerMaxX, outerMinZ, outerMaxZ);
        level.removeBlock(controllerPos, false);

        var type = EnrollEntities.HOME_ELEVATOR_CARRIAGE.get();
        HomeElevatorCarriageEntity entity = new HomeElevatorCarriageEntity(type, level);
        entity.initFloors(scanResult.floorYs(), scanResult.baseFloorY(), scanResult.yToLogicalFloor());
        entity.moveTo(centerX, centerY, centerZ, 0f, 0f);
        level.addFreshEntity(entity);
    }

    private static void clearVolume(Level level,
                                    int minFloor,
                                    int maxFloor,
                                    int outerMinX,
                                    int outerMaxX,
                                    int outerMinZ,
                                    int outerMaxZ)
    {
        for (int y = minFloor; y <= maxFloor + 1; y++)
        {
            for (int x = outerMinX; x <= outerMaxX; x++)
            {
                for (int z = outerMinZ; z <= outerMaxZ; z++)
                {
                    BlockPos target = new BlockPos(x, y, z);
                    Block block = level.getBlockState(target).getBlock();
                    if (block == EnrollBlocks.MAIN_CONTROLLER_BLOCK.get())
                    {
                        continue;
                    }
                    level.removeBlock(target, false);
                }
            }
        }
    }

    private static void notifyPlayer(@Nullable Player player, @Nonnull String message, boolean isError)
    {
        if (player != null)
        {
            player.displayClientMessage(Component.literal(message), isError);
        }
    }
}
