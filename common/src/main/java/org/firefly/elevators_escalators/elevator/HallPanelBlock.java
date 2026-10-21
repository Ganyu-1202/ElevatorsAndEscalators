package org.firefly.elevators_escalators.elevator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
public final class HallPanelBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<HallPanelBlock> CODEC=simpleCodec(HallPanelBlock::new);
    public HallPanelBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
    @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block block,BlockPos from,boolean moving){
        if(!level.isClientSide && !level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).isCollisionShapeFullBlock(level,pos.relative(state.getValue(FACING).getOpposite())))level.removeBlock(pos,false);
    }
    @Override protected float getDestroyProgress(BlockState s,Player player,BlockGetter level,BlockPos pos){return 0;}
    @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new HallPanelBlockEntity(p,s);}
    @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return switch(s.getValue(FACING)){case SOUTH->box(4,2,0,12,14,2);case NORTH->box(4,2,14,12,14,16);case EAST->box(0,2,4,2,14,12);default->box(14,2,4,16,14,12);};}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(!l.isClientSide && l.getBlockEntity(p) instanceof HallPanelBlockEntity panel)panel.call(player,hit.getLocation().y-p.getY()>=.5);
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
