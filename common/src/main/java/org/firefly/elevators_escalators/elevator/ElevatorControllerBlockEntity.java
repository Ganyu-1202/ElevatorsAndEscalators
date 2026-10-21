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
    private boolean paidCabin;
    public void recordCabinPayment(){paidCabin=true;setChanged();}
    public void refundMaterials(){
        if(!paidCabin)return;paidCabin=false;setChanged();
        org.firefly.elevators_escalators.ConstructionMaterials.drop(level,worldPosition,ModContent.CABIN_ITEM.get(),1);
    }
    private final java.util.Set<UUID> previewPlayers=new java.util.HashSet<>();
    private final java.util.Set<BlockPos> generatedPanels=new java.util.HashSet<>();
    private int previewTimer;
    public void previewTick(){
        if(!configured||platformId!=null||!(level instanceof ServerLevel server)||++previewTimer%20!=0)return;
        for(var player:server.players())if(travelBox().inflate(64).contains(player.position()))preview(player);
    }
    private FrameBlueprint blueprint(){return FrameBlueprint.create(travelBox(),floors,facing);}
    private FrameBlueprint.Status validateFrame(Player player){
        var plan=blueprint();int air=0,full=0;BlockPos missing=null;
        for(BlockPos pos:plan.cells()){
            if(level.isOutsideBuildHeight(pos)||!level.getWorldBorder().isWithinBounds(pos)||!level.hasChunkAt(pos)){player.displayClientMessage(Component.literal("立柱范围越界或未加载："+pos.toShortString()),false);return FrameBlueprint.Status.PARTIAL;}
            var state=level.getBlockState(pos);
            if(state.isAir())air++;else if(state.isCollisionShapeFullBlock(level,pos)&&level.getFluidState(pos).isEmpty())full++;
            if((state.isAir()||!state.isCollisionShapeFullBlock(level,pos))&&missing==null)missing=pos;
        }
        var status=FrameBlueprint.classify(air,full,plan.cells().size());
        if(status==FrameBlueprint.Status.PARTIAL)player.displayClientMessage(Component.literal("四根立柱必须全部不建或连续完整搭建。检查："+(missing==null?"材质非完整方块":missing.toShortString())),false);
        return status;
    }
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
        previewPlayers.add(player.getUUID());
        // Preview the exact full-block cells accepted by the four-column validator.
        var cells=blueprint().cells(); int stride=Math.max(1,(cells.size()+511)/512),sample=0;
        for(BlockPos cell:cells){
            if(sample++%stride!=0)continue;
            double x=cell.getX(),y=cell.getY(),z=cell.getZ();
            for(double dx:new double[]{0,1})for(double dz:new double[]{0,1})
                server.sendParticles(sp,net.minecraft.core.particles.ParticleTypes.END_ROD,true,x+dx,y+.5,z+dz,1,0,0,0,0);
        }

    }
    public boolean assemble(Player player){
        if(!configured){tell(player,"configure_first");return false;}
        if(platformId!=null){tell(player,"height_locked");return false;}
        if(!(level instanceof ServerLevel server)||player.isSpectator()||!player.mayBuild())return false;
        if(!ElevatorPlatformEntity.clear(level,travelBox().deflate(1e-5))){tell(player,"blocked");return false;}
        var frameStatus=validateFrame(player);if(frameStatus==FrameBlueprint.Status.PARTIAL)return false;
        var plan=blueprint();
        if(frameStatus==FrameBlueprint.Status.COMPLETE)for(BlockPos pos:plan.panels()){
            if(level.isOutsideBuildHeight(pos)||!level.hasChunkAt(pos)||!level.getBlockState(pos).isAir()||!level.mayInteract(player,pos)||!player.mayUseItemAt(pos,facing,player.getMainHandItem())){
                player.displayClientMessage(Component.literal("呼梯面板位置受阻或无权限："+pos.toShortString()),false);return false;
            }
        }
        var p=new ElevatorPlatformEntity(ModContent.PLATFORM.get(),server);p.bind(worldPosition,height(),facing);p.applySpec(spec,labels);
        p.setPos(worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5);
        platformId=p.getUUID();if(!server.addFreshEntity(p)){platformId=null;return false;}
        if(frameStatus==FrameBlueprint.Status.COMPLETE){
            for(int i=0;i<plan.panels().size();i++){
                BlockPos pos=plan.panels().get(i);
                if(!server.setBlock(pos,ModContent.HALL.get().defaultBlockState().setValue(HallPanelBlock.FACING,facing),3)||!(server.getBlockEntity(pos) instanceof HallPanelBlockEntity panel)){
                    removeCabin();platformId=null;player.displayClientMessage(Component.literal("面板创建失败，装配已撤回。"),false);return false;
                }
                generatedPanels.add(pos);panel.bind(worldPosition,platformId,i);
            }
        }
        previewPlayers.clear();setChanged();return true;
    }
    public void removeCabin(){var p=platform();if(p!=null)p.discard();
        if(level instanceof ServerLevel server)for(BlockPos pos:generatedPanels){server.getChunk(pos.getX()>>4,pos.getZ()>>4);if(server.getBlockEntity(pos) instanceof HallPanelBlockEntity panel&&panel.belongs(worldPosition,platformId))server.removeBlock(pos,false);}
        generatedPanels.clear();
    }
    public void operate(Player player){if(level.isClientSide||player.isSpectator())return;if(!configured)SetupMenu.open(player,worldPosition);else if(platform()!=null)ElevatorMenu.open(player,platform());else preview(player);}
    public static void tell(Player p,String key){p.displayClientMessage(Component.translatable("message.elevators_escalators."+key),false);}
    @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putBoolean("PaidCabin",paidCabin);t.putLongArray("HallPanels",generatedPanels.stream().mapToLong(BlockPos::asLong).toArray());t.putBoolean("Configured",configured);t.putString("Facing",facing.getName());t.putIntArray("Spec",new int[]{spec.width(),spec.length(),spec.cabinHeight(),spec.minFloor(),spec.maxFloor(),spec.spacing(),spec.speedHundredths()});t.putIntArray("Floors",floors);t.putIntArray("Labels",labels);if(platformId!=null)t.putUUID("Platform",platformId);}
    @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);paidCabin=t.getBoolean("PaidCabin");platformId=t.hasUUID("Platform")?t.getUUID("Platform"):null;generatedPanels.clear();for(long pos:t.getLongArray("HallPanels"))generatedPanels.add(BlockPos.of(pos));Direction d=Direction.byName(t.getString("Facing"));facing=d!=null&&d.getAxis()!=Direction.Axis.Y?d:Direction.SOUTH;
        int[] v=t.getIntArray("Spec");configured=t.getBoolean("Configured");if(v.length==5||v.length==6||v.length==7){var candidate=new ElevatorSpec(v[0],v[1],v[2],v[3],v[4],v.length>=6?v[5]:v[2],v.length>=7?v[6]:200);if(candidate.error(new ElevatorLimits())==null)spec=candidate;else configured=false;}
        else if(t.contains("Floors")){int[] old=t.getIntArray("Floors");if(old.length>0){configured=true;spec=new ElevatorSpec(3,3,4,1,Math.min(256,old.length),4);}}
        floors=spec.offsets();labels=spec.labels();if(v.length!=5&&v.length!=6&&v.length!=7&&t.getIntArray("Floors").length>0)floors=t.getIntArray("Floors");
    }
}
