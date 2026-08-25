package com.nedraw.upgrading.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBinds {
    public static final String KEY_CATEGORY = "key.categories.upgrading";

    public static final KeyMapping OPEN_DISK_MENU = new KeyMapping(
            "key.upgrading.open_disk_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            KEY_CATEGORY
    );

    public static final KeyMapping ACTIVATE_SLOT_1 = new KeyMapping(
            "key.upgrading.activate_slot_1",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KEY_CATEGORY
    );

    public static final KeyMapping ACTIVATE_SLOT_2 = new KeyMapping(
            "key.upgrading.activate_slot_2",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KEY_CATEGORY
    );

    public static final KeyMapping ACTIVATE_SLOT_3 = new KeyMapping(
            "key.upgrading.activate_slot_3",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            KEY_CATEGORY
    );
}