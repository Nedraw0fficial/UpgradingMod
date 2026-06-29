package com.nedraw.upgrading.effect;

import com.nedraw.upgrading.UpgradingMod;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, UpgradingMod.MODID);

    public static final DeferredHolder<MobEffect, NecromisisEffect> NECROMISIS =
            MOB_EFFECTS.register("necromisis", NecromisisEffect::new);
}