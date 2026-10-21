package org.firefly.elevators_escalators.escalator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
public final class EscalatorSurveyItem extends Item {
 public EscalatorSurveyItem(Properties p){super(p);}
 @Override public InteractionResult useOn(UseOnContext c){
  var player=c.getPlayer();if(player==null||player.isSpectator()||!player.mayBuild())return InteractionResult.FAIL;
  if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;
  if(c.getLevel().getBlockEntity(c.getClickedPos())instanceof EscalatorControllerBlockEntity){
   CompoundTag tag=new CompoundTag();tag.putLong("Controller",c.getClickedPos().asLong());tag.putString("Dimension",c.getLevel().dimension().location().toString());c.getItemInHand().set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
   player.displayClientMessage(Component.literal("已绑定起点。右键终点支撑方块的表面选择终点。 / Select endpoint support face."),false);
  }else{
   var tag=c.getItemInHand().getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
   if(!tag.contains("Controller")||!tag.getString("Dimension").equals(c.getLevel().dimension().location().toString())){player.displayClientMessage(Component.literal("请先右键本维度扶梯控制器 / Bind a controller first"),false);return InteractionResult.FAIL;}
   BlockPos pos=BlockPos.of(tag.getLong("Controller"));
   if(!c.getLevel().hasChunkAt(pos)||!(c.getLevel().getBlockEntity(pos)instanceof EscalatorControllerBlockEntity be)){player.displayClientMessage(Component.literal("起点未加载 / Controller unloaded"),false);return InteractionResult.FAIL;}
   if(player.distanceToSqr(pos.getX(),pos.getY(),pos.getZ())>520*520)return InteractionResult.FAIL;
   if(be.setEnd(player,c.getClickedPos().relative(c.getClickedFace())))EscalatorMenu.open(player,be);
  }
  return InteractionResult.CONSUME;
 }
}
