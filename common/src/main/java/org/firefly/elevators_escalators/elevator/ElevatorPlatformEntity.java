package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

/** Fixed 3x3 deck. Server-authoritative movement; clients interpolate tracked positions. */
public final class ElevatorPlatformEntity extends Entity {
    private static final EntityDataAccessor<Boolean> UP = SynchedEntityData.defineId(ElevatorPlatformEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CLOSED=SynchedEntityData.defineId(ElevatorPlatformEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CURRENT_FLOOR=SynchedEntityData.defineId(ElevatorPlatformEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DIRECTION=SynchedEntityData.defineId(ElevatorPlatformEntity.class,EntityDataSerializers.INT);
    public int currentFloor() { return entityData.get(CURRENT_FLOOR); }
    public int travelDirection() { return entityData.get(DIRECTION); }
    private final DoorTimer autoDoor=new DoorTimer();
    private boolean arrivalPending;
    public void controlActivity() { autoDoor.reset(); }
    public boolean manualDoor(Player player,boolean close) {
        controlActivity();
        if (level().isClientSide || player.isSpectator()) return false;
        if (Math.abs(controller.getY()+1+targetOffset()-getY())>1e-4) {
            ElevatorControllerBlockEntity.tell(player,"door_moving"); return false;
        }
        if (close && doorwayOccupied()) { ElevatorControllerBlockEntity.tell(player,"door_blocked"); return false; }
        entityData.set(CLOSED,close); manualClosed=close; return true;
    }
    private boolean doorwayOccupied() {
        return !level().getEntities(this,new AABB(getX()-1.1,getY()+.25,getZ()+1.1,getX()+1.1,getY()+3,getZ()+1.8),e -> e instanceof LivingEntity && !e.isSpectator()).isEmpty();
    }
    private boolean manualClosed;
    private int doorTicks;
    public boolean doorsClosed() { return entityData.get(CLOSED); }
    public java.util.List<net.minecraft.world.phys.shapes.VoxelShape> cabinShapes() {
        return CabinGeometry.shell(doorsClosed()).stream().map(b -> net.minecraft.world.phys.shapes.Shapes.create(
            new AABB(b.x1()+getX(),b.y1()+getY(),b.z1()+getZ(),b.x2()+getX(),b.y2()+getY(),b.z2()+getZ()))).toList();
    }
    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().expandTowards(0,CabinGeometry.TOP,0); }
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(ElevatorPlatformEntity.class,EntityDataSerializers.INT);
    public int targetOffset() { return entityData.get(TARGET); }
    private BlockPos controller = BlockPos.ZERO;
    private int height = 8;
    private double lerpX, lerpY, lerpZ;
    private int lerpSteps;
    private final java.util.Set<java.util.UUID> savedRiders = new java.util.HashSet<>();
    private final java.util.Map<java.util.UUID, Vec3> riderOffsets = new java.util.HashMap<>();
    private int loadGrace;
    private int restoredGrace;
    private double speed;
    private boolean waitingForRiders;
    public ElevatorPlatformEntity(EntityType<? extends ElevatorPlatformEntity> type, Level level) { super(type, level); setNoGravity(true); }
    public void bind(BlockPos pos, int travel) { controller = pos.immutable(); height = travel; }
    public boolean isGoingUp() { return entityData.get(UP); }
    public BlockPos controllerPos() { return controller; }
    public void awaitReturningRider(Player player) {
        if (riderOffsets.containsKey(player.getUUID()) || DeckSupport.recordable(player.getY(),getBoundingBox().maxY,player.getDeltaMovement().y)) {
            remember(player);
            savedRiders.add(player.getUUID()); waitingForRiders=true;
        }
    }
    private void remember(Player player) {
        riderOffsets.put(player.getUUID(), new Vec3(
            net.minecraft.util.Mth.clamp(player.getX()-getX(),-1.35,1.35), 0,
            net.minecraft.util.Mth.clamp(player.getZ()-getZ(),-1.35,1.35)));
    }
    private void restoreRiders() {
        if (!(level() instanceof net.minecraft.server.level.ServerLevel server)) return;
        var iterator=savedRiders.iterator();
        while (iterator.hasNext()) {
            java.util.UUID id=iterator.next();
            var player=server.getServer().getPlayerList().getPlayer(id);
            if (player==null) continue;
            Vec3 offset=riderOffsets.getOrDefault(id,Vec3.ZERO);
            // Never retrieve someone who changed dimension or left this shaft.
            if (player.level()!=level() || player.distanceToSqr(getX(),getY(),getZ())>16*16) {
                iterator.remove(); riderOffsets.remove(id); continue;
            }
            double top=getBoundingBox().maxY;
            AABB destination=player.getBoundingBox().move(getX()+offset.x-player.getX(),top-player.getY(),getZ()+offset.z-player.getZ());
            boolean previous=noPhysics; noPhysics=true;
            boolean free;
            try { free=level().noCollision(player,destination); } finally { noPhysics=previous; }
            if (!free) continue;
            player.connection.teleport(getX()+offset.x,top,getZ()+offset.z,player.getYRot(),player.getXRot());
            player.setDeltaMovement(player.getDeltaMovement().multiply(1,0,1));
            player.fallDistance=0; player.setOnGround(true);
            iterator.remove();
            restoredGrace=40;
            org.firefly.elevators_escalators.ElevatorsEscalators.LOGGER.info("Restored rider {} on elevator {}", id,getUUID());
        }
        waitingForRiders=!savedRiders.isEmpty();
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(UP, false); builder.define(TARGET,0); builder.define(CLOSED,false); builder.define(CURRENT_FLOOR,1); builder.define(DIRECTION,0); }
    @Override public boolean isPickable() { return true; }
    @Override public boolean canBeCollidedWith() { return !isRemoved() && !noPhysics; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean canCollideWith(Entity entity) { return false; }
    @Override public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            if (player.isShiftKeyDown() && player.getItemInHand(hand).getItem() instanceof ElevatorRemoteItem remote)
                remote.bind(player.getItemInHand(hand),player,controller);
            else ElevatorMenu.open(player,this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
    public void request(Player player, boolean up) { requestFloor(player,up?height:0); }
    public void requestFloor(Player player,int offset) {
        if (player.isSpectator() || level().isClientSide) return;
        if (!(level().getBlockEntity(controller) instanceof ElevatorControllerBlockEntity be)
                || java.util.Arrays.stream(be.floors()).noneMatch(v->v==offset)) return;
        waitingForRiders=false; savedRiders.clear(); riderOffsets.clear();
        entityData.set(TARGET,offset); entityData.set(UP,controller.getY()+1+offset>getY());
        doorTicks=10; manualClosed=false; autoDoor.reset();
        arrivalPending=Math.abs(controller.getY()+1+offset-getY())>1e-4;
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.elevators_escalators.floor_target",offset),true);
    }
    public static boolean clear(Level level, AABB area) {
        int minX = (int)Math.floor(area.minX), maxX = (int)Math.floor(area.maxX - 1e-5);
        int minZ = (int)Math.floor(area.minZ), maxZ = (int)Math.floor(area.maxZ - 1e-5);
        for (int x=minX; x<=maxX; x++) for (int z=minZ; z<=maxZ; z++) {
            if (!level.hasChunkAt(new BlockPos(x, (int)area.minY, z))) return false;
            for (int y=(int)Math.floor(area.minY); y<=Math.floor(area.maxY-1e-5); y++) {
                BlockPos pos = new BlockPos(x,y,z);
                if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return false;
            }
        }
        return true;
    }
    @Override public void tick() {
        super.tick();
        AABB oldBox = getBoundingBox();
        double nextX=getX(), nextY=getY(), nextZ=getZ();
        var occupants = level().getEntities(this, oldBox.inflate(0,0.4,0),
            e -> e instanceof LivingEntity && !e.isSpectator() && !e.isPassenger()
                && DeckSupport.supported(e.getY(),oldBox.maxY,e.getDeltaMovement().y));
        if (level().isClientSide) {
            if (lerpSteps > 0) {
                nextX += (lerpX-getX())/lerpSteps;
                nextY += (lerpY-getY())/lerpSteps;
                nextZ += (lerpZ-getZ())/lerpSteps;
                lerpSteps--;
            }
        } else {
            if (!level().hasChunkAt(controller)) return;
            if (!(level().getBlockEntity(controller) instanceof ElevatorControllerBlockEntity be) || !be.owns(getUUID())) { discard(); return; }
            if (loadGrace > 0) { loadGrace--; return; }
            if (waitingForRiders) {
                restoreRiders();
                if (waitingForRiders) return;
            }
            if (restoredGrace>0) { restoredGrace--; return; }
            riderOffsets.clear();
            for (Entity e:level().getEntities(this,oldBox.inflate(0,0.7,0),
                    candidate -> candidate instanceof Player && !candidate.isSpectator()
                        && DeckSupport.recordable(candidate.getY(),oldBox.maxY,candidate.getDeltaMovement().y))) {
                remember((Player)e);
            }
            int[] stops=be.floors();
            entityData.set(CURRENT_FLOOR,FloorDisplay.number(stops,getY()-controller.getY()-1));
            double target=controller.getY()+1+targetOffset();
            entityData.set(DIRECTION,Math.abs(target-getY())<1e-6?0:(target>getY()?1:-1));
            if (Math.abs(target-getY())<1e-6) {
                if (arrivalPending) { entityData.set(CLOSED,false); manualClosed=false; arrivalPending=false; autoDoor.reset(); }
                if (autoDoor.tick(!doorsClosed(),doorwayOccupied())) { entityData.set(CLOSED,true); manualClosed=true; }
                speed=0; return;
            }
            if (!doorsClosed()) {
                if (doorwayOccupied()) return;
                entityData.set(CLOSED,true); doorTicks=10;
            }
            if (doorTicks>0) { doorTicks--; return; }
            speed=Motion.approachSpeed(speed,target-getY());
            nextY=Motion.step(getY(),target,Math.max(0.001,Math.abs(speed)));
            double dy=nextY-getY();
            if (Math.abs(dy)>1e-6 && !clear(level(),oldBox.expandTowards(0,dy,0).expandTowards(0,CabinGeometry.TOP-.25,0).deflate(1e-4))) return;
        }
        double dy=nextY-getY();
        if (Math.abs(dy)>1e-8) {
            for (Entity occupant : occupants) {
                // Players send their own predicted position. Moving them again on the
                // server creates an upward correction loop. Other entities are server-owned.
                if (occupant instanceof Player player) {
                    if (!level().isClientSide || !player.isLocalPlayer()) continue;
                } else if (level().isClientSide) continue;
                double amount=DeckSupport.carryDelta(occupant.getY(),oldBox.maxY,oldBox.maxY+dy);
                boolean previous=noPhysics; noPhysics=true;
                try { occupant.move(MoverType.SELF,new Vec3(0,amount,0)); }
                finally { noPhysics=previous; }
                occupant.fallDistance=0;
                occupant.setOnGround(true);
                if (occupant.getDeltaMovement().y<0) occupant.setDeltaMovement(occupant.getDeltaMovement().multiply(1,0,1));
            }
        }
        // Carry before advancing the collision surface, not after embedding it in riders.
        setPos(nextX,nextY,nextZ);
    }
    @Override public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) { lerpX=x; lerpY=y; lerpZ=z; lerpSteps=Math.max(1,steps); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putLong("Controller",controller.asLong()); tag.putInt("Height",height); tag.putBoolean("Up",isGoingUp()); tag.putInt("TargetOffset",targetOffset());
        net.minecraft.nbt.ListTag riders=new net.minecraft.nbt.ListTag();
        java.util.Set<java.util.UUID> ids=new java.util.HashSet<>(riderOffsets.keySet()); ids.addAll(savedRiders);
        for (java.util.UUID id:ids) riders.add(net.minecraft.nbt.StringTag.valueOf(id.toString()));
        tag.put("Riders",riders);
        net.minecraft.nbt.ListTag offsets=new net.minecraft.nbt.ListTag();
        for (var entry:riderOffsets.entrySet()) {
            CompoundTag item=new CompoundTag(); item.putUUID("Player",entry.getKey());
            item.putDouble("X",entry.getValue().x); item.putDouble("Z",entry.getValue().z); offsets.add(item);
        }
        tag.put("RiderOffsets",offsets);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { controller=BlockPos.of(tag.getLong("Controller")); int h=tag.getInt("Height"); height=Motion.validHeight(h)?h:8; entityData.set(UP,tag.getBoolean("Up")); entityData.set(TARGET,tag.contains("TargetOffset")?net.minecraft.util.Mth.clamp(tag.getInt("TargetOffset"),0,height):(isGoingUp()?height:0));
        savedRiders.clear();
        for (net.minecraft.nbt.Tag rider:tag.getList("Riders",8)) {
            try { savedRiders.add(java.util.UUID.fromString(rider.getAsString())); }
            catch (IllegalArgumentException ignored) { /* Ignore corrupt optional rider metadata. */ }
        }
        riderOffsets.clear();
        for (net.minecraft.nbt.Tag value:tag.getList("RiderOffsets",10)) {
            CompoundTag item=(CompoundTag)value;
            if (!item.hasUUID("Player")) continue;
            double x=item.getDouble("X"), z=item.getDouble("Z");
            if (Double.isFinite(x) && Double.isFinite(z) && Math.abs(x)<=1.35 && Math.abs(z)<=1.35)
                riderOffsets.put(item.getUUID("Player"),new Vec3(x,0,z));
        }
        savedRiders.addAll(riderOffsets.keySet());
        waitingForRiders=!savedRiders.isEmpty(); loadGrace=40; speed=0; arrivalPending=Math.abs(controller.getY()+1+targetOffset()-getY())>1e-4; autoDoor.reset(); entityData.set(CLOSED,arrivalPending);
    }
}
