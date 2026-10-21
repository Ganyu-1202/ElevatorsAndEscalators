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
    private SpeedSlider speedSlider;
    private boolean automatic=true;
    public SetupScreen(SetupMenu menu,Inventory inventory,Component title) { super(menu,inventory,title);imageWidth=290;imageHeight=287; }
    private final class SpeedSlider extends AbstractSliderButton {
        SpeedSlider(int x,int y){super(x,y,180,20,Component.empty(),.2);setSpeed(200);}
        int speed(){return ElevatorSpeed.fromSlider(value);}
        void setSpeed(int v){value=(v-ElevatorSpeed.MIN)/(double)(ElevatorSpeed.MAX-ElevatorSpeed.MIN);updateMessage();}
        @Override protected void updateMessage(){setMessage(Component.literal(String.format(java.util.Locale.ROOT,"速度 / Speed: %.2f 格/s%s",speed()/100.0,automatic?" (推荐)":"")));}
        @Override protected void applyValue(){automatic=false;updateMessage();}
    }
    private void recommend(){
        if(!automatic||speedSlider==null)return;
        try{int min=Integer.parseInt(fields[3].getValue().trim()),max=Integer.parseInt(fields[4].getValue().trim());
            if(min!=0&&max!=0&&max>=min){long count=(long)max-min+1-(min<0&&max>0?1:0);speedSlider.setSpeed(ElevatorSpeed.recommended((int)Math.min(256,count)));}
        }catch(NumberFormatException ignored){}
    }
    @Override protected void init() {
        super.init();String[] defaults={"3","3","4","1","3","4"};
        for(int i=0;i<6;i++) { fields[i]=new EditBox(font,leftPos+155,topPos+30+i*24,120,20,Component.empty());fields[i].setMaxLength(7);fields[i].setValue(defaults[i]);addRenderableWidget(fields[i]); }
        speedSlider=addRenderableWidget(new SpeedSlider(leftPos+10,topPos+178));
        addRenderableWidget(Button.builder(Component.literal("推荐 / Auto"),b->{automatic=true;recommend();}).bounds(leftPos+195,topPos+178,80,20).build());
        fields[3].setResponder(v->recommend());fields[4].setResponder(v->recommend());recommend();
        addRenderableWidget(Button.builder(Component.literal("确认并锁定 / Lock"),b->{
            try {
                int[] v=java.util.Arrays.stream(fields).mapToInt(f->Integer.parseInt(f.getValue().trim())).toArray();
                ElevatorLimits limits=new ElevatorLimits();limits.minSize=menu.minSize;limits.maxSize=menu.maxSize;limits.maxFloors=menu.maxFloors;
                var spec=new ElevatorSpec(v[0],v[1],v[2],v[3],v[4],v[5],speedSlider.speed());error=spec.error(limits);
                if(error!=null)return;
                NetworkManager.sendToServer(new SetupPayload(menu.containerId,String.join(",",java.util.Arrays.stream(fields).map(f->f.getValue().trim()).toArray(String[]::new))+","+speedSlider.speed()));
            }catch(NumberFormatException ex){error="请输入整数 / Integers only";}
        }).bounds(leftPos+55,topPos+232,180,20).build());
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFFC6C6C6);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){
        g.drawString(font,title,8,8,0x404040,false);
        String[] labels={"宽 / Width","长 / Length","高 / Height","最低层 / Min floor","最高层 / Max floor","层间距 / Spacing"};
        for(int i=0;i<6;i++)g.drawString(font,labels[i],10,36+i*24,0x404040,false);
        g.drawString(font,"尺寸 "+menu.minSize+"–"+menu.maxSize+"; 层数≤"+menu.maxFloors+"; 无0层",8,207,0x404040,false);
        if(error!=null&&!error.isEmpty())g.drawString(font,font.plainSubstrByWidth(error,272),8,265,0xAA0000,false);
    }
    @Override public void render(GuiGraphics g,int x,int y,float partial){super.render(g,x,y,partial);}
}
