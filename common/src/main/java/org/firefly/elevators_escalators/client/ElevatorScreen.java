package org.firefly.elevators_escalators.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.firefly.elevators_escalators.elevator.ElevatorMenu;

public final class ElevatorScreen extends AbstractContainerScreen<ElevatorMenu> {
    public ElevatorScreen(ElevatorMenu menu,Inventory inventory,Component title) { super(menu,inventory,title); imageWidth=216; imageHeight=208; }
    private void send(int button) { if (minecraft!=null && minecraft.gameMode!=null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId,button); }
    @Override protected void init() {
        super.init();
        int[] floors=menu.floors();
        for(int i=0;i<floors.length;i++) {
            final int index=i;
            addRenderableWidget(Button.builder(Component.literal((i+1)+"F (+"+floors[i]+")"),b -> send(index))
                .bounds(leftPos+10+(i%4)*50,topPos+53+(i/4)*25,47,20).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("screen.elevators_escalators.open"),b -> send(100)).bounds(leftPos+10,topPos+163,95,20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.elevators_escalators.close"),b -> send(101)).bounds(leftPos+111,topPos+163,95,20).build());
    }
    @Override protected void renderBg(GuiGraphics graphics,float partial,int x,int y) {
        graphics.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF373737);
        graphics.fill(leftPos+2,topPos+2,leftPos+imageWidth-2,topPos+imageHeight-2,0xFFFFFFFF);
        graphics.fill(leftPos+4,topPos+4,leftPos+imageWidth-4,topPos+imageHeight-4,0xFFC6C6C6);
        graphics.fill(leftPos+10,topPos+23,leftPos+206,topPos+46,0xFF1B262A);
    }
    @Override protected void renderLabels(GuiGraphics graphics,int x,int y) {
        graphics.drawString(font,title,10,9,0x404040,false);
        graphics.drawString(font,Component.translatable("screen.elevators_escalators.status",menu.currentFloor(),menu.target()),15,30,0xB5FFCF,false);
        graphics.drawString(font,Component.translatable(menu.closed()?"screen.elevators_escalators.closed":"screen.elevators_escalators.opened"),10,191,0x404040,false);
    }
    @Override public void render(GuiGraphics graphics,int x,int y,float partial) { super.render(graphics,x,y,partial); renderTooltip(graphics,x,y); }
}
