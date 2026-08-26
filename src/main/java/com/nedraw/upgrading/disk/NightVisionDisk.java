package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NightVisionDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> BLINK_TIMERS = new HashMap<>();
    private static final Map<UUID, Boolean> IS_BLINK_ON = new HashMap<>();
    private static final int LIGHT_THRESHOLD = 7;

    public NightVisionDisk() {
        super("night_vision", "Night Vision", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {}

    @Override
    public void applyTickEffect(Player player, int level, int slot, float efficiency) {
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();
        BlockPos pos = player.blockPosition();
        int lightLevel = player.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);

        if (lightLevel >= LIGHT_THRESHOLD) {
            BLINK_TIMERS.remove(playerId);
            IS_BLINK_ON.remove(playerId);
            player.removeEffect(MobEffects.NIGHT_VISION);
            return;
        }

        if (level >= 12) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, true));

            // Efficiency scales detection radius at L12
            double detectRadius = 20.0 * efficiency;
            List<net.minecraft.world.entity.LivingEntity> invisible = player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    player.getBoundingBox().inflate(detectRadius),
                    entity -> entity.isInvisible() && entity != player
            );

            if (!invisible.isEmpty()) {
                invisible.forEach(entity -> entity.addEffect(
                        new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false)));
                if (player instanceof ServerPlayer sp) ModAdvancementTriggers.SEE_INVISIBLE(sp);
            }
            return;
        }

        // Blink mechanic: efficiency increases ON duration and decreases OFF duration
        int onDuration  = (int)(getOnDuration(level)  * efficiency);
        int offDuration = (int)(getOffDuration(level) / efficiency);

        if (!BLINK_TIMERS.containsKey(playerId)) {
            BLINK_TIMERS.put(playerId, 0);
            IS_BLINK_ON.put(playerId, true);
        }

        int timer = BLINK_TIMERS.get(playerId);
        boolean isBlinkOn = IS_BLINK_ON.get(playerId);

        if (isBlinkOn) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 40, 0, false, false, true));
            if (timer >= onDuration) {
                IS_BLINK_ON.put(playerId, false);
                BLINK_TIMERS.put(playerId, 0);
                player.removeEffect(MobEffects.NIGHT_VISION);
            } else {
                BLINK_TIMERS.put(playerId, timer + 1);
            }
        } else {
            player.removeEffect(MobEffects.NIGHT_VISION);
            if (timer >= offDuration) {
                IS_BLINK_ON.put(playerId, true);
                BLINK_TIMERS.put(playerId, 0);
            } else {
                BLINK_TIMERS.put(playerId, timer + 1);
            }
        }
    }

    @Override
    public void removeEffect(Player player) {
        UUID playerId = player.getUUID();
        BLINK_TIMERS.remove(playerId);
        IS_BLINK_ON.remove(playerId);
        player.removeEffect(MobEffects.NIGHT_VISION);
    }

    private int getOnDuration(int level) {
        return switch (level) {
            case 1 -> 40;  case 2 -> 60;  case 3 -> 80;
            case 4 -> 100; case 5 -> 120; case 6 -> 140;
            case 7 -> 160; case 8 -> 180; case 9 -> 200;
            case 10 -> 220; case 11 -> 240;
            default -> 40;
        };
    }

    private int getOffDuration(int level) {
        return switch (level) {
            case 1 -> 160; case 2 -> 140; case 3 -> 120;
            case 4 -> 100; case 5 -> 80;  case 6 -> 60;
            case 7 -> 40;  case 8 -> 20;  case 9 -> 10;
            case 10 -> 6;  case 11 -> 4;
            default -> 160;
        };
    }
}
