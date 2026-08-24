package com.nedraw.upgrading.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ZSlotComponentItem extends Item {

    private final ZSlotComponentType componentType;
    private final String componentId;

    public ZSlotComponentItem(ZSlotComponentType componentType, String componentId, Properties properties) {
        super(properties);
        this.componentType = componentType;
        this.componentId = componentId;
    }

    public ZSlotComponentType getComponentType() {
        return componentType;
    }

    public String getComponentId() {
        return componentId;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        String key = "tooltip.upgrading." + componentType.name().toLowerCase() + "." + componentId;
        tooltipComponents.add(
                Component.translatable(key)
                        .withStyle(style -> style.withColor(0xAAAAAA))
        );
    }
}