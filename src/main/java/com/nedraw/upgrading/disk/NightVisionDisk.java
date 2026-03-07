package com.nedraw.upgrading.disk;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NightVisionDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> BLINK_TIMERS = new HashMap<>();
    private static final Map<UUID, Boolean> IS_BLINK_ON = new HashMap<>();

    private static final int LIGHT_THRESHOLD = 7;

    public NightVisionDisk() {
        super("night_vision", "Night Vision", DiskRarity.BASIC);

        this.withDescription(1, "Night vision blinks in darkness: 2s on, 8s off")
                .withDescription(2, "Night vision blinks in darkness: 3s on, 7s off")
                .withDescription(3, "Night vision blinks in darkness: 4s on, 6s off")
                .withDescription(4, "Night vision blinks in darkness: 5s on, 5s off")
                .withDescription(5, "Night vision blinks in darkness: 6s on, 4s off")
                .withDescription(6, "Night vision blinks in darkness: 7s on, 3s off")
                .withDescription(7, "Night vision blinks in darkness: 8s on, 2s off")
                .withDescription(8, "Night vision blinks in darkness: 9s on, 1s off")
                .withDescription(9, "Night vision blinks in darkness: 10s on, 0.5s off")
                .withDescription(10, "Night vision blinks in darkness: 11s on, 0.3s off")
                .withDescription(11, "Night vision blinks in darkness: 12s on, 0.2s off")
                .withDescription(12, "Perfect constant night vision and\nsee invisible nearby entities");
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Just track - actual logic in applyTickEffect
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // Server-side only
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();

        // Check if it's dark
        BlockPos pos = player.blockPosition();
        int lightLevel = player.level().getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos);

        if (lightLevel >= LIGHT_THRESHOLD) {
            // Not dark - clear everything
            BLINK_TIMERS.remove(playerId);
            IS_BLINK_ON.remove(playerId);
            player.removeEffect(MobEffects.NIGHT_VISION);
            return;
        }

        // Level 12: Always on
        if (level >= 12) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    220,
                    0,
                    false,
                    false,
                    true
            ));

            // Glow invisible entities
            player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    player.getBoundingBox().inflate(20),
                    entity -> entity.isInvisible() && entity != player
            ).forEach(entity -> {
                entity.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING,
                        40,
                        0,
                        false,
                        false,
                        false
                ));
            });
            return;
        }

        // Levels 1-11: BLINK MECHANIC

        int onDuration = getOnDuration(level);
        int offDuration = getOffDuration(level);

        // Initialize if first time
        if (!BLINK_TIMERS.containsKey(playerId)) {
            BLINK_TIMERS.put(playerId, 0);
            IS_BLINK_ON.put(playerId, true);
        }

        int timer = BLINK_TIMERS.get(playerId);
        boolean isBlinkOn = IS_BLINK_ON.get(playerId);

        if (isBlinkOn) {
            // ON PHASE - Apply night vision
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    40,
                    0,
                    false,
                    false,
                    true
            ));

            // Check if ON phase is over
            if (timer >= onDuration) {
                // Switch to OFF
                IS_BLINK_ON.put(playerId, false);
                BLINK_TIMERS.put(playerId, 0);
                player.removeEffect(MobEffects.NIGHT_VISION);
            } else {
                // Continue ON phase
                BLINK_TIMERS.put(playerId, timer + 1);
            }
        } else {
            // OFF PHASE - No night vision
            player.removeEffect(MobEffects.NIGHT_VISION);

            // Check if OFF phase is over
            if (timer >= offDuration) {
                // Switch to ON
                IS_BLINK_ON.put(playerId, true);
                BLINK_TIMERS.put(playerId, 0);
            } else {
                // Continue OFF phase
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
            case 1 -> 40;
            case 2 -> 60;
            case 3 -> 80;
            case 4 -> 100;
            case 5 -> 120;
            case 6 -> 140;
            case 7 -> 160;
            case 8 -> 180;
            case 9 -> 200;
            case 10 -> 220;
            case 11 -> 240;
            default -> 40;
        };
    }

    private int getOffDuration(int level) {
        return switch (level) {
            case 1 -> 160;
            case 2 -> 140;
            case 3 -> 120;
            case 4 -> 100;
            case 5 -> 80;
            case 6 -> 60;
            case 7 -> 40;
            case 8 -> 20;
            case 9 -> 10;
            case 10 -> 6;
            case 11 -> 4;
            default -> 160;
        };
    }
}