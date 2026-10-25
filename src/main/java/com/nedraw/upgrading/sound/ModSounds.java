package com.nedraw.upgrading.sound;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.fx.DiskFxType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, UpgradingMod.MODID);

    public static final Map<DiskFxType, DeferredHolder<SoundEvent, SoundEvent>> DISK_SOUNDS =
            new EnumMap<>(DiskFxType.class);

    static {
        for (DiskFxType type : DiskFxType.values()) {
            String name = "disk." + type.getId();
            DISK_SOUNDS.put(type, SOUNDS.register(name, () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, name))));
        }
    }
}