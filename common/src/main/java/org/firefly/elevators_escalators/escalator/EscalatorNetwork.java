package org.firefly.elevators_escalators.escalator;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Per-dimension durable registry. Never requests a chunk load. */
public final class EscalatorNetwork extends SavedData {
    public static final int BUDGET=64;
    private final Map<UUID,Route> routes=new LinkedHashMap<>();
    private final Deque<Map.Entry<Route,BlockPos>> work=new ArrayDeque<>();
    private boolean rebuildWork=true;
    static final class Route {
        UUID owner; BlockPos controller; List<EscalatorPlan.Step> plan;
        Map<BlockPos,Integer> indices=new HashMap<>();
        EscalatorMotion motion; boolean deleting, broken;
        Set<BlockPos> pending=new LinkedHashSet<>();
        Route(UUID id,BlockPos c,List<EscalatorPlan.Step> p,EscalatorMotion m){owner=id;controller=c;plan=p;motion=m;for(int i=0;i<p.size();i++)indices.put(p.get(i).pos(),i);}
    }
    public static EscalatorNetwork get(ServerLevel level){
        return level.getDataStorage().computeIfAbsent(new Factory<>(EscalatorNetwork::new,EscalatorNetwork::load,null),"elevators_escalators_routes");
    }
    public void register(ServerLevel level,UUID owner,BlockPos controller,List<EscalatorPlan.Step> plan,int mode,int speed){
        Route r=routes.get(owner);
        if(r==null){r=new Route(owner,controller.immutable(),plan,new EscalatorMotion(level.getGameTime(),0,0,mode*speed/2000.0));routes.put(owner,r);rebuildWork=true;setDirty();}
        // Do not overwrite a tombstone when a stale controller chunk is loaded.
        publish(level,r);
    }
    public void change(ServerLevel level,UUID owner,int mode,int speed){
        Route r=routes.get(owner);if(r==null||r.deleting||r.broken)return;
        r.motion=r.motion.retarget(level.getGameTime(),mode*speed/2000.0);setDirty();publish(level,r);
    }
    public boolean stopped(UUID owner){Route r=routes.get(owner);return r==null||r.deleting||r.broken;}
    public void delete(ServerLevel level,UUID owner,BlockPos controller,Collection<BlockPos> generated){
        Route r=routes.get(owner);
        if(r==null){r=new Route(owner,controller,List.of(),new EscalatorMotion(level.getGameTime(),0,0,0));routes.put(owner,r);}
        r.deleting=true;r.pending.addAll(generated);rebuildWork=true;r.motion=new EscalatorMotion(level.getGameTime(),r.motion.distanceAt(level.getGameTime()),0,0);
        setDirty();publish(level,r);
    }
    public void refresh(ServerLevel level,UUID owner,EscalatorStepBlockEntity step){
        Route r=routes.get(owner);if(r==null||!step.owned(r.controller,r.owner))return;
        Integer index=r.indices.get(step.getBlockPos());
        if(index!=null)step.configure(r.plan,index,r.motion,r.deleting||r.broken);
    }
    public void fault(ServerLevel level,UUID owner){
        Route r=routes.get(owner);if(r==null||r.deleting||r.broken)return;
        r.broken=true;r.motion=new EscalatorMotion(level.getGameTime(),r.motion.distanceAt(level.getGameTime()),0,0);
        setDirty();publish(level,r);
    }
    private void publish(ServerLevel level,Route r){
        for(int i=0;i<r.plan.size();i++){
            var p=r.plan.get(i).pos();
            if(level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof EscalatorStepBlockEntity step&&step.owned(r.controller,r.owner))
                step.configure(r.plan,i,r.motion,r.deleting||r.broken);
        }
    }
    public void tick(ServerLevel level){
        // Rebuild only on route changes, not every tick. Rotate at most 64 cells globally.
        if(rebuildWork){
            work.clear();for(Route r:routes.values()){
                if(r.deleting){for(var p:r.pending)work.add(Map.entry(r,p));}
                else for(var s:r.plan)work.add(Map.entry(r,s.pos()));
            }rebuildWork=false;
        }
        int count=Math.min(BUDGET,work.size());
        for(int n=0;n<count;n++){
            var entry=work.removeFirst();var r=entry.getKey();var p=entry.getValue();
            if(r.deleting&&!r.pending.contains(p))continue;
            work.addLast(entry);
            if(!level.hasChunkAt(p))continue;
            if(!r.deleting&&level.hasChunkAt(r.controller)){
                var control=level.getBlockEntity(r.controller);
                if(!(control instanceof EscalatorControllerBlockEntity c)||!c.id().equals(r.owner)){
                    delete(level,r.owner,r.controller,r.plan.stream().map(EscalatorPlan.Step::pos).toList());
                }
            }
            var be=level.getBlockEntity(p);
            if(r.deleting){
                if(be instanceof EscalatorStepBlockEntity step&&step.owned(r.controller,r.owner))level.removeBlock(p,false);
                if(r.pending.remove(p))setDirty();
            }else if(!(be instanceof EscalatorStepBlockEntity step)||!step.owned(r.controller,r.owner)){
                fault(level,r.owner);
            }else {
                step.configure(r.plan,r.indices.get(p),r.motion,r.broken);
            }
        }
        // Retain deletion tombstones: stale/unloaded segments must never resume transport.
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider lookup){
        ListTag list=new ListTag();
        for(Route r:routes.values()){
            CompoundTag t=new CompoundTag();t.putUUID("Owner",r.owner);t.putLong("Controller",r.controller.asLong());
            ListTag steps=new ListTag();for(var s:r.plan){CompoundTag a=new CompoundTag();a.putLong("Pos",s.pos().asLong());a.putInt("Facing",s.up().get2DDataValue());a.putBoolean("Rises",s.rises());steps.add(a);}t.put("Steps",steps);
            EscalatorStepBlockEntity.writeMotion(t,r.motion);t.putBoolean("Deleting",r.deleting);t.putBoolean("Broken",r.broken);
            t.putLongArray("Pending",r.pending.stream().mapToLong(BlockPos::asLong).toArray());list.add(t);
        }
        tag.put("Routes",list);return tag;
    }
    private static EscalatorNetwork load(CompoundTag tag,HolderLookup.Provider lookup){
        var data=new EscalatorNetwork();for(Tag raw:tag.getList("Routes",Tag.TAG_COMPOUND)){
            CompoundTag t=(CompoundTag)raw;var plan=EscalatorStepBlockEntity.readPlan(t);
            Route r=new Route(t.getUUID("Owner"),BlockPos.of(t.getLong("Controller")),plan,EscalatorStepBlockEntity.readMotion(t));
            r.deleting=t.getBoolean("Deleting");r.broken=t.getBoolean("Broken");for(long p:t.getLongArray("Pending"))r.pending.add(BlockPos.of(p));data.routes.put(r.owner,r);
        }return data;
    }
}
