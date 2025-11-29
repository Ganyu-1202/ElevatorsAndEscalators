package org.firefly.elevators_escalators;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import java.util.List;

public class TipsBlockItem extends BlockItem
{
    String toolTipInfo;

    public TipsBlockItem(Block block, Properties properties)
    {
        super(block, properties);
        this.toolTipInfo = null;
    }

    public TipsBlockItem(Block block, Properties properties, String toolTipInfo)
    {
        super(block, properties);
        this.toolTipInfo = toolTipInfo;
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context,
                                @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag)
    {
        if (this.toolTipInfo != null)
        {
            if (Screen.hasAltDown())
            {
                tooltip.add(Component.translatable(this.toolTipInfo).withStyle(ChatFormatting.GRAY));
            }
            else
            {
                tooltip.add(Component.translatable("More_information").withStyle(ChatFormatting.YELLOW));
            }
        }
        else
        {
            tooltip.add(Component.translatable("No_information").withStyle(ChatFormatting.GRAY));
        }
    }
}