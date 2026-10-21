package org.firefly.elevators_escalators;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class ConstructionMaterials {
    private ConstructionMaterials(){}
    public static int count(Player player,Item item){
        int count=0;for(int i=0;i<player.getInventory().getContainerSize();i++){
            var stack=player.getInventory().getItem(i);if(stack.is(item))count+=stack.getCount();
        }return count;
    }
    public static boolean take(Player player,Item item,int amount){
        if(count(player,item)<amount)return false;
        for(int i=0;i<player.getInventory().getContainerSize()&&amount>0;i++){
            var stack=player.getInventory().getItem(i);if(!stack.is(item))continue;
            int take=Math.min(amount,stack.getCount());stack.shrink(take);amount-=take;
        }
        player.getInventory().setChanged();return true;
    }
    public static void drop(Level level,BlockPos pos,Item item,int amount){
        while(amount>0){int size=Math.min(64,amount);Block.popResource(level,pos,new ItemStack(item,size));amount-=size;}
    }
}
