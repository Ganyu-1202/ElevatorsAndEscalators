package org.firefly.elevators_escalators.escalator;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.firefly.elevators_escalators.ModContent;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
public final class EscalatorStepBlockEntity extends BlockEntity {
 private BlockPos controller=BlockPos.ZERO;private UUID owner;
 public EscalatorStepBlockEntity(BlockPos p,BlockState s){super(ModContent.ESC_STEP_BE.get(),p,s);}
 public void bind(BlockPos p,UUID id){controller=p.immutable();owner=id;setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 public boolean owned(BlockPos p,UUID id){return p.equals(controller)&&id.equals(owner);}
 public EscalatorControllerBlockEntity controller(){return level.hasChunkAt(controller)&&level.getBlockEntity(controller)instanceof EscalatorControllerBlockEntity be&&be.id().equals(owner)?be:null;}
 private List<EscalatorPlan.Step> plan=List.of();private int index;
 private EscalatorMotion motion=new EscalatorMotion(0,0,0,0);private boolean stopped=true;
 private static final Map<Entity,Long> MOVED=Collections.synchronizedMap(new WeakHashMap<>());
 private boolean alreadyMoved(Entity e){return Objects.equals(MOVED.get(e),level.getGameTime());}
 public void configure(List<EscalatorPlan.Step> route,int at,EscalatorMotion m,boolean stop){
  if(!plan.isEmpty()&&motion.equals(m)&&stopped==stop)return;
  int from=Math.max(0,at-2),to=Math.min(route.size(),at+3);
  var local=List.copyOf(route.subList(from,to));int nextIndex=at-from;
  if(plan.equals(local)&&index==nextIndex&&motion.equals(m)&&stopped==stop)return;
  plan=local;index=nextIndex;motion=m;stopped=stop;setChanged();level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
 }
 public double animationDistance(float partial){return motion.distanceAt(level.getGameTime()+(stopped?0:partial));}
 public void tick(){
  if(level instanceof net.minecraft.server.level.ServerLevel server&&owner!=null)EscalatorNetwork.get(server).refresh(server,owner,this);
  if(stopped||owner==null||plan.isEmpty()||index<0||index>=plan.size())return;
  if(level instanceof net.minecraft.server.level.ServerLevel server&&EscalatorNetwork.get(server).stopped(owner))return;
  double actualSpeed=motion.speed(level.getGameTime());if(Math.abs(actualSpeed)<1e-6)return;
  // Unloaded neighbors are not damage. Loaded missing/foreign segments stop local transport immediately.
  for(var s:plan){if(!level.hasChunkAt(s.pos()))continue;
   if(!(level.getBlockEntity(s.pos()) instanceof EscalatorStepBlockEntity other)||!other.owned(controller,owner)){
    if(level instanceof net.minecraft.server.level.ServerLevel server)EscalatorNetwork.get(server).fault(server,owner);
    return;
   }
  }

  // Client transport only concerns the local player; do not query the world for every step.
  List<? extends Entity> localRiders=level.isClientSide
      ? level.players().stream().filter(Player::isLocalPlayer).toList() : List.of();
  for(int stepIndex=index;stepIndex<=index;stepIndex++){
   var step=plan.get(stepIndex);
   var pos=step.pos();
   AABB area=new AABB(pos.getX(),pos.getY()+.4,pos.getZ(),pos.getX()+1,pos.getY()+1.8,pos.getZ()+1);
   for(Entity e:level.isClientSide?localRiders:level.getEntities((Entity)null,area,c->c instanceof LivingEntity&&!(c instanceof Player)&&!c.isSpectator()&&!c.isPassenger())){
    if(!e.getBoundingBox().intersects(area)||e.isSpectator()||e.isPassenger())continue;
    if(e instanceof Player player){if(!level.isClientSide||!player.isLocalPlayer())continue;}else if(level.isClientSide)continue;
    if(e.isShiftKeyDown()||e.getDeltaMovement().y>.1||e.fallDistance>1)continue;
    double progress=EscalatorRamp.progress(e.getX()-pos.getX(),e.getZ()-pos.getZ(),step.up());
    double top=pos.getY()+EscalatorRamp.surface(progress,step.rises());
    // Wide entities stand on the highest intersecting slice; allow that small footprint difference.
    if(e.getY()<top-.12||e.getY()>top+.38||alreadyMoved(e))continue;
    Direction d=actualSpeed<0&&stepIndex>0?plan.get(stepIndex-1).up():step.up();double scalar=actualSpeed;
    // Centre alignment prevents accumulated drift off a one-block-wide diagonal route.
    double cx=pos.getX()+.5,cz=pos.getZ()+.5;
    double mx=d.getStepX()*scalar+(d.getAxis()==Direction.Axis.Z?Math.max(-.03,Math.min(.03,cx-e.getX())):0);
    double mz=d.getStepZ()*scalar+(d.getAxis()==Direction.Axis.X?Math.max(-.03,Math.min(.03,cz-e.getZ())):0);
    Vec3 horizontal=new Vec3(mx,0,mz);
    var box=e.getBoundingBox();
    boolean loaded=true;
    for(int x=net.minecraft.util.Mth.floor(box.minX+mx);x<=net.minecraft.util.Mth.floor(box.maxX+mx);x++)
     for(int z=net.minecraft.util.Mth.floor(box.minZ+mz);z<=net.minecraft.util.Mth.floor(box.maxZ+mz);z++)
      if(!level.hasChunkAt(new BlockPos(x,worldPosition.getY(),z)))loaded=false;
    if(!loaded)continue;
    double support=EscalatorRamp.supportHeight(plan,stepIndex,box.minX+mx,box.minZ+mz,box.maxX+mx,box.maxZ+mz);
    // Include the front of the footprint even while its centre still lies on the landing.
    double lift=Double.isFinite(support)?Math.max(0,support-e.getY())+1e-4:0;
    if(lift>0&&!level.noCollision(e,box.move(0,lift,0)))continue;
    // The settling move can invoke fall damage synchronously. Reset before it,
    // not only afterwards; actual falling entities were excluded above.
    e.fallDistance=0;
    if(lift>0){
        e.move(MoverType.SELF,new Vec3(0,lift,0));
    }
    e.move(MoverType.SELF,horizontal);
    // Settle onto real collision, including descent and a blocked horizontal move.
    e.move(MoverType.SELF,new Vec3(0,-(lift+Math.abs(actualSpeed)+.04),0));
    MOVED.put(e,level.getGameTime());
    e.fallDistance=0;e.setOnGround(true);
    if(e.getDeltaMovement().y<0)e.setDeltaMovement(e.getDeltaMovement().multiply(1,0,1));
   }
  }
 }
 static void writeMotion(CompoundTag t,EscalatorMotion m){t.putLong("Epoch",m.epoch());t.putDouble("Distance",m.distance());t.putDouble("InitialSpeed",m.initialSpeed());t.putDouble("TargetSpeed",m.targetSpeed());}
 static EscalatorMotion readMotion(CompoundTag t){return new EscalatorMotion(t.getLong("Epoch"),t.getDouble("Distance"),t.getDouble("InitialSpeed"),t.getDouble("TargetSpeed"));}
 static List<EscalatorPlan.Step> readPlan(CompoundTag t){
  List<EscalatorPlan.Step> result=new ArrayList<>();for(var raw:t.getList("Steps",net.minecraft.nbt.Tag.TAG_COMPOUND)){
   var a=(CompoundTag)raw;result.add(new EscalatorPlan.Step(BlockPos.of(a.getLong("Pos")),Direction.from2DDataValue(a.getInt("Facing")),a.getBoolean("Rises")));
  }return List.copyOf(result);
 }
 @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){
  super.saveAdditional(t,r);t.putLong("Controller",controller.asLong());if(owner!=null)t.putUUID("Owner",owner);
  writeMotion(t,motion);t.putInt("Index",index);t.putBoolean("Stopped",stopped);
  var steps=new net.minecraft.nbt.ListTag();for(var s:plan){var a=new CompoundTag();a.putLong("Pos",s.pos().asLong());a.putInt("Facing",s.up().get2DDataValue());a.putBoolean("Rises",s.rises());steps.add(a);}t.put("Steps",steps);
 }
 @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){
  super.loadAdditional(t,r);controller=BlockPos.of(t.getLong("Controller"));owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;
  plan=readPlan(t);index=t.getInt("Index");motion=readMotion(t);stopped=!t.contains("Epoch")||t.getBoolean("Stopped");
 }
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
