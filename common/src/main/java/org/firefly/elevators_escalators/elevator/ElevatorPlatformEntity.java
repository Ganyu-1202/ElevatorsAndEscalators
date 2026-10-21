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
    private final StopQueue requests=new StopQueue();
    private int serviceDirection;
    public boolean requested(int offset){return requests.contains(offset);}
    private boolean moving(){return arrivalPending && Math.abs(controller.getY()+1+targetOffset()-getY())>1e-6;}
    private void selectTarget(int offset){
        entityData.set(TARGET,offset);serviceDirection=Double.compare(controller.getY()+1+offset,getY());
        entityData.set(UP,serviceDirection>0);arrivalPending=serviceDirection!=0;doorTicks=0;
    }
    public boolean manualDoor(Player player,boolean close) {
        if (level().isClientSide || player.isSpectator()) return false;
        if (moving()) {
            ElevatorControllerBlockEntity.tell(player,"door_moving"); return false;
        }
        if (close && doorwayOccupied()) { ElevatorControllerBlockEntity.tell(player,"door_blocked"); return false; }
        if(!close)autoDoor.reset();
        entityData.set(CLOSED,close); manualClosed=close; return true;
    }
    private boolean doorwayOccupied() {
        var a=facing();
        // Doorway spans the local front edge and just outside it; rotation keeps it consistent.
        var cs=cabinSpec();
        var door=new AABB(-1.1*cs.sx(),.25*cs.sy(),1.1*cs.sz(),1.1*cs.sx(),3*cs.sy(),1.8*cs.sz());
        var rotated=CabinGeometry.rotate(new CabinGeometry.Box(door.minX,door.minY,door.minZ,door.maxX,door.maxY,door.maxZ),a);
        return !level().getEntities(this,new AABB(getX()+rotated.x1(),getY()+rotated.y1(),getZ()+rotated.z1(),
            getX()+rotated.x2(),getY()+rotated.y2(),getZ()+rotated.z2()),e -> e instanceof LivingEntity && !e.isSpectator()).isEmpty();
    }
    private boolean manualClosed;
    private int doorTicks;
    public boolean doorsClosed() { return entityData.get(CLOSED); }
    public java.util.List<net.minecraft.world.phys.shapes.VoxelShape> cabinShapes() {
        return CabinGeometry.shell(doorsClosed()).stream().map(cabinSpec()::scale).map(b -> CabinGeometry.rotate(b,facing())).toList().stream().map(b -> net.minecraft.world.phys.shapes.Shapes.create(
            new AABB(b.x1()+getX(),b.y1()+getY(),b.z1()+getZ(),b.x2()+getX(),b.y2()+getY(),b.z2()+getZ()))).toList();
    }
    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().expandTowards(0,cabinSpec().cabinHeight(),0); }
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(ElevatorPlatformEntity.class,EntityDataSerializers.INT);
    public int targetOffset() { return entityData.get(TARGET); }
    private static final EntityDataAccessor<Integer> FACING=SynchedEntityData.defineId(ElevatorPlatformEntity.class,EntityDataSerializers.INT);
    public net.minecraft.core.Direction facing() { return CabinGeometry.facing(entityData.get(FACING)); }
    public void setFacing(net.minecraft.core.Direction direction) { entityData.set(FACING,direction.get2DDataValue()); }
    private static final EntityDataAccessor<Integer> WIDTH=SynchedEntityData.defineId(ElevatorPlatformEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LENGTH=SynchedEntityData.defineId(ElevatorPlatformEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CABIN_HEIGHT=SynchedEntityData.defineId(ElevatorPlatformEntity.class,EntityDataSerializers.INT);
    private int configuredSpeed=200;
    private int[] floorLabels={1,2,3};
    public ElevatorSpec cabinSpec(){return new ElevatorSpec(entityData.get(WIDTH),entityData.get(LENGTH),entityData.get(CABIN_HEIGHT),1,1,1);}
    public int[] floorLabels(){return floorLabels.clone();}
    public void applySpec(ElevatorSpec spec,int[] labels){entityData.set(WIDTH,spec.width());entityData.set(LENGTH,spec.length());entityData.set(CABIN_HEIGHT,spec.cabinHeight());configuredSpeed=spec.speedHundredths();floorLabels=labels.clone();refreshDimensions();setBoundingBox(makeBoundingBox());}
    public double deckTop(){return getY()+cabinSpec().deckThickness();}
    @Override protected AABB makeBoundingBox(){
        if(entityData==null)return super.makeBoundingBox();
        var spec=cabinSpec(); var local=CabinGeometry.rotate(new CabinGeometry.Box(-spec.width()/2.,0,-spec.length()/2.,spec.width()/2.,spec.deckThickness(),spec.length()/2.),facing());
        return new AABB(getX()+local.x1(),getY(),getZ()+local.z1(),getX()+local.x2(),getY()+local.y2(),getZ()+local.z2());
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key==WIDTH||key==LENGTH||key==CABIN_HEIGHT||key==FACING)setBoundingBox(makeBoundingBox());}
    private BlockPos controller = BlockPos.ZERO;
    private int height = 8;
    private double lerpX, lerpY, lerpZ;
    private int lerpSteps;
    private final java.util.Map<java.util.UUID,Integer> recentSupport=new java.util.HashMap<>();
    private final java.util.Set<java.util.UUID> savedRiders = new java.util.HashSet<>();
    private final java.util.Map<java.util.UUID, Vec3> riderOffsets = new java.util.HashMap<>();
    private int loadGrace;
    private int restoredGrace;
    private double speed;
    private boolean waitingForRiders;
    public ElevatorPlatformEntity(EntityType<? extends ElevatorPlatformEntity> type, Level level) { super(type, level); setNoGravity(true); }
    public void bind(BlockPos pos, int travel) { bind(pos,travel,net.minecraft.core.Direction.SOUTH); }
    public void bind(BlockPos pos,int travel,net.minecraft.core.Direction facing) { controller=pos.immutable(); height=travel; setFacing(facing); }
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
            net.minecraft.util.Mth.clamp(player.getX()-getX(),-getBoundingBox().getXsize()/2+.1,getBoundingBox().getXsize()/2-.1), 0,
            net.minecraft.util.Mth.clamp(player.getZ()-getZ(),-getBoundingBox().getZsize()/2+.1,getBoundingBox().getZsize()/2-.1)));
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
            if (player.level()!=level() || player.distanceToSqr(getX(),getY(),getZ())>Math.pow(Math.max(16,Math.max(cabinSpec().width(),cabinSpec().length())),2)) {
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
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(UP, false); builder.define(TARGET,0); builder.define(CLOSED,false); builder.define(CURRENT_FLOOR,1); builder.define(DIRECTION,0); builder.define(FACING,0); builder.define(WIDTH,3); builder.define(LENGTH,3); builder.define(CABIN_HEIGHT,4); }
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
    public void requestHall(Player player,int offset,int direction){
        if(level().isClientSide||player.isSpectator()||!(level().getBlockEntity(controller) instanceof ElevatorControllerBlockEntity be)||java.util.Arrays.stream(be.floors()).noneMatch(f->f==offset))return;
        waitingForRiders=false;savedRiders.clear();
        double position=getY()-controller.getY()-1;
        if(!moving()&&Math.abs(position-offset)<1e-5){entityData.set(CLOSED,false);autoDoor.reset();serviceDirection=direction;}
        else requests.addHall(offset,direction);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(direction>0?"已登记上行呼梯 / Up call registered":"已登记下行呼梯 / Down call registered"),true);
    }
    public void request(Player player, boolean up) { requestFloor(player,up?height:0); }
    public void requestFloor(Player player,int offset) {
        if (player.isSpectator() || level().isClientSide) return;
        if (!(level().getBlockEntity(controller) instanceof ElevatorControllerBlockEntity be)
                || java.util.Arrays.stream(be.floors()).noneMatch(v->v==offset)) return;
        waitingForRiders=false; savedRiders.clear(); riderOffsets.clear();
        // Selecting floors only records requests. It never closes doors or resets their timer.
        if(Math.abs(controller.getY()+1+offset-getY())>1e-5 || moving())requests.add(offset);
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
        double estimatedStep=level().isClientSide && lerpSteps>0?(lerpY-getY())/lerpSteps:speed;
        double depth=DeckSupport.recoveryDepth(estimatedStep);
        var occupants=level().getEntities(this,oldBox.inflate(0,depth,0),e -> {
            if(!(e instanceof LivingEntity)||e.isSpectator()||e.isPassenger())return false;
            // AABB overlap alone is not enough: exclude anyone walking off the deck.
            if(e.getX()<oldBox.minX+.05||e.getX()>oldBox.maxX-.05||e.getZ()<oldBox.minZ+.05||e.getZ()>oldBox.maxZ-.05)return false;
            boolean grounded=DeckSupport.supported(e.getY(),oldBox.maxY,e.getDeltaMovement().y);
            Integer last=recentSupport.get(e.getUUID());
            boolean retained=last!=null && DeckSupport.retained(e.getY(),oldBox.maxY,e.getDeltaMovement().y,estimatedStep,tickCount-last);
            if(grounded) recentSupport.put(e.getUUID(),tickCount);
            return grounded||retained;
        });
        recentSupport.keySet().removeIf(id -> occupants.stream().noneMatch(e -> e.getUUID().equals(id)));
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
            if (be.facing()!=facing()) setFacing(be.facing());
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
            int floorIndex=Math.min(be.labels().length-1,FloorDisplay.number(stops,getY()-controller.getY()-1)-1);
            entityData.set(CURRENT_FLOOR,be.labels()[floorIndex]);
            double position=getY()-controller.getY()-1;
            if(arrivalPending && Math.abs(position-targetOffset())<1e-6){
                requests.served(targetOffset(),serviceDirection);
                if(!requests.ahead(position,serviceDirection)){serviceDirection=-serviceDirection;requests.served(targetOffset(),serviceDirection);}
                arrivalPending=false;speed=0;
                entityData.set(CLOSED,false);manualClosed=false;autoDoor.reset();
            }
            if(!arrivalPending){
                // A request for the already occupied floor is satisfied without reopening doors.
                for(int stop:requests.snapshot())if(Math.abs(stop-position)<1e-6){
                    if(!requests.ahead(position,serviceDirection))serviceDirection=-serviceDirection;
                    requests.served(stop,serviceDirection);
                }
                entityData.set(DIRECTION,0);
                if(autoDoor.tick(!doorsClosed(),doorwayOccupied())){entityData.set(CLOSED,true);manualClosed=true;}
                Integer next=requests.next(position,serviceDirection);
                if(next==null || !doorsClosed()){speed=0;return;}
                if(doorwayOccupied()){entityData.set(CLOSED,false);manualClosed=false;autoDoor.reset();return;}
                selectTarget(next);
            }else{
                Integer intermediate=requests.intercept(position,targetOffset(),speed);
                if(intermediate!=null)selectTarget(intermediate);
            }
            double target=controller.getY()+1+targetOffset();
            entityData.set(DIRECTION,Double.compare(target,getY()));
            if (doorTicks>0) { doorTicks--; return; }
            speed=Motion.approachSpeed(speed,target-getY(),ElevatorSpeed.perTick(configuredSpeed));
            nextY=Motion.step(getY(),target,Math.max(0.001,Math.abs(speed)));
            double dy=nextY-getY();
            if (Math.abs(dy)>1e-6 && !clear(level(),oldBox.expandTowards(0,dy,0).expandTowards(0,cabinSpec().cabinHeight()-cabinSpec().deckThickness(),0).deflate(1e-4))) return;
        }
        double dy=nextY-getY();
        if (Math.abs(dy)>1e-8) {
            for (Entity occupant : occupants) {
                // Players send their own predicted position. Moving them again on the
                // server creates an upward correction loop. Other entities are server-owned.
                if (occupant instanceof Player player) {
                    if (!level().isClientSide || !player.isLocalPlayer()) continue;
                } else if (level().isClientSide) continue;
                double amount=DeckSupport.retainedCarry(occupant.getY(),oldBox.maxY,oldBox.maxY+dy);
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
    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putLong("Controller",controller.asLong()); tag.putInt("Height",height); tag.putBoolean("Up",isGoingUp()); tag.putInt("TargetOffset",targetOffset()); tag.putInt("Facing",entityData.get(FACING)); tag.putIntArray("CabinSize",new int[]{cabinSpec().width(),cabinSpec().length(),cabinSpec().cabinHeight()});tag.putIntArray("FloorLabels",floorLabels);tag.putInt("SpeedHundredths",configuredSpeed);tag.putIntArray("StopQueue",requests.cabinSnapshot());tag.putIntArray("HallUp",requests.hallSnapshot(1));tag.putIntArray("HallDown",requests.hallSnapshot(-1));tag.putInt("ServiceDirection",serviceDirection);tag.putBoolean("TripActive",arrivalPending);tag.putBoolean("DoorsClosed",doorsClosed());
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
    @Override protected void readAdditionalSaveData(CompoundTag tag) { controller=BlockPos.of(tag.getLong("Controller")); int h=tag.getInt("Height"); height=Math.max(0,Math.min(65280,h)); entityData.set(UP,tag.getBoolean("Up")); entityData.set(TARGET,tag.contains("TargetOffset")?net.minecraft.util.Mth.clamp(tag.getInt("TargetOffset"),0,height):(isGoingUp()?height:0));
        entityData.set(FACING,tag.contains("Facing")?Math.floorMod(tag.getInt("Facing"),4):0);
        int[] size=tag.getIntArray("CabinSize");
        if(size.length==3 && java.util.Arrays.stream(size).allMatch(v->v>=1&&v<=64))applySpec(new ElevatorSpec(size[0],size[1],size[2],1,1,1),tag.getIntArray("FloorLabels"));
        configuredSpeed=tag.contains("SpeedHundredths") && ElevatorSpeed.valid(tag.getInt("SpeedHundredths"))?tag.getInt("SpeedHundredths"):200;
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
            if (Double.isFinite(x) && Double.isFinite(z) && Math.abs(x)<=32 && Math.abs(z)<=32)
                riderOffsets.put(item.getUUID("Player"),new Vec3(x,0,z));
        }
        savedRiders.addAll(riderOffsets.keySet());
        waitingForRiders=!savedRiders.isEmpty(); loadGrace=40; speed=0; arrivalPending=Math.abs(controller.getY()+1+targetOffset()-getY())>1e-4; autoDoor.reset();
        serviceDirection=net.minecraft.util.Mth.clamp(tag.getInt("ServiceDirection"),-1,1);
        if(tag.contains("TripActive"))arrivalPending=tag.getBoolean("TripActive");
        for(int stop:tag.getIntArray("StopQueue"))if(stop>=0&&stop<=height)requests.add(stop);
        for(int stop:tag.getIntArray("HallUp"))if(stop>=0&&stop<=height)requests.addHall(stop,1);
        for(int stop:tag.getIntArray("HallDown"))if(stop>=0&&stop<=height)requests.addHall(stop,-1);
        if(arrivalPending&&!requests.contains(targetOffset()))requests.add(targetOffset());
        entityData.set(CLOSED,arrivalPending || tag.getBoolean("DoorsClosed"));
    }
}
