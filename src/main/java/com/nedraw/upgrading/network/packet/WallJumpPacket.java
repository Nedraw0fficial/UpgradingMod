package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.MountainGoatHandler;
import com.nedraw.upgrading.UpgradingMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WallJumpPacket(int wallDirection) implements CustomPacketPayload {

    public static final Type<WallJumpPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "wall_jump"));

    public static final StreamCodec<ByteBuf, WallJumpPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            WallJumpPacket::wallDirection,
            WallJumpPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WallJumpPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound() && context.player() instanceof ServerPlayer player) {

                // Verify player is actually clinging
                if (!MountainGoatHandler.CLING_START.containsKey(player.getUUID())) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("✗ Not clinging!")
                                    .withStyle(style -> style.withColor(0xFF5555)),
                            true
                    );
                    return;
                }

                Direction wall = Direction.from3DDataValue(packet.wallDirection());
                MountainGoatHandler.performWallJump(player, wall);
            }
        });
    }
}