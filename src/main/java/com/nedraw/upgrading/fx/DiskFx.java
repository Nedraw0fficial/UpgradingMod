package com.nedraw.upgrading.fx;

import com.nedraw.upgrading.particle.ModParticles;
import com.nedraw.upgrading.sound.ModSounds;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiskFx {

    private static final Map<UUID, Map<DiskFxType, Long>> LAST_PLAYED = new HashMap<>();

    public static void play(Player player, DiskFxType type) {
        play(player, type, type.getParticleCount());
    }

    public static void play(Player player, DiskFxType type, int particleCount) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!(serverPlayer.level() instanceof ServerLevel level)) return;

        long now = level.getGameTime();
        Map<DiskFxType, Long> last = LAST_PLAYED.computeIfAbsent(
                serverPlayer.getUUID(), k -> new EnumMap<>(DiskFxType.class));
        Long previous = last.get(type);
        if (previous != null && now - previous < type.getCooldownTicks()) return;
        last.put(type, now);

        emit(level, serverPlayer, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                type, particleCount);
    }

    public static void playAt(ServerLevel level, double x, double y, double z, DiskFxType type) {
        emit(level, null, x, y, z, type, type.getParticleCount());
    }

    private static void emit(ServerLevel level, ServerPlayer target, double x, double feetY, double z,
                             DiskFxType type, int particleCount) {
        float pitch = 0.9f + level.random.nextFloat() * 0.2f;
        double soundY = feetY + 0.1;
        boolean selfOnly = target != null && type.getAudience() == DiskFxType.Audience.SELF;

        SoundEvent sound = ModSounds.DISK_SOUNDS.get(type).get();

        if (selfOnly) {
            target.connection.send(new ClientboundSoundPacket(
                    ModSounds.DISK_SOUNDS.get(type), SoundSource.PLAYERS,
                    x, soundY, z, type.getVolume(), pitch, level.random.nextLong()));
        } else {
            level.playSound(null, x, soundY, z, sound, SoundSource.PLAYERS, type.getVolume(), pitch);
        }

        if (particleCount <= 0) return;

        SimpleParticleType particle = ModParticles.DISK_PARTICLES.get(type).get();
        int count = Math.max(1, particleCount + level.random.nextInt(3) - 1);
        double particleY = feetY + type.getHeight() - 0.1 + level.random.nextDouble() * 0.2;
        double spread = 0.35 + level.random.nextDouble() * 0.1;
        double ySpread = type.getHeight() < 0.5 ? 0.05 : spread;

        if (selfOnly) {
            level.sendParticles(target, particle, false,
                    x, particleY, z, count, spread, ySpread, spread, 0.0);
        } else {
            level.sendParticles(particle, x, particleY, z, count, spread, ySpread, spread, 0.0);
        }
    }
}