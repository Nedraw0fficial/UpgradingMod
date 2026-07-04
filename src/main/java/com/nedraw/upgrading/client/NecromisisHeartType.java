package com.nedraw.upgrading.client;

import net.minecraft.resources.ResourceLocation;
import com.nedraw.upgrading.UpgradingMod;

/**
 * Provides all 8 ResourceLocation constructor parameters for the Necromisis
 * HeartType enum extension. The "name" field in the JSON entry is namespaced
 * separately (per @NamedEnum rules) and is NOT part of the visible constructor -
 * Java's enum machinery auto-injects it as a hidden parameter.
 */
public class NecromisisHeartType {

    private static final String[] PATHS = {
            "hud/heart/necromisis_full",
            "hud/heart/necromisis_full_blinking",
            "hud/heart/necromisis_half",
            "hud/heart/necromisis_half_blinking",
            "hud/heart/necromisis_hardcore_full",
            "hud/heart/necromisis_hardcore_full_blinking",
            "hud/heart/necromisis_hardcore_half",
            "hud/heart/necromisis_hardcore_half_blinking"
    };

    public static Object getParameter(int index, Class<?> expectedType) {
        return expectedType.cast(
                ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, PATHS[index])
        );
    }

    public static net.minecraft.client.gui.Gui.HeartType get() {
        return net.minecraft.client.gui.Gui.HeartType.valueOf("UPGRADING_NECROMISIS");
    }
}