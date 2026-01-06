package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.PyroclasmDisk;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Random;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class PyroclasmHandler {

    private static final Random RANDOM = new Random();

    // When player attacks enemy
    @SubscribeEvent
    public static void onPlayerAttack(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (player.level().isClientSide) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if ("pyroclasm".equals(diskId)) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof PyroclasmDisk pyroclasm) {
                    int level = diskData.getDiskLevel(diskId);
                    float fireChance = pyroclasm.getFireChance(level);

                    // Roll for igniting target
                    if (RANDOM.nextFloat() < fireChance) {
                        target.setRemainingFireTicks(200); // 10 seconds

                        // Play sound
                        player.level().playSound(
                                null,
                                target.blockPosition(),
                                net.minecraft.sounds.SoundEvents.FIRECHARGE_USE,
                                net.minecraft.sounds.SoundSource.PLAYERS,
                                0.5f,
                                1.2f
                        );
                    }
                }
                break;
            }
        }
    }

    // When player takes damage
    @SubscribeEvent
    public static void onPlayerDamaged(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // Get the attacker
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if ("pyroclasm".equals(diskId)) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof PyroclasmDisk pyroclasm) {
                    int level = diskData.getDiskLevel(diskId);
                    float fireChance = pyroclasm.getFireChance(level);

                    // Roll for igniting attacker
                    if (RANDOM.nextFloat() < fireChance) {
                        attacker.setRemainingFireTicks(150); // 7.5 seconds

                        // Play sound
                        player.level().playSound(
                                null,
                                attacker.blockPosition(),
                                net.minecraft.sounds.SoundEvents.FIRECHARGE_USE,
                                net.minecraft.sounds.SoundSource.PLAYERS,
                                0.5f,
                                1.0f
                        );
                    }
                }
                break;
            }
        }
    }
}