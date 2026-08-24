package com.nedraw.upgrading.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.network.packet.ClaimEncryptedFloppyPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class EncryptedFloppyScreen extends Screen {

    private static final int DISK_SIZE = 128;
    private static final int ORB_SIZE = 48;
    private static final int ORB_SPACING = 15;

    private DiskRarity currentRarity;
    private final int totalChances;
    private int clicksRemaining;
    private boolean readyToOpen;
    private float diskBobOffset;
    private float diskScale;
    private float diskRotation;
    private float diskIdleRotation;
    private float disk3DFlip;
    private int tickCount;
    private final Random random;

    // Dynamic orb hover arrays based on totalChances
    private final float[] orbHoverOffsets;
    private final float[] orbHoverSpeeds;

    private float bgScrollOffset;

    private static final float BASIC_TO_RARE = 0.1396f;
    private static final float RARE_TO_EPIC = 0.3042f;
    private static final float EPIC_TO_LEGENDARY = 0.4118f;
    private static final float LEGENDARY_TO_MYTHIC = 0.1145f;

    private boolean isFlashing;
    private int flashTicks;
    private static final int FLASH_DURATION = 15;

    public EncryptedFloppyScreen(DiskRarity startingRarity, int amountOfChances) {
        super(Component.translatable("gui.upgrading.encrypted_floppy.title"));
        this.currentRarity = startingRarity;
        this.totalChances = amountOfChances;
        this.clicksRemaining = amountOfChances;
        this.readyToOpen = amountOfChances == 0; // 0 chances = instant open
        this.diskBobOffset = 0;
        this.diskScale = 1.0f;
        this.diskRotation = 0;
        this.diskIdleRotation = 0;
        this.disk3DFlip = 0;
        this.tickCount = 0;
        this.random = new Random();
        this.isFlashing = false;
        this.flashTicks = 0;
        this.bgScrollOffset = 0;

        // Dynamic size based on amountOfChances
        int orbCount = Math.max(1, amountOfChances);
        this.orbHoverOffsets = new float[orbCount];
        this.orbHoverSpeeds = new float[orbCount];

        for (int i = 0; i < orbCount; i++) {
            orbHoverSpeeds[i] = 0.08f + (random.nextFloat() * 0.04f);
            orbHoverOffsets[i] = random.nextFloat() * 6.28f;
        }
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;

        diskBobOffset = (float) Math.sin(tickCount * 0.1) * 8.0f;
        diskIdleRotation = (float) Math.sin(tickCount * 0.05) * 5.0f;

        if (diskScale > 1.0f) {
            diskScale -= 0.08f;
            if (diskScale < 1.0f) diskScale = 1.0f;
        }

        if (diskRotation != 0) {
            diskRotation *= 0.85f;
            if (Math.abs(diskRotation) < 0.5f) diskRotation = 0;
        }

        if (disk3DFlip > 0) {
            disk3DFlip -= 40.0f;
            if (disk3DFlip < 0) disk3DFlip = 0;
        }

        for (int i = 0; i < orbHoverOffsets.length; i++) {
            orbHoverOffsets[i] += orbHoverSpeeds[i];
        }

        bgScrollOffset += 0.5f;

        if (isFlashing) {
            flashTicks++;
            if (flashTicks >= FLASH_DURATION) {
                claimDisk();
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackgroundWithPattern(graphics);
        renderGlow(graphics);

        String rarityKey = "rarity.upgrading." + currentRarity.name().toLowerCase();
        Component rarityText = Component.translatable(rarityKey).withStyle(style -> style.withBold(true));

        graphics.pose().pushPose();
        graphics.pose().scale(2.0f, 2.0f, 1.0f);

        int textWidth = this.font.width(rarityText);
        int scaledX = (this.width / 2 - textWidth) / 2;
        graphics.drawString(this.font, rarityText, scaledX, 20, 0xFFFFFF, true);

        graphics.pose().popPose();

        if (!readyToOpen) {
            renderUpgradeOrbs(graphics);
        } else {
            Component openText = Component.translatable("gui.upgrading.encrypted_floppy.click_to_open");
            int openTextWidth = this.font.width(openText);
            graphics.drawString(this.font, openText,
                    (this.width - openTextWidth) / 2, this.height - 80,
                    0xFFFFFF, true);
        }

        renderDisk(graphics);

        if (isFlashing) {
            float alpha = 1.0f - ((float) flashTicks / FLASH_DURATION);
            int whiteAlpha = (int) (alpha * 255);
            graphics.fill(0, 0, this.width, this.height,
                    (whiteAlpha << 24) | 0xFFFFFF);
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void renderBackgroundWithPattern(GuiGraphics graphics) {
        int baseColor = currentRarity.getColor();
        int bgColor = 0xFF000000 | baseColor;
        graphics.fill(0, 0, this.width, this.height, bgColor);

        int r = ((baseColor >> 16) & 0xFF) * 2 / 3;
        int g = ((baseColor >> 8) & 0xFF) * 2 / 3;
        int b = (baseColor & 0xFF) * 2 / 3;
        int patternColor = (0x66 << 24) | (r << 16) | (g << 8) | b;

        int patternSize = 24;
        int spacing = 40;
        float scrollX = bgScrollOffset * 0.3f;
        float scrollY = bgScrollOffset * 0.3f;

        int startCol = (int) Math.floor(-scrollX / spacing) - 2;
        int startRow = (int) Math.floor(-scrollY / spacing) - 2;
        int endCol = (int) Math.ceil((this.width - scrollX) / spacing) + 2;
        int endRow = (int) Math.ceil((this.height - scrollY) / spacing) + 2;

        int mouseX = this.width / 2;
        int mouseY = this.height / 2;
        try {
            if (minecraft != null && minecraft.mouseHandler != null && minecraft.getWindow() != null) {
                int screenWidth = minecraft.getWindow().getScreenWidth();
                int screenHeight = minecraft.getWindow().getScreenHeight();
                if (screenWidth > 0 && screenHeight > 0) {
                    mouseX = (int) minecraft.mouseHandler.xpos() * this.width / screenWidth;
                    mouseY = (int) minecraft.mouseHandler.ypos() * this.height / screenHeight;
                }
            }
        } catch (Exception e) {}

        for (int col = startCol; col <= endCol; col++) {
            for (int row = startRow; row <= endRow; row++) {
                float x = (col * spacing) + scrollX;
                float y = (row * spacing) + scrollY;
                float centerX = x + patternSize / 2f;
                float centerY = y + patternSize / 2f;
                float distToMouse = (float) Math.sqrt(
                        Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2));

                float maxDistance = 200f;
                float scale = 1.0f;
                if (distToMouse < maxDistance) {
                    float influence = 1.0f - (distToMouse / maxDistance);
                    scale = 1.0f - (influence * 0.5f);
                }

                float rotation = tickCount * 0.5f;

                graphics.pose().pushPose();
                graphics.pose().translate(centerX, centerY, 0);
                graphics.pose().scale(scale, scale, 1.0f);
                graphics.pose().rotateAround(
                        new org.joml.Quaternionf().rotationZ((float) Math.toRadians(rotation)),
                        0, 0, 0);

                int halfSize = patternSize / 2;
                graphics.fill(-halfSize, -halfSize, halfSize, halfSize, patternColor);
                graphics.pose().popPose();
            }
        }
    }

    private void renderGlow(GuiGraphics graphics) {
        int centerX = this.width / 2;
        int centerY = (this.height / 2) + (int) diskBobOffset;
        int glowSize = 220;
        int glowColor = currentRarity.getColor();

        for (int i = 0; i < 6; i++) {
            int size = glowSize - (i * 25);
            int alpha = 60 - (i * 8);
            int color = (alpha << 24) | glowColor;
            graphics.fill(centerX - size/2, centerY - size/2,
                    centerX + size/2, centerY + size/2, color);
        }
    }

    private void renderDisk(GuiGraphics graphics) {
        int centerX = this.width / 2;
        int centerY = (this.height / 2) + (int) diskBobOffset;

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                UpgradingMod.MODID, "textures/gui/encrypted_floppy.png");

        float normalizedFlip = disk3DFlip % 360f;
        boolean showFront = (normalizedFlip >= 0 && normalizedFlip < 90) || (normalizedFlip >= 270 && normalizedFlip < 360);

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 100);
        graphics.pose().scale(diskScale, diskScale, 1.0f);

        if (disk3DFlip > 0) {
            graphics.pose().rotateAround(
                    new org.joml.Quaternionf().rotationY((float) Math.toRadians(disk3DFlip)),
                    0, 0, 0);
        }

        float totalRotation = diskRotation + diskIdleRotation;
        if (totalRotation != 0) {
            graphics.pose().rotateAround(
                    new org.joml.Quaternionf().rotationZ((float) Math.toRadians(totalRotation)),
                    0, 0, 0);
        }

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();

        if (showFront) {
            graphics.blit(texture, -DISK_SIZE/2, -DISK_SIZE/2, 0, 0, DISK_SIZE, DISK_SIZE, DISK_SIZE, DISK_SIZE);
        } else {
            graphics.pose().pushPose();
            graphics.pose().scale(-1.0f, 1.0f, 1.0f);
            graphics.blit(texture, -DISK_SIZE/2, -DISK_SIZE/2, 0, 0, DISK_SIZE, DISK_SIZE, DISK_SIZE, DISK_SIZE);
            graphics.pose().popPose();
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.pose().popPose();
    }

    private void renderUpgradeOrbs(GuiGraphics graphics) {
        if (totalChances == 0) return;

        Component upgradeText = Component.translatable("gui.upgrading.encrypted_floppy.upgrade_chances");
        int textWidth = this.font.width(upgradeText);
        graphics.drawString(this.font, upgradeText,
                (this.width - textWidth) / 2, this.height - 80, 0xFFFFFF, true);

        // Dynamic orb spreading based on totalChances
        int totalWidth = (ORB_SIZE * totalChances) + (ORB_SPACING * (totalChances - 1));
        int startX = (this.width - totalWidth) / 2;
        int orbY = this.height - 70;

        for (int i = 0; i < totalChances; i++) {
            int orbX = startX + (i * (ORB_SIZE + ORB_SPACING));

            // Clicks disappear from right to left
            int orbIndex = (totalChances - 1) - i;
            boolean isUsed = orbIndex >= clicksRemaining;
            boolean isCurrent = orbIndex == (clicksRemaining - 1);

            float hoverY = (float) Math.sin(orbHoverOffsets[i]) * 5.0f;
            renderOrb(graphics, orbX, (int)(orbY + hoverY), isUsed, isCurrent);
        }
    }

    private void renderOrb(GuiGraphics graphics, int x, int y, boolean isUsed, boolean isCurrent) {
        String textureName = isUsed ? "orb_used" : (isCurrent ? "orb_highlight" : "orb_default");
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                UpgradingMod.MODID, "textures/gui/" + textureName + ".png");

        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, 0, 0, ORB_SIZE, ORB_SIZE, ORB_SIZE, ORB_SIZE);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        if (isFlashing) return true;

        if (readyToOpen) {
            startFlash();
            return true;
        }

        if (clicksRemaining > 0) {
            boolean upgraded = attemptUpgrade();
            clicksRemaining--;

            if (upgraded) {
                diskScale = 1.65f;
            } else {
                diskScale = 1.1f;
            }
            diskRotation = (random.nextFloat() - 0.5f) * 30.0f;

            if (clicksRemaining == 0) {
                readyToOpen = true;
            }

            minecraft.getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.5f));

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean attemptUpgrade() {
        float roll = random.nextFloat();
        boolean upgraded = false;

        float pitty = net.minecraft.client.Minecraft.getInstance().player != null
                ? com.nedraw.upgrading.data.PlayerDiskData.get(
                net.minecraft.client.Minecraft.getInstance().player).getPittyMeter()
                : 0.0f;
        float multiplier = 1.0f + pitty;

        switch (currentRarity) {
            case BASIC:
                if (roll < BASIC_TO_RARE * multiplier) { currentRarity = DiskRarity.RARE; upgraded = true; }
                break;
            case RARE:
                if (roll < RARE_TO_EPIC * multiplier) { currentRarity = DiskRarity.EPIC; upgraded = true; }
                break;
            case EPIC:
                if (roll < EPIC_TO_LEGENDARY * multiplier) { currentRarity = DiskRarity.LEGENDARY; upgraded = true; }
                break;
            case LEGENDARY:
                if (roll < LEGENDARY_TO_MYTHIC * multiplier) { currentRarity = DiskRarity.MYTHIC; upgraded = true; }
                break;
            case MYTHIC:
                break;
        }

        if (upgraded) {
            minecraft.getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.2f));
            disk3DFlip = 360f;
        }

        return upgraded;
    }

    private void startFlash() {
        isFlashing = true;
        flashTicks = 0;
    }

    private void claimDisk() {
        if (minecraft.player != null) {
            com.nedraw.upgrading.data.PlayerDiskData.get(minecraft.player)
                    .updatePittyMeter(currentRarity);
        }
        PacketDistributor.sendToServer(new ClaimEncryptedFloppyPacket(currentRarity.name()));
        this.onClose();
    }

    @Override
    public void onClose() {
        if (!isFlashing) {
            PacketDistributor.sendToServer(new ClaimEncryptedFloppyPacket(currentRarity.name()));
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}