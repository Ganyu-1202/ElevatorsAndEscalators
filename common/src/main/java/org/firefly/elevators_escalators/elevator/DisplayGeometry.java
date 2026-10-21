package org.firefly.elevators_escalators.elevator;

import net.minecraft.core.Direction;
import java.util.ArrayList;
import java.util.List;

/** All dimensions are defined in the proven south-facing panel space, then rotated as boxes. */
public final class DisplayGeometry {
    public static final double DEPTH=.012;
    private DisplayGeometry() {}
    public static CabinGeometry.Box housing(Direction facing) {
        return CabinGeometry.rotate(new CabinGeometry.Box(-.42,2.10,-1.374,.42,2.46,-1.349),facing);
    }
    public static CabinGeometry.Box segment(double centerX,double y,double width,double height,Direction facing) {
        return CabinGeometry.rotate(new CabinGeometry.Box(centerX-width/2,y,-1.337-DEPTH/2,
            centerX+width/2,y+height,-1.337+DEPTH/2),facing);
    }
    public static List<CabinGeometry.Box> glyphs(int floor,int direction,Direction facing) {
        List<CabinGeometry.Box> result=new ArrayList<>();
        double[][] bars={{.02,.24,.10,.018},{.12,.13,.018,.11},{.12,.02,.018,.11},
            {.02,0,.10,.018},{0,.02,.018,.11},{0,.13,.018,.11},{.02,.12,.10,.018}};
        String number=SegmentDisplay.floor(floor);
        for(int i=0;i<number.length();i++) {
            int mask=number.charAt(i)=='-'?64:SegmentDisplay.digit(number.charAt(i)-'0');
            for(int j=0;j<7;j++) if((mask&(1<<j))!=0) {
                double[] b=bars[j];
                result.add(segment(-.26+i*.17+b[0]+b[2]/2,2.15+b[1],b[2],b[3],Direction.SOUTH));
            }
        }
        result.add(segment(.12,2.15,.018,.258,Direction.SOUTH));
        result.add(segment(.175,2.39,.11,.018,Direction.SOUTH));
        result.add(segment(.165,2.27,.09,.018,Direction.SOUTH));
        if(direction!=0) for(int i=0;i<3;i++) {
            double y=direction>0?2.34-i*.035:2.21+i*.035;
            result.add(segment(.30-i*.025,y,.025,.025,Direction.SOUTH));
            result.add(segment(.30+i*.025,y,.025,.025,Direction.SOUTH));
        }
        double factor=number.length()>2?2.0/number.length():1;
        return result.stream().map(b->CabinGeometry.rotate(new CabinGeometry.Box(b.x1()*factor,b.y1(),b.z1(),b.x2()*factor,b.y2(),b.z2()),facing)).toList();
    }
}
