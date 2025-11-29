package org.firefly.elevators_escalators.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.DoorBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.firefly.elevators_escalators.EnrollBlocks;

import static org.firefly.elevators_escalators.ElevatorsEscalators.MODID;

public class ModBlockStateProvider extends BlockStateProvider
{
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper)
    {
        super(output, MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        doorBlock((DoorBlock) EnrollBlocks.ELEVATOR_DOOR_BLOCK.get(),
                modLoc("block/elevator_door_bottom"),
                modLoc("block/elevator_door_top"));


    }
}
