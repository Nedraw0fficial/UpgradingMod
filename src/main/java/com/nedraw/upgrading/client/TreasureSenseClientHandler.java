package com.nedraw.upgrading.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.TreasureSenseDisk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class TreasureSenseClientHandler {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("treasure_sense")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof TreasureSenseDisk treasureDisk) {
                    int level = diskData.getDiskLevel(diskId);

                    if (level >= 12) {
                        renderGlowingChests(event, player);
                    }
                }

                return;
            }
        }
    }

    private static void renderGlowingChests(RenderLevelStageEvent event, Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        int range = 8;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();

        // Setup for xray rendering
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(3.0f);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        int chestsFound = 0;

        // Find nearby chests
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);

                    if (playerPos.distSqr(checkPos) > range * range) continue;

                    BlockEntity blockEntity = level.getBlockEntity(checkPos);
                    if (blockEntity instanceof ChestBlockEntity) {
                        chestsFound++;

                        poseStack.pushPose();

                        // Translate to chest position relative to camera
                        double renderX = checkPos.getX() - cameraPos.x;
                        double renderY = checkPos.getY() - cameraPos.y;
                        double renderZ = checkPos.getZ() - cameraPos.z;

                        poseStack.translate(renderX, renderY, renderZ);

                        Matrix4f matrix = poseStack.last().pose();

                        // Draw box
                        drawBox(builder, matrix, 0, 0, 0, 1, 0.875f, 1);

                        poseStack.popPose();
                    }
                }
            }
        }

        // Only draw if we found chests
        if (chestsFound > 0) {
            BufferUploader.drawWithShader(builder.buildOrThrow());
        }

        // Reset render state
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    private static void drawBox(BufferBuilder builder, Matrix4f matrix, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        // Yellow color
        float r = 1.0f, g = 1.0f, b = 0.0f, a = 1.0f;

        // Bottom face
        builder.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);

        builder.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);

        builder.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);

        builder.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);

        // Top face
        builder.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);

        builder.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);

        builder.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);

        builder.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);

        // Vertical edges
        builder.addVertex(matrix, minX, minY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, maxY, minZ).setColor(r, g, b, a);

        builder.addVertex(matrix, maxX, minY, minZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, minZ).setColor(r, g, b, a);

        builder.addVertex(matrix, maxX, minY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, maxX, maxY, maxZ).setColor(r, g, b, a);

        builder.addVertex(matrix, minX, minY, maxZ).setColor(r, g, b, a);
        builder.addVertex(matrix, minX, maxY, maxZ).setColor(r, g, b, a);
    }
}