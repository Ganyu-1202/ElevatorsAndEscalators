package org.firefly.elevators_escalators.elevator;
import java.util.*;
public final class StopQueue {
    private final NavigableSet<Integer> stops=new TreeSet<>(),up=new TreeSet<>(),down=new TreeSet<>();
    public boolean add(int floor){return stops.size()<256&&stops.add(floor);}
    public boolean addHall(int floor,int direction){var s=direction>0?up:down;return s.size()<256&&s.add(floor);}
    public boolean contains(int floor){return stops.contains(floor)||up.contains(floor)||down.contains(floor);}
    public void served(int floor){stops.remove(floor);up.remove(floor);down.remove(floor);}
    public void served(int floor,int direction){stops.remove(floor);if(direction>=0)up.remove(floor);if(direction<=0)down.remove(floor);}
    public int[] snapshot(){TreeSet<Integer> all=new TreeSet<>(stops);all.addAll(up);all.addAll(down);return all.stream().mapToInt(Integer::intValue).toArray();}
    public int[] cabinSnapshot(){return stops.stream().mapToInt(Integer::intValue).toArray();}
    public int[] hallSnapshot(int direction){return (direction>0?up:down).stream().mapToInt(Integer::intValue).toArray();}
    public boolean ahead(double position,int direction){return Arrays.stream(snapshot()).anyMatch(s->direction>0?s>position:s<position);}
    public Integer next(double position,int direction){
        TreeSet<Integer> all=new TreeSet<>();for(int s:snapshot())all.add(s);if(all.isEmpty())return null;
        TreeSet<Integer> compatible=new TreeSet<>(stops);compatible.addAll(direction>0?up:down);
        if(direction>0){var n=compatible.ceiling((int)Math.ceil(position));if(n!=null)return n;if(all.last()>position)return all.last();return all.floor((int)Math.floor(position));}
        if(direction<0){var n=compatible.floor((int)Math.floor(position));if(n!=null)return n;if(all.first()<position)return all.first();return all.ceiling((int)Math.ceil(position));}
        return all.stream().min(Comparator.<Integer>comparingDouble(v->Math.abs(v-position)).thenComparingInt(v->v)).orElse(null);
    }
    public Integer intercept(double position,int target,double velocity){
        double braking=velocity*velocity/(2*.01)+Math.abs(velocity)*2+.05;
        TreeSet<Integer> eligible=new TreeSet<>(stops);eligible.addAll(target>position?up:down);
        if(target>position){for(int s:eligible)if(s>position+braking&&s<target)return s;}
        else if(target<position){for(int s:eligible.descendingSet())if(s<position-braking&&s>target)return s;}
        return null;
    }
}
