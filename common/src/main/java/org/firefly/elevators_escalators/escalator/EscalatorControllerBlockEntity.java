package org.firefly.elevators_escalators.escalator;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.firefly.elevators_escalators.ModContent;
import java.util.*;
public final class EscalatorControllerBlockEntity extends BlockEntity {
 private UUID owner=UUID.randomUUID();private BlockPos end;private boolean built;private int mode=1,speed=100;private List<EscalatorPlan.Step> plan=List.of();private final Set<BlockPos> generated=new HashSet<>();
 private int paidTreads;
 public void refundMaterials(){
  int amount=paidTreads;paidTreads=0;setChanged();
  org.firefly.elevators_escalators.ConstructionMaterials.drop(level,worldPosition,ModContent.ESCALATOR_ITEM.get(),amount);
 }
 public EscalatorControllerBlockEntity(BlockPos p,BlockState s){super(ModContent.ESC_CONTROLLER_BE.get(),p,s);}
 public UUID id(){return owner;}public boolean built(){return built;}public int mode(){return mode;}public int speed(){return speed;}public boolean hasEnd(){return end!=null;}
 public BlockPos start(){return worldPosition.above();}
 public boolean setEnd(Player p,BlockPos pos){if(built){tell(p,"已建造；更改端点请重新放置控制器 / Already built");return false;}String err=EscalatorPlan.error(start(),pos);if(err!=null){tell(p,err);return false;}end=pos.immutable();speed=EscalatorPlan.recommendedSpeed(start(),end);setChanged();return true;}
 public void action(Player player,int action,int selectedSpeed){
  if(player.isSpectator()||!player.mayBuild()||!level.mayInteract(player,worldPosition)
     ||!player.mayUseItemAt(worldPosition,Direction.UP,player.getMainHandItem()))return;
  if(action==0){if(EscalatorPlan.validSpeed(selectedSpeed))build(player,selectedSpeed);return;}
  if(!built)return;
  mode=action==1?1:action==2?-1:0;if(level instanceof net.minecraft.server.level.ServerLevel server)EscalatorNetwork.get(server).change(server,owner,mode,speed);sync();
 }
 private void build(Player p,int selectedSpeed){
  if(built||end==null)return;
  List<EscalatorPlan.Step> steps=EscalatorPlan.withEntrance(start(),end);
  Set<BlockPos> pathCells=new HashSet<>();for(var step:steps)pathCells.add(step.pos());
  for(var step:steps){var pos=step.pos();
   for(int y=0;y<=3;y++){var check=pos.above(y);if(level.isOutsideBuildHeight(check)||!level.getWorldBorder().isWithinBounds(check)||!level.hasChunkAt(check)||!level.mayInteract(p,check)||!p.mayUseItemAt(check,Direction.UP,p.getMainHandItem())){tell(p,"路径越界、区块未加载或无权限："+check.toShortString());return;}
    // Full headroom is conservative; never remove blocks, fluids, or inventories.
    var state=level.getBlockState(check);boolean ownStep=pathCells.contains(check);
    if(!state.isAir() && (y==0||!ownStep)){tell(p,"路径或头部空间受阻："+check.toShortString());return;}
    if(!level.getFluidState(check).isEmpty()){tell(p,"路径有流体："+check.toShortString());return;}
   }
   if(!level.getEntities((Entity)null,new AABB(pos),e->!e.isSpectator()).isEmpty()){tell(p,"踏步位置有实体，请先让开："+pos.toShortString());return;}
  }
  int required=steps.size();
  if(!p.getAbilities().instabuild&&org.firefly.elevators_escalators.ConstructionMaterials.count(p,ModContent.ESCALATOR_ITEM.get())<required){
   tell(p,"踏板不足：需要 "+required+" 个（包含入口和出口平段） / Not enough treads");return;
  }
  for(var step:steps){var pos=step.pos();
   var state=ModContent.ESCALATOR.get().defaultBlockState().setValue(EscalatorBlock.FACING,step.up()).setValue(EscalatorBlock.RISING,step.rises()).setValue(EscalatorBlock.ASSEMBLED,true);
   if(!level.setBlock(pos,state,3)||!(level.getBlockEntity(pos)instanceof EscalatorStepBlockEntity be)){if(level.getBlockState(pos).is(ModContent.ESCALATOR.get())&&level.getBlockState(pos).getValue(EscalatorBlock.ASSEMBLED))level.removeBlock(pos,false);dismantle();tell(p,"建造失败，已撤回本次踏步 / Build rolled back");return;}
   generated.add(pos);be.bind(worldPosition,owner);
  }
  if(!p.getAbilities().instabuild){
   if(!org.firefly.elevators_escalators.ConstructionMaterials.take(p,ModContent.ESCALATOR_ITEM.get(),required)){
    dismantle();tell(p,"材料扣除失败，已撤回本次建造 / Material transaction failed");return;
   }
   paidTreads=required;
  }
  plan=steps;speed=selectedSpeed;mode=1;built=true;registered=false;tick();sync();tell(p,"扶梯已建成，默认上行 / Escalator built, ascending");
 }
 public void dismantle(){
  if(level instanceof net.minecraft.server.level.ServerLevel server)
   EscalatorNetwork.get(server).delete(server,owner,worldPosition,generated);
  generated.clear();built=false;setChanged();
 }
 @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
 private void sync(){setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 private boolean registered;
 public void tick(){
  if(level instanceof net.minecraft.server.level.ServerLevel server&&built&&end!=null&&!registered){
   if(plan.isEmpty()){
    var entrance=EscalatorPlan.withEntrance(start(),end);
    plan=generated.contains(entrance.getFirst().pos())?entrance:EscalatorPlan.build(start(),end);
   }
   EscalatorNetwork.get(server).register(server,owner,worldPosition,plan,mode,speed);registered=true;
  }
 }
 private static void tell(Player p,String s){p.displayClientMessage(Component.literal(s),false);}
 @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putInt("PaidTreads",paidTreads);t.putUUID("Owner",owner);if(end!=null)t.putLong("End",end.asLong());t.putBoolean("Built",built);t.putInt("Mode",mode);t.putInt("Speed",speed);t.putLongArray("Generated",generated.stream().mapToLong(BlockPos::asLong).toArray());}
 @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);paidTreads=Math.max(0,t.getInt("PaidTreads"));if(t.hasUUID("Owner"))owner=t.getUUID("Owner");end=t.contains("End")?BlockPos.of(t.getLong("End")):null;built=t.getBoolean("Built");mode=Math.max(-1,Math.min(1,t.getInt("Mode")));speed=EscalatorPlan.validSpeed(t.getInt("Speed"))?t.getInt("Speed"):100;generated.clear();for(long p:t.getLongArray("Generated"))generated.add(BlockPos.of(p));if(end!=null&&EscalatorPlan.error(start(),end)!=null){end=null;built=false;}}
}
