package com.nedraw.upgrading.network;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.network.packet.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                SyncDiskDataPacket.TYPE,
                SyncDiskDataPacket.STREAM_CODEC,
                SyncDiskDataPacket::handle
        );

        registrar.playToServer(
                UpgradeDiskPacket.TYPE,
                UpgradeDiskPacket.STREAM_CODEC,
                UpgradeDiskPacket::handle
        );

        registrar.playToServer(
                EquipDiskPacket.TYPE,
                EquipDiskPacket.STREAM_CODEC,
                EquipDiskPacket::handle
        );

        registrar.playToServer(
                ClaimEncryptedFloppyPacket.TYPE,
                ClaimEncryptedFloppyPacket.STREAM_CODEC,
                ClaimEncryptedFloppyPacket::handle
        );

        registrar.playToServer(
                WallJumpPacket.TYPE,
                WallJumpPacket.STREAM_CODEC,
                WallJumpPacket::handle
        );

        registrar.playToServer(
                ActivateMythicPacket.TYPE,
                ActivateMythicPacket.STREAM_CODEC,
                ActivateMythicPacket::handle
        );

        registrar.playToServer(
                EquipZSlotPacket.TYPE,
                EquipZSlotPacket.STREAM_CODEC,
                EquipZSlotPacket::handle
        );
    }

}