package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

public class ArchitectsMindDisk extends UpgradeDisk {

    private static final ResourceLocation REACH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "architects_mind_reach");

    public ArchitectsMindDisk() {
        super("architects_mind", "Architect's Mind", DiskRarity.LEGENDARY);
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Get reach bonus based on level
        double reachBonus = getReachBonus(level);

        // Apply to BLOCK_INTERACTION_RANGE attribute (for block placement/breaking)
        var reachAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reachAttr != null) {
            reachAttr.removeModifier(REACH_MODIFIER_ID);

            AttributeModifier reachModifier = new AttributeModifier(
                    REACH_MODIFIER_ID,
                    reachBonus,
                    AttributeModifier.Operation.ADD_VALUE
            );

            reachAttr.addPermanentModifier(reachModifier);
        }
    }

    @Override
    public void removeEffect(Player player) {
        var reachAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reachAttr != null) {
            reachAttr.removeModifier(REACH_MODIFIER_ID);
        }
    }

    private double getReachBonus(int level) {
        return switch (level) {
            case 9 -> 1.0;
            case 10 -> 2.0;
            case 11 -> 3.5;
            case 12 -> 5.5;
            default -> 0.0;
        };
    }
}