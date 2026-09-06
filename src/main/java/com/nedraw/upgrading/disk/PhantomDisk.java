package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhantomDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> SNEAK_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> INVISIBLE_TICKS = new HashMap<>();

    public PhantomDisk() {
        super("phantom", "Phantom", DiskRarity.EPIC);
    }

    @Override
    public void applyTickEffect(Player player, int level, int slot, float efficiency) {
        if (player.level().isClientSide) return;
        UUID id = player.getUUID();
        boolean isNight = !player.level().isDay();
        boolean isSneaking = player.isCrouching();

        if (!isNight || !isSneaking) {
            SNEAK_TICKS.put(id, 0);
            player.removeEffect(MobEffects.INVISIBILITY);
            INVISIBLE_TICKS.put(id, 0);
            return;
        }

        if (level >= 12) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false, true));
            trackInvisibleTicks(player, id);
            return;
        }

        int threshold = (int)(getThresholdTicks(level) / efficiency);
        int ticks = SNEAK_TICKS.getOrDefault(id, 0) + 1;
        SNEAK_TICKS.put(id, ticks);

        if (ticks >= threshold) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false, true));
            trackInvisibleTicks(player, id);
        }
    }

    private void trackInvisibleTicks(Player player, UUID id) {
        if (player.hasEffect(MobEffects.INVISIBILITY)) {
            int invisTicks = INVISIBLE_TICKS.getOrDefault(id, 0) + 1;
            INVISIBLE_TICKS.put(id, invisTicks);
            if (invisTicks >= 1200 && player instanceof ServerPlayer sp) {
                ModAdvancementTriggers.GHOST(sp);
                INVISIBLE_TICKS.put(id, 0);
            }
        }
    }

    @Override
    public void removeEffect(Player player) {
        UUID id = player.getUUID();
        SNEAK_TICKS.remove(id);
        INVISIBLE_TICKS.remove(id);
        player.removeEffect(MobEffects.INVISIBILITY);
    }

    private int getThresholdTicks(int level) {
        return switch (level) {
            case 7  -> 60;
            case 8  -> 50;
            case 9  -> 40;
            case 10 -> 30;
            case 11 -> 20;
            default -> 60;
        };
    }
}