package com.nedraw.upgrading.network;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.network.packet.SyncDiskDataPacket;
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
    }
}