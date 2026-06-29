package com.nedraw.upgrading.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.EffectCure;

import java.util.Set;

public class NecromisisEffect extends MobEffect {

    public NecromisisEffect() {
        // HARMFUL category, soulless dark color (0x1A1A1A)
        super(MobEffectCategory.HARMFUL, 0x1A1A1A);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Necromisis doesn't tick - purely a marker effect
        // Its effects are handled via events in NecroArcherHandler
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }

    // MILK DOES NOT CURE THIS EFFECT
    // Override from IMobEffectExtension - leave the set empty = nothing cures it
    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        // Intentionally empty - no cures, not even milk
    }
}