package org.firefly.elevators_escalators.elevator;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.firefly.elevators_escalators.ModContent;
import java.util.*;
public final class ElevatorControllerBlockEntity extends BlockEntity {
    private UUID platformId;
    private net.minecraft.core.Direction facing=net.minecraft.core.Direction.SOUTH;
    private ElevatorSpec spec=ElevatorSpec.defaults();
    private boolean configured;
    private int[] floors=spec.offsets(),labels=spec.labels();
    public ElevatorControllerBlockEntity(BlockPos pos,BlockState state){super(ModContent.CONTROLLER_BE.get(),pos,state);}
    public boolean configured(){return configured;}
    public ElevatorSpec spec(){return spec;}
    public Direction facing(){return facing;}
    public void setFacing(Direction d){facing=d;setChanged();}
    public int height(){return spec.travel();}
    public int[] floors(){return floors.clone();}
    public int[] labels(){return labels.clone();}
    public boolean owns(UUID uuid){return uuid.equals(platformId);}
    public ElevatorPlatformEntity platform(){return level instanceof ServerLevel s&&platformId!=null&&s.getEntity(platformId) instanceof ElevatorPlatformEntity p?p:null;}
    // Compatibility only: old automatic frameworks are deliberately no longer dismantled.
    public UUID shaftOwner(){return new UUID(0,0);}
    public Set<BlockPos> shaftCells(){return Set.of();}
    public void setShaftCells(Set<BlockPos> ignored){}
    public boolean addFloor(int offset,Player p){tell(p,"config_locked");return false;}
    public void configure(Player p,ElevatorSpec proposed){
        if(configured||!(level instanceof ServerLevel)||p.isSpectator()||!p.mayBuild())return;
        String error=proposed.error(ElevatorLimits.INSTANCE);
        if(error==null && (worldPosition.getY()+1L+proposed.travel()+proposed.cabinHeight()>level.getMaxBuildHeight()))error="总行程超出世界高度 / Travel exceeds world height";
        if(error!=null){p.displayClientMessage(Component.literal(error),false);return;}
        spec=proposed;floors=spec.offsets();labels=spec.labels();configured=true;setChanged();
        p.closeContainer();preview(p);
        p.displayClientMessage(Component.literal("配置已锁定。轮廓仅作参考；手持轿厢方块右键控制器装配。 / Configuration locked."),false);
    }
    public AABB travelBox(){
        var b=CabinGeometry.rotate(new CabinGeometry.Box(-spec.width()/2.,0,-spec.length()/2.,spec.width()/2.,spec.cabinHeight()+spec.travel(),spec.length()/2.),facing);
        return new AABB(worldPosition.getX()+.5+b.x1(),worldPosition.getY()+1,worldPosition.getZ()+.5+b.z1(),worldPosition.getX()+.5+b.x2(),worldPosition.getY()+1+b.y2(),worldPosition.getZ()+.5+b.z2());
    }
    public void preview(Player player){
        if(!(level instanceof ServerLevel server)||!(player instanceof net.minecraft.server.level.ServerPlayer sp))return;
        AABB a=travelBox().inflate(.05);
        // Sparse particles are private to this player and place no world blocks.
        int steps=32;
        for(int i=0;i<=steps;i++){double t=i/(double)steps;
            for(double x:new double[]{a.minX,a.maxX})for(double z:new double[]{a.minZ,a.maxZ})server.sendParticles(sp,net.minecraft.core.particles.ParticleTypes.END_ROD,true,x,a.minY+t*(a.maxY-a.minY),z,1,0,0,0,0);
            for(double y:new double[]{a.minY,a.maxY})for(double z:new double[]{a.minZ,a.maxZ})server.sendParticles(sp,net.minecraft.core.particles.ParticleTypes.END_ROD,true,a.minX+t*(a.maxX-a.minX),y,z,1,0,0,0,0);
            for(double y:new double[]{a.minY,a.maxY})for(double x:new double[]{a.minX,a.maxX})server.sendParticles(sp,net.minecraft.core.particles.ParticleTypes.END_ROD,true,x,y,a.minZ+t*(a.maxZ-a.minZ),1,0,0,0,0);
        }
    }
    public boolean assemble(Player player){
        if(!configured){tell(player,"configure_first");return false;}
        if(platformId!=null){tell(player,"height_locked");return false;}
        if(!(level instanceof ServerLevel server)||player.isSpectator()||!player.mayBuild())return false;
        if(!ElevatorPlatformEntity.clear(level,travelBox().deflate(1e-5))){tell(player,"blocked");return false;}
        var p=new ElevatorPlatformEntity(ModContent.PLATFORM.get(),server);p.bind(worldPosition,height(),facing);p.applySpec(spec,labels);
        p.setPos(worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5);
        platformId=p.getUUID();if(!server.addFreshEntity(p)){platformId=null;return false;}setChanged();return true;
    }
    public void removeCabin(){var p=platform();if(p!=null)p.discard();}
    public void operate(Player player){if(level.isClientSide||player.isSpectator())return;if(!configured)SetupMenu.open(player,worldPosition);else if(platform()!=null)ElevatorMenu.open(player,platform());else preview(player);}
    public static void tell(Player p,String key){p.displayClientMessage(Component.translatable("message.elevators_escalators."+key),false);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putBoolean("Configured",configured);t.putString("Facing",facing.getName());t.putIntArray("Spec",new int[]{spec.width(),spec.length(),spec.cabinHeight(),spec.minFloor(),spec.maxFloor(),spec.spacing(),spec.speedHundredths()});t.putIntArray("Floors",floors);t.putIntArray("Labels",labels);if(platformId!=null)t.putUUID("Platform",platformId);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);platformId=t.hasUUID("Platform")?t.getUUID("Platform"):null;Direction d=Direction.byName(t.getString("Facing"));facing=d!=null&&d.getAxis()!=Direction.Axis.Y?d:Direction.SOUTH;
        int[] v=t.getIntArray("Spec");configured=t.getBoolean("Configured");if(v.length==5||v.length==6||v.length==7){var candidate=new ElevatorSpec(v[0],v[1],v[2],v[3],v[4],v.length>=6?v[5]:v[2],v.length>=7?v[6]:200);if(candidate.error(new ElevatorLimits())==null)spec=candidate;else configured=false;}
        else if(t.contains("Floors")){int[] old=t.getIntArray("Floors");if(old.length>0){configured=true;spec=new ElevatorSpec(3,3,4,1,Math.min(256,old.length),4);}}
        floors=spec.offsets();labels=spec.labels();if(v.length!=5&&v.length!=6&&v.length!=7&&t.getIntArray("Floors").length>0)floors=t.getIntArray("Floors");
    }
}
