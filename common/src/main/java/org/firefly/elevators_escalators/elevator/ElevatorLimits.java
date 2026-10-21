package org.firefly.elevators_escalators.elevator;

import java.nio.file.*;
import java.util.Properties;

public final class ElevatorLimits {
    public int minSize=1,maxSize=64,maxFloors=256;
    public static final ElevatorLimits INSTANCE=new ElevatorLimits();
    public static void load() {
        Path path=dev.architectury.platform.Platform.getConfigFolder().resolve("elevators_escalators.properties");
        Properties properties=new Properties();
        try {
            if(Files.exists(path)) try(var in=Files.newInputStream(path)) { properties.load(in); }
            INSTANCE.minSize=read(properties,"minCabinSize",1,1,64);
            INSTANCE.maxSize=read(properties,"maxCabinSize",64,INSTANCE.minSize,64);
            INSTANCE.maxFloors=read(properties,"maxFloors",256,1,256);
            if(!Files.exists(path)) {
                properties.setProperty("minCabinSize","1"); properties.setProperty("maxCabinSize","64"); properties.setProperty("maxFloors","256");
                Files.createDirectories(path.getParent());
                try(var out=Files.newOutputStream(path)) { properties.store(out,"Elevator server limits. Restart to apply. Hard safety ceilings: size 64, floors 256."); }
            }
        } catch(Exception ex) { org.firefly.elevators_escalators.ElevatorsEscalators.LOGGER.error("Cannot read elevator limits; using safe defaults",ex); }
    }
    private static int read(Properties p,String key,int fallback,int min,int max) {
        try { return Math.max(min,Math.min(max,Integer.parseInt(p.getProperty(key,""+fallback)))); } catch(NumberFormatException ex) { return fallback; }
    }
}
