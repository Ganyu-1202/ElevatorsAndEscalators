package org.firefly.elevators_escalators.elevator;
import java.util.NavigableSet;
import java.util.TreeSet;
public final class StopQueue {
    private final NavigableSet<Integer> stops=new TreeSet<>();
    public boolean add(int floor){return stops.size()<256 && stops.add(floor);}
    public boolean contains(int floor){return stops.contains(floor);}
    public void served(int floor){stops.remove(floor);}
    public int[] snapshot(){return stops.stream().mapToInt(Integer::intValue).toArray();}
    public Integer next(double position,int direction){
        if(stops.isEmpty())return null;
        if(direction>0){var n=stops.ceiling((int)Math.ceil(position));return n!=null?n:stops.last();}
        if(direction<0){var n=stops.floor((int)Math.floor(position));return n!=null?n:stops.first();}
        return stops.stream().min(java.util.Comparator.<Integer>comparingDouble(v->Math.abs(v-position)).thenComparingInt(v->v)).orElse(null);
    }
    public Integer intercept(double position,int target,double velocity){
        double braking=velocity*velocity/(2*.01)+Math.abs(velocity)*2+.05;
        if(target>position){for(int s:stops)if(s>position+braking && s<target)return s;}
        else if(target<position){for(int s:stops.descendingSet())if(s<position-braking && s>target)return s;}
        return null;
    }
}
