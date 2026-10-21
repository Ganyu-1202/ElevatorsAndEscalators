package org.firefly.elevators_escalators.client;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.firefly.elevators_escalators.elevator.*;
public final class SetupScreen extends AbstractContainerScreen<SetupMenu> {
    private final EditBox[] fields=new EditBox[6];
    private String error="";
    public SetupScreen(SetupMenu menu,Inventory inventory,Component title) { super(menu,inventory,title);imageWidth=270;imageHeight=257; }
    @Override protected void init() {
        super.init();String[] defaults={"3","3","4","1","3","4"};
        for(int i=0;i<6;i++) { fields[i]=new EditBox(font,leftPos+145,topPos+32+i*27,108,20,Component.empty()); fields[i].setMaxLength(7);fields[i].setValue(defaults[i]);addRenderableWidget(fields[i]); }
        addRenderableWidget(Button.builder(Component.literal("确认并锁定 / Lock"),b->{
            try {
                int[] v=java.util.Arrays.stream(fields).mapToInt(f->Integer.parseInt(f.getValue().trim())).toArray();
                ElevatorLimits limits=new ElevatorLimits(); limits.minSize=menu.minSize;limits.maxSize=menu.maxSize;limits.maxFloors=menu.maxFloors;
                var spec=new ElevatorSpec(v[0],v[1],v[2],v[3],v[4],v[5]);error=spec.error(limits);
                if(error!=null)return;
                NetworkManager.sendToServer(new SetupPayload(menu.containerId,String.join(",",java.util.Arrays.stream(fields).map(f->f.getValue().trim()).toArray(String[]::new))));
            } catch(NumberFormatException ex) { error="请输入整数 / Integers only"; }
        }).bounds(leftPos+45,topPos+203,180,20).build());
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int x,int y) {g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFFC6C6C6);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y) {
        g.drawString(font,title,8,8,0x404040,false);
        String[] labels={"宽 / Width","长 / Length","高 / Height","最低层 / Min floor","最高层 / Max floor","层间距 / Spacing"};
        for(int i=0;i<6;i++)g.drawString(font,labels[i],10,38+i*27,0x404040,false);
        g.drawString(font,"尺寸 "+menu.minSize+"–"+menu.maxSize+"; 层数≤"+menu.maxFloors+"; 无0层",8,187,0x404040,false);
        if(error!=null&&!error.isEmpty()) g.drawString(font,font.plainSubstrByWidth(error,252),8,232,0xAA0000,false);
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial) {super.render(g,x,y,partial);}
}
