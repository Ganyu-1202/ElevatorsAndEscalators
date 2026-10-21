package org.firefly.elevators_escalators.client;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.firefly.elevators_escalators.escalator.*;
public final class EscalatorScreen extends AbstractContainerScreen<EscalatorMenu>{
 private Slider slider;private Button build;private final java.util.List<Button> controls=new java.util.ArrayList<>();private boolean initialized;
 public EscalatorScreen(EscalatorMenu m,Inventory i,Component c){super(m,i,c);imageWidth=250;imageHeight=180;}
 private class Slider extends AbstractSliderButton{Slider(int x,int y){super(x,y,226,20,Component.empty(),.2);updateMessage();}int speed(){return 50+(int)Math.round(value*50)*5;}void setSpeed(int s){value=Math.max(0,Math.min(1,(s-50)/250.0));updateMessage();}protected void updateMessage(){setMessage(Component.literal(String.format(java.util.Locale.ROOT,"速度 / Speed: %.2f 格/s",speed()/100.)));}protected void applyValue(){updateMessage();}}
 private void send(int action){NetworkManager.sendToServer(new EscalatorPayload(menu.containerId,action,slider.speed()));}
 @Override protected void init(){super.init();slider=addRenderableWidget(new Slider(leftPos+12,topPos+45));build=addRenderableWidget(Button.builder(Component.literal("确认建造 / Build"),b->send(0)).bounds(leftPos+12,topPos+75,226,20).build());
 controls.clear();controls.add(addRenderableWidget(Button.builder(Component.literal("上行 / Up"),b->send(1)).bounds(leftPos+12,topPos+70,70,20).build()));controls.add(addRenderableWidget(Button.builder(Component.literal("下行 / Down"),b->send(2)).bounds(leftPos+90,topPos+70,70,20).build()));controls.add(addRenderableWidget(Button.builder(Component.literal("急停 / Stop"),b->send(3)).bounds(leftPos+168,topPos+70,70,20).build()));initialized=false;}
 @Override protected void renderBg(GuiGraphics g,float partial,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFFC6C6C6);}
 @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,12,10,0x404040,false);g.drawString(font,menu.built()?"状态 / Mode: "+(menu.mode()>0?"上行":menu.mode()<0?"下行":"急停"):menu.hasEnd()?"终点已设置 / Endpoint selected":"请先用设置工具选择终点",12,29,0x404040,false);g.drawString(font,"先减速再反向；急停立即停止输送",12,148,0x404040,false);}
 @Override public void render(GuiGraphics g,int x,int y,float partial){if(!initialized&&menu.speed()>=50){slider.setSpeed(menu.speed());initialized=true;}slider.visible=!menu.built();build.visible=!menu.built();for(Button control:controls)control.visible=menu.built();slider.active=!menu.built();build.active=!menu.built()&&menu.hasEnd();super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
