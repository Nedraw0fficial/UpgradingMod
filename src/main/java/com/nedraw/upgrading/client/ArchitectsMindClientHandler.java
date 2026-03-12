package com.nedraw.upgrading.client;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class ArchitectsMindClientHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Check if player has Architect's Mind at L12
        PlayerDiskData data = PlayerDiskData.get(mc.player);
        boolean hasL12 = false;

        for (int slot = 0; slot < 3; slot++) {
            String diskId = data.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("architects_mind")) {
                int level = data.getDiskLevel(diskId);
                if (level >= 12) {
                    hasL12 = true;
                    break;
                }
            }
        }

        if (hasL12) {
            // FORCE INSTANT RIGHT-CLICK!
            // The field is: rightClickDelay in Minecraft class
            // We need to set it to 0 every tick to bypass the 4-tick cooldown

            // Access the rightClickDelay field using reflection
            try {
                var field = Minecraft.class.getDeclaredField("rightClickDelay");
                field.setAccessible(true);
                field.setInt(mc, 0); // Set to 0 for instant clicks
            } catch (Exception e) {
                // Field name might be obfuscated, try common mappings
                try {
                    var field = Minecraft.class.getDeclaredField("f_91074_"); // Obfuscated name
                    field.setAccessible(true);
                    field.setInt(mc, 0);
                } catch (Exception ex) {
                    // Silent fail
                }
            }
        }
    }
}