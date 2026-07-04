package com.nedraw.upgrading.particle;

import com.nedraw.upgrading.UpgradingMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, UpgradingMod.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NECROMISIS =
            PARTICLE_TYPES.register("necromisis", () -> new SimpleParticleType(false));
}