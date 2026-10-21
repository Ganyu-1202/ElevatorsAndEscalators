package org.firefly.elevators_escalators.elevator;

public record ElevatorSpec(int width,int length,int cabinHeight,int minFloor,int maxFloor,int spacing) {
    public static ElevatorSpec defaults() { return new ElevatorSpec(3,3,4,1,3,4); }
    public int count() {
        long n=(long)maxFloor-minFloor+1-(minFloor<0 && maxFloor>0?1:0);
        return (int)Math.max(0,Math.min(Integer.MAX_VALUE,n));
    }
    public String error(ElevatorLimits limits) {
        if(width<limits.minSize || length<limits.minSize || cabinHeight<limits.minSize || width>limits.maxSize || length>limits.maxSize || cabinHeight>limits.maxSize) return "轿厢尺寸超出服务器限制 / Cabin size out of bounds";
        if(minFloor==0 || maxFloor==0 || minFloor>maxFloor || minFloor< -32767 || maxFloor>32767) return "楼层范围不合法（不能以0层为端点，编号范围±32767） / Invalid floor labels";
        if(count()<1 || count()>limits.maxFloors) return "楼层数量超出服务器限制 / Too many floors";
        if(spacing<cabinHeight || spacing>256) return "层间距不能小于轿厢高度，且不得超过256格 / Spacing must be >= cabin height and <= 256";
        return null;
    }
    public int[] labels() { return java.util.stream.IntStream.rangeClosed(minFloor,maxFloor).filter(i->i!=0).toArray(); }
    public int[] offsets() { return java.util.stream.IntStream.range(0,count()).map(i->i*spacing).toArray(); }
    public int travel() { return (count()-1)*spacing; }
    public double sx() { return width/3.0; }
    public double sy() { return cabinHeight/CabinGeometry.TOP; }
    public double sz() { return length/3.0; }
    public double deckThickness() { return .25*sy(); }
    public CabinGeometry.Box scale(CabinGeometry.Box b) { return new CabinGeometry.Box(b.x1()*sx(),b.y1()*sy(),b.z1()*sz(),b.x2()*sx(),b.y2()*sy(),b.z2()*sz()); }
}
