package org.firefly.elevators_escalators.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.firefly.elevators_escalators.elevator.ElevatorMenu;
public final class ElevatorScreen extends AbstractContainerScreen<ElevatorMenu> {
    private int row;private boolean dragging;private final Button[] buttons=new Button[16];
    public ElevatorScreen(ElevatorMenu menu,Inventory inventory,Component title){super(menu,inventory,title);imageWidth=228;imageHeight=208;}
    private int maxRow(){return Math.max(0,(menu.floors().length+3)/4-4);}
    private void send(int i){dev.architectury.networking.NetworkManager.sendToServer(new org.firefly.elevators_escalators.elevator.ControlPayload(menu.containerId,i));}
    @Override protected void init(){super.init();for(int i=0;i<16;i++){final int slot=i;buttons[i]=addRenderableWidget(Button.builder(Component.empty(),b->send(row*4+slot)).bounds(leftPos+10+(i%4)*50,topPos+53+(i/4)*25,47,20).build());}
        addRenderableWidget(Button.builder(Component.translatable("screen.elevators_escalators.open"),b->send(1000)).bounds(leftPos+10,topPos+163,95,20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.elevators_escalators.close"),b->send(1001)).bounds(leftPos+111,topPos+163,95,20).build());update();}
    private void update(){int[] labels=menu.labels();for(int i=0;i<16;i++){int index=row*4+i;buttons[i].visible=index<labels.length;if(index<labels.length)buttons[i].setMessage(Component.literal((menu.selected(index)?"* ":"")+labels[index]+"F"));}}
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){row=Math.max(0,Math.min(maxRow(),row-(int)Math.signum(vertical)));update();return true;}
    private void seek(double y){row=(int)Math.round(Math.max(0,Math.min(1,(y-topPos-53)/90))*maxRow());update();}
    @Override public boolean mouseClicked(double x,double y,int button){if(button==0&&x>=leftPos+212&&x<=leftPos+224&&y>=topPos+53&&y<=topPos+148){dragging=true;seek(y);return true;}return super.mouseClicked(x,y,button);}
    @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){if(dragging){seek(y);return true;}return super.mouseDragged(x,y,b,dx,dy);}
    @Override public boolean mouseReleased(double x,double y,int button){dragging=false;return super.mouseReleased(x,y,button);}
    @Override protected void renderBg(GuiGraphics g,float partial,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFFC6C6C6);g.fill(leftPos+10,topPos+23,leftPos+206,topPos+46,0xFF1B262A);g.fill(leftPos+213,topPos+53,leftPos+222,topPos+148,0xFF555555);int yy=topPos+53+(maxRow()==0?0:row*80/maxRow());g.fill(leftPos+213,yy,leftPos+222,yy+15,0xFFEEEEEE);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,10,9,0x404040,false);g.drawString(font,Component.translatable("screen.elevators_escalators.status",menu.currentFloor(),menu.target()),15,30,0xB5FFCF,false);g.drawString(font,Component.translatable(menu.closed()?"screen.elevators_escalators.closed":"screen.elevators_escalators.opened"),10,191,0x404040,false);}
    @Override public void render(GuiGraphics g,int x,int y,float partial){update();super.render(g,x,y,partial);renderTooltip(g,x,y);}
}
