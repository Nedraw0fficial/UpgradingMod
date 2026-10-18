package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.TransmitterDisk;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class TransmitterHandler {

    @SubscribeEvent
    public static void onDamageReceived(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (!"transmitter".equals(diskId)) continue;

            var disk = DiskRegistry.getDisk(diskId);
            if (!(disk instanceof TransmitterDisk transmitter)) continue;

            int level = diskData.getDiskLevel(diskId);
            float efficiency = ZSlotEffects.getEfficiencyMultiplier(player, slot);

            float incomingDamage = event.getOriginalDamage();

            //L12
            float damageReduction = transmitter.getDamageReduction(level, efficiency);
            if (damageReduction > 0) {
                float reduced = incomingDamage * damageReduction;
                event.setNewDamage(incomingDamage - reduced);
                incomingDamage = reduced;
            }

            float transmitAmount = incomingDamage * transmitter.getTransmitPercent(level, efficiency);
            double range = transmitter.getRange(level, efficiency);

            AABB searchBox = player.getBoundingBox().inflate(range);
            List<LivingEntity> nearby = player.level().getEntitiesOfClass(
                    LivingEntity.class, searchBox, entity -> {
                        if (entity == player) return false;
                        if (entity instanceof TamableAnimal tamed && tamed.isTame()) return false;
                        if (player.getTeam() != null && player.getTeam().isAlliedTo(entity.getTeam())) return false;
                        return entity.isAlive();
                    });

            if (nearby.isEmpty()) return;

            DamageSource transmitSource = player.level().damageSources().magic();
            for (LivingEntity target : nearby) {
                target.hurt(transmitSource, transmitAmount);
            }

            if (nearby.size() >= 10 && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                ModAdvancementTriggers.TRANSMIT_10(sp);
            }

            return;
        }
    }
}