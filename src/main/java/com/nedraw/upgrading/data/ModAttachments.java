package com.nedraw.upgrading.data;

import com.nedraw.upgrading.UpgradingMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, UpgradingMod.MODID);

    public static final Supplier<AttachmentType<PlayerDiskData>> PLAYER_DISK_DATA =
            ATTACHMENT_TYPES.register("player_disk_data", () ->
                    AttachmentType.serializable(PlayerDiskData::new).build()
            );
}