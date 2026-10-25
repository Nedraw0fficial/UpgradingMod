package com.nedraw.upgrading.particle;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.fx.DiskFxType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, UpgradingMod.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NECROMISIS =
            PARTICLE_TYPES.register("necromisis", () -> new SimpleParticleType(false));

    public static final Map<DiskFxType, DeferredHolder<ParticleType<?>, SimpleParticleType>> DISK_PARTICLES =
            new EnumMap<>(DiskFxType.class);

    static {
        for (DiskFxType type : DiskFxType.values()) {
            DISK_PARTICLES.put(type, PARTICLE_TYPES.register("disk_" + type.getId(),
                    () -> new SimpleParticleType(false)));
        }
    }
}