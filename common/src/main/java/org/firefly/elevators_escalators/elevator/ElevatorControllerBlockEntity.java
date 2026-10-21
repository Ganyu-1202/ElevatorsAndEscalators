package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.firefly.elevators_escalators.ModContent;
import java.util.UUID;

public final class ElevatorControllerBlockEntity extends BlockEntity {
    private UUID platformId;
    private net.minecraft.core.Direction facing=net.minecraft.core.Direction.SOUTH;
    public net.minecraft.core.Direction facing() { return facing; }
    public void setFacing(net.minecraft.core.Direction direction) { facing=direction; var p=platform(); if(p!=null) p.setFacing(direction); setChanged(); }
    private UUID shaftOwner=UUID.randomUUID();
    private java.util.Set<BlockPos> shaftCells=new java.util.HashSet<>();
    public UUID shaftOwner() { return shaftOwner; }
    public java.util.Set<BlockPos> shaftCells() { return java.util.Set.copyOf(shaftCells); }
    public void setShaftCells(java.util.Set<BlockPos> cells) { shaftCells=new java.util.HashSet<>(cells); setChanged(); }
    private int height = 8;
    private int[] floors = FloorPlan.defaults(8);
    public int[] floors() { return floors.clone(); }
    public ElevatorPlatformEntity platform() {
        if (!(level instanceof ServerLevel server) || platformId==null) return null;
        return server.getEntity(platformId) instanceof ElevatorPlatformEntity entity ? entity : null;
    }
    public void syncFacingToPlatform() {
        var entity=platform(); if(entity!=null) entity.setFacing(facing);
    }
    public boolean addFloor(int offset,Player player) {
        try { int[] updated=FloorPlan.add(floors,offset,height);
            if(!shaftCells.isEmpty() && !AutomaticShaft.rebuild(this,height,updated)) { tell(player,"blocked"); return false; }
            floors=updated; setChanged();
            org.firefly.elevators_escalators.ElevatorsEscalators.LOGGER.info("Elevator {} floor table {}",worldPosition,java.util.Arrays.toString(floors));
            player.displayClientMessage(Component.translatable("message.elevators_escalators.floor_added",offset,floors.length),true); return true;
        } catch (IllegalArgumentException ex) { tell(player,"floor_invalid"); return false; }
    }
    public ElevatorControllerBlockEntity(BlockPos pos, BlockState state) { super(ModContent.CONTROLLER_BE.get(), pos, state); }
    public boolean owns(UUID uuid) { return uuid.equals(platformId); }
    public int height() { return height; }
    public void operate(Player player) {
        if (player.isSpectator() || !(level instanceof ServerLevel server)) return;
        Entity existing = platformId == null ? null : server.getEntity(platformId);
        if (player.isShiftKeyDown()) {
            if (platformId != null) { tell(player, "height_locked"); return; }
            int newHeight=Motion.nextHeight(height); int[] newFloors=FloorPlan.defaults(newHeight);
            if(!AutomaticShaft.rebuild(this,newHeight,newFloors)) { tell(player,"blocked"); return; }
            height=newHeight; floors=newFloors; setChanged();
            player.displayClientMessage(Component.translatable("message.elevators_escalators.height", height), true); return;
        }
        if (platformId != null) {
            if (existing instanceof ElevatorPlatformEntity platform) ElevatorMenu.open(player,platform);
            else tell(player, "unavailable");
            return;
        }
        double y = worldPosition.getY() + 1;
        AABB space = new AABB(worldPosition.getX()-1, y, worldPosition.getZ()-1, worldPosition.getX()+2, y+height+CabinGeometry.TOP, worldPosition.getZ()+2);
        if (y + height + 2.25 >= server.getMaxBuildHeight() || !ElevatorPlatformEntity.clear(server, space)) { tell(player, "blocked"); return; }
        // No conversion of world blocks: a dedicated persistent entity owns the platform.
        ElevatorPlatformEntity platform = new ElevatorPlatformEntity(ModContent.PLATFORM.get(), server);
        platform.bind(worldPosition, height, facing);
        platform.setPos(worldPosition.getX()+0.5, y, worldPosition.getZ()+0.5);
        platformId = platform.getUUID();
        if (!server.addFreshEntity(platform)) platformId = null;
        else {
            // Creation raises the collision surface by 1/4 block. Lift feet out
            // immediately instead of starting the first trip with embedded players.
            AABB deck=platform.getBoundingBox();
            for (Entity rider:server.getEntities(platform,deck.inflate(0,0.3,0),e -> e instanceof net.minecraft.world.entity.LivingEntity
                    && DeckSupport.supported(e.getY(),deck.maxY,e.getDeltaMovement().y))) {
                if (rider.getY()<deck.maxY) {
                    if (rider instanceof net.minecraft.server.level.ServerPlayer sp) sp.connection.teleport(sp.getX(),deck.maxY,sp.getZ(),sp.getYRot(),sp.getXRot());
                    else rider.setPos(rider.getX(),deck.maxY,rider.getZ());
                    rider.fallDistance=0;
                }
            }
        }
        setChanged();
        tell(player, platformId == null ? "unavailable" : "assembled");
    }
    public static void tell(Player player, String key) { player.displayClientMessage(Component.translatable("message.elevators_escalators." + key), true); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); tag.putInt("Height", height); tag.putIntArray("Floors",floors);
        if (platformId != null) tag.putUUID("Platform", platformId);
        tag.putUUID("ShaftOwner",shaftOwner); tag.putString("Facing",facing.getName());
        tag.putLongArray("ShaftCells",shaftCells.stream().mapToLong(BlockPos::asLong).toArray());
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries); int saved = tag.getInt("Height"); height = Motion.validHeight(saved) ? saved : 8; floors=FloorPlan.load(tag.getIntArray("Floors"),height);
        platformId = tag.hasUUID("Platform") ? tag.getUUID("Platform") : null;
        if(tag.hasUUID("ShaftOwner")) shaftOwner=tag.getUUID("ShaftOwner");
        facing=net.minecraft.core.Direction.byName(tag.getString("Facing"));
        if(facing==null || facing.getAxis()==net.minecraft.core.Direction.Axis.Y) facing=net.minecraft.core.Direction.SOUTH;
        shaftCells.clear(); for(long value:tag.getLongArray("ShaftCells")) shaftCells.add(BlockPos.of(value));
    }
}
