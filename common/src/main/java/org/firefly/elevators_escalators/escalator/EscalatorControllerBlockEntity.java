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
 private UUID owner=UUID.randomUUID();private BlockPos end;private boolean built;private int mode=1,speed=100;private double actualSpeed;private List<EscalatorPlan.Step> plan=List.of();private final Set<BlockPos> generated=new HashSet<>();
 public EscalatorControllerBlockEntity(BlockPos p,BlockState s){super(ModContent.ESC_CONTROLLER_BE.get(),p,s);}
 public UUID id(){return owner;}public boolean built(){return built;}public int mode(){return mode;}public int speed(){return speed;}public boolean hasEnd(){return end!=null;}
 public BlockPos start(){return worldPosition.above();}
 public boolean setEnd(Player p,BlockPos pos){if(built){tell(p,"已建造；更改端点请重新放置控制器 / Already built");return false;}String err=EscalatorPlan.error(start(),pos);if(err!=null){tell(p,err);return false;}end=pos.immutable();speed=EscalatorPlan.recommendedSpeed(start(),end);setChanged();return true;}
 public void action(Player player,int action,int selectedSpeed){
  if(player.isSpectator()||!player.mayBuild())return;
  if(action==0){if(EscalatorPlan.validSpeed(selectedSpeed))build(player,selectedSpeed);return;}
  if(!built)return;
  mode=action==1?1:action==2?-1:0;if(mode==0)actualSpeed=0;sync();
 }
 private void build(Player p,int selectedSpeed){
  if(built||end==null)return;
  List<EscalatorPlan.Step> steps=EscalatorPlan.build(start(),end);
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
  for(var step:steps){var pos=step.pos();
   var state=ModContent.ESCALATOR.get().defaultBlockState().setValue(EscalatorBlock.FACING,step.up()).setValue(EscalatorBlock.RISING,step.rises()).setValue(EscalatorBlock.ASSEMBLED,true);
   if(!level.setBlock(pos,state,3)||!(level.getBlockEntity(pos)instanceof EscalatorStepBlockEntity be)){if(level.getBlockState(pos).is(ModContent.ESCALATOR.get())&&level.getBlockState(pos).getValue(EscalatorBlock.ASSEMBLED))level.removeBlock(pos,false);dismantle();tell(p,"建造失败，已撤回本次踏步 / Build rolled back");return;}
   generated.add(pos);be.bind(worldPosition,owner);
  }
  plan=steps;speed=selectedSpeed;mode=1;built=true;sync();tell(p,"扶梯已建成，默认上行 / Escalator built, ascending");
 }
 public void dismantle(){for(var p:generated){if(!level.hasChunkAt(p))level.getChunk(p.getX()>>4,p.getZ()>>4);if(level.getBlockEntity(p)instanceof EscalatorStepBlockEntity be&&be.owned(worldPosition,owner))level.removeBlock(p,false);}generated.clear();built=false;actualSpeed=0;setChanged();}
 @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
 private void sync(){setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 public void tick(){
  if(!built||end==null)return;if(plan.isEmpty())plan=EscalatorPlan.build(start(),end);
  // Incomplete or unloaded segments stop the entire conveyor without force loading.
  for(var step:plan)if(!level.hasChunkAt(step.pos())||!(level.getBlockEntity(step.pos())instanceof EscalatorStepBlockEntity be)||!be.owned(worldPosition,owner)){actualSpeed=0;return;}
  double target=mode*speed/2000.0;
  if(mode==0){actualSpeed=0;return;}
  // Decelerate to zero before changing direction.
  double desired=actualSpeed*target<0?0:target;
  actualSpeed+=Math.max(-.003,Math.min(.003,desired-actualSpeed));
  if(Math.abs(actualSpeed)<1e-6)return;
  Set<Integer> moved=new HashSet<>();
  for(int stepIndex=0;stepIndex<plan.size();stepIndex++){
   var step=plan.get(stepIndex);
   var pos=step.pos();double top=pos.getY()+.5;
   AABB area=new AABB(pos.getX(),pos.getY()+.4,pos.getZ(),pos.getX()+1,pos.getY()+1.4,pos.getZ()+1);
   for(Entity e:level.getEntities((Entity)null,area,c->c instanceof LivingEntity&&!c.isSpectator()&&!c.isPassenger())){
    if(e instanceof Player player){if(!level.isClientSide||!player.isLocalPlayer())continue;}else if(level.isClientSide)continue;
    if(e.isShiftKeyDown()||e.getDeltaMovement().y>.1)continue;
    if(e.getY()<top-.12||e.getY()>top+.15||!moved.add(e.getId()))continue;
    Direction d=actualSpeed<0&&stepIndex>0?plan.get(stepIndex-1).up():step.up();double scalar=actualSpeed;
    // Centre alignment prevents accumulated drift off a one-block-wide diagonal route.
    double cx=pos.getX()+.5,cz=pos.getZ()+.5;
    double mx=d.getStepX()*scalar+(d.getAxis()==Direction.Axis.Z?Math.max(-.03,Math.min(.03,cx-e.getX())):0);
    double mz=d.getStepZ()*scalar+(d.getAxis()==Direction.Axis.X?Math.max(-.03,Math.min(.03,cz-e.getZ())):0);
    Vec3 horizontal=new Vec3(mx,0,mz);
    if(!level.noCollision(e,e.getBoundingBox().move(horizontal))){
     // A one-block rise needs a full-block step, not the old hard-coded half lift.
     // Check the raised destination including horizontal travel, not just current position.
     double rise=EscalatorTransport.liftForClearance(
         level.noCollision(e,e.getBoundingBox().move(horizontal).move(0,.5,0)),
         level.noCollision(e,e.getBoundingBox().move(horizontal).move(0,1,0)));
     if(rise<=0||!level.noCollision(e,e.getBoundingBox().move(0,rise,0)))continue;
     e.move(MoverType.SELF,new Vec3(0,rise,0));
    }
    e.move(MoverType.SELF,horizontal);e.fallDistance=0;
    if(e.getDeltaMovement().y<0)e.setDeltaMovement(e.getDeltaMovement().multiply(1,0,1));
   }
  }
 }
 private static void tell(Player p,String s){p.displayClientMessage(Component.literal(s),false);}
 @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putUUID("Owner",owner);if(end!=null)t.putLong("End",end.asLong());t.putBoolean("Built",built);t.putInt("Mode",mode);t.putInt("Speed",speed);t.putLongArray("Generated",generated.stream().mapToLong(BlockPos::asLong).toArray());}
 @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);if(t.hasUUID("Owner"))owner=t.getUUID("Owner");end=t.contains("End")?BlockPos.of(t.getLong("End")):null;built=t.getBoolean("Built");mode=Math.max(-1,Math.min(1,t.getInt("Mode")));speed=EscalatorPlan.validSpeed(t.getInt("Speed"))?t.getInt("Speed"):100;generated.clear();for(long p:t.getLongArray("Generated"))generated.add(BlockPos.of(p));if(end!=null&&EscalatorPlan.error(start(),end)!=null){end=null;built=false;}}
}
