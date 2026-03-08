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
    private int clicksRemaining;
    private boolean readyToOpen;
    private float diskBobOffset;
    private float diskScale;
    private float diskRotation;
    private float diskIdleRotation; // Casual rotation
    private float disk3DFlip; // NEW: 3D flip animation on upgrade!
    private int tickCount;
    private final Random random;

    // Orb hover offsets
    private final float[] orbHoverOffsets = new float[4];
    private final float[] orbHoverSpeeds = new float[4];

    // Background scroll
    private float bgScrollOffset;

    // Upgrade chances - PROPERLY BALANCED for final distribution
    private static final float BASIC_TO_RARE = 0.1396f;
    private static final float RARE_TO_EPIC = 0.3042f;
    private static final float EPIC_TO_LEGENDARY = 0.4118f;
    private static final float LEGENDARY_TO_MYTHIC = 0.1145f;

    // White flash animation
    private boolean isFlashing;
    private int flashTicks;
    private static final int FLASH_DURATION = 15; // Shorter flash

    public EncryptedFloppyScreen() {
        super(Component.translatable("gui.upgrading.encrypted_floppy.title"));
        this.currentRarity = DiskRarity.BASIC;
        this.clicksRemaining = 4;
        this.readyToOpen = false;
        this.diskBobOffset = 0;
        this.diskScale = 1.0f;
        this.diskRotation = 0;
        this.diskIdleRotation = 0;
        this.disk3DFlip = 0; // Start with no flip
        this.tickCount = 0;
        this.random = new Random();
        this.isFlashing = false;
        this.flashTicks = 0;
        this.bgScrollOffset = 0;

        // Initialize random hover speeds for each orb
        for (int i = 0; i < 4; i++) {
            orbHoverSpeeds[i] = 0.08f + (random.nextFloat() * 0.04f);
            orbHoverOffsets[i] = random.nextFloat() * 6.28f;
        }
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;

        // Smooth bobbing animation for disk
        diskBobOffset = (float) Math.sin(tickCount * 0.1) * 8.0f;

        // Casual idle rotation (swaying back and forth)
        diskIdleRotation = (float) Math.sin(tickCount * 0.05) * 5.0f; // ±5 degrees sway

        // Animate disk scale back to 1.0
        if (diskScale > 1.0f) {
            diskScale -= 0.08f;
            if (diskScale < 1.0f) diskScale = 1.0f;
        }

        // Animate disk rotation back to 0 (click rotation)
        if (diskRotation != 0) {
            diskRotation *= 0.85f;
            if (Math.abs(diskRotation) < 0.5f) diskRotation = 0;
        }

        // Animate 3D flip (SUPER FAST! POW!)
        if (disk3DFlip > 0) {
            disk3DFlip -= 40.0f; // EVEN FASTER! Completes in 9 ticks!
            if (disk3DFlip < 0) disk3DFlip = 0;
        }

        // Animate orb hover
        for (int i = 0; i < 4; i++) {
            orbHoverOffsets[i] += orbHoverSpeeds[i];
        }

        // Scroll background pattern (INFINITE - NO RESET!)
        bgScrollOffset += 0.5f;
        // DON'T RESET! Let it scroll forever!

        // Handle flash animation
        if (isFlashing) {
            flashTicks++;
            if (flashTicks >= FLASH_DURATION) {
                // Flash done - immediately close and claim
                claimDisk();
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Background with pattern
        renderBackgroundWithPattern(graphics);

        // Render glow effect behind disk
        renderGlow(graphics);

        // Render rarity text at top (BIGGER AND BOLD!)
        String rarityKey = "rarity.upgrading." + currentRarity.name().toLowerCase();
        Component rarityText = Component.translatable(rarityKey).withStyle(style -> style.withBold(true));

        // Scale up the text
        graphics.pose().pushPose();
        graphics.pose().scale(2.0f, 2.0f, 1.0f); // 2x bigger!

        int textWidth = this.font.width(rarityText);
        int scaledX = (this.width / 2 - textWidth) / 2; // Adjust for scale
        int scaledY = 20; // Higher position

        // Draw with shadow
        graphics.drawString(this.font, rarityText, scaledX, scaledY, 0xFFFFFF, true);

        graphics.pose().popPose();

        // Render upgrade chances orbs at bottom
        if (!readyToOpen) {
            renderUpgradeOrbs(graphics);
        } else {
            // Show "CLICK TO OPEN!" text (LOWER position)
            Component openText = Component.translatable("gui.upgrading.encrypted_floppy.click_to_open");
            int openTextWidth = this.font.width(openText);
            graphics.drawString(this.font, openText,
                    (this.width - openTextWidth) / 2, this.height - 80, // Moved down
                    0xFFFFFF, true);
        }

        // RENDER DISK LAST SO IT'S ALWAYS ON TOP!
        renderDisk(graphics);

        // White flash overlay (LAST!)
        if (isFlashing) {
            float alpha = 1.0f - ((float) flashTicks / FLASH_DURATION);
            int whiteAlpha = (int) (alpha * 255);
            graphics.fill(0, 0, this.width, this.height,
                    (whiteAlpha << 24) | 0xFFFFFF);
        }

        // FINAL SAFETY: Reset ALL RenderSystem state!
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void renderBackgroundWithPattern(GuiGraphics graphics) {
        // STEP 1: FULLY OPAQUE SOLID BACKGROUND (rarity color)
        int baseColor = currentRarity.getColor();
        int bgColor = 0xFF000000 | baseColor;
        graphics.fill(0, 0, this.width, this.height, bgColor);

        // STEP 2: Calculate darker color for pattern
        int r = ((baseColor >> 16) & 0xFF) * 2 / 3;
        int g = ((baseColor >> 8) & 0xFF) * 2 / 3;
        int b = (baseColor & 0xFF) * 2 / 3;

        int patternColor = (0x66 << 24) | (r << 16) | (g << 8) | b;

        // STEP 3: Draw rotating squares with mouse interaction!
        int patternSize = 24;
        int spacing = 40;

        // TRUE INFINITE SCROLLING - NO MODULO, NO SNAP!
        float scrollX = bgScrollOffset * 0.3f;
        float scrollY = bgScrollOffset * 0.3f;

        // Calculate where to START drawing based on scroll
        // This creates seamless infinite scrolling
        int startCol = (int) Math.floor(-scrollX / spacing) - 2;
        int startRow = (int) Math.floor(-scrollY / spacing) - 2;
        int endCol = (int) Math.ceil((this.width - scrollX) / spacing) + 2;
        int endRow = (int) Math.ceil((this.height - scrollY) / spacing) + 2;

        // Get mouse position safely
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
        } catch (Exception e) {
            // Use center as fallback
        }

        // Draw grid using GRID COORDINATES (no modulo!)
        for (int col = startCol; col <= endCol; col++) {
            for (int row = startRow; row <= endRow; row++) {

                // Calculate actual position with scroll offset
                float x = (col * spacing) + scrollX;
                float y = (row * spacing) + scrollY;

                // Calculate distance from mouse
                float centerX = x + patternSize / 2f;
                float centerY = y + patternSize / 2f;
                float distToMouse = (float) Math.sqrt(
                        Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2)
                );

                // Scale based on distance
                float maxDistance = 200f;
                float scale = 1.0f;
                if (distToMouse < maxDistance) {
                    float influence = 1.0f - (distToMouse / maxDistance);
                    scale = 1.0f - (influence * 0.5f);
                }

                // Rotation (slow clockwise)
                float rotation = tickCount * 0.5f;

                // Draw rotated and scaled square
                graphics.pose().pushPose();
                graphics.pose().translate(centerX, centerY, 0);
                graphics.pose().scale(scale, scale, 1.0f);
                graphics.pose().rotateAround(
                        new org.joml.Quaternionf().rotationZ((float) Math.toRadians(rotation)),
                        0, 0, 0
                );

                int halfSize = patternSize / 2;
                graphics.fill(
                        -halfSize, -halfSize,
                        halfSize, halfSize,
                        patternColor
                );

                graphics.pose().popPose();
            }
        }
    }

    private void renderGlow(GuiGraphics graphics) {
        int centerX = this.width / 2;
        int centerY = (this.height / 2) + (int) diskBobOffset;

        // Brighter radial glow effect
        int glowSize = 220;
        int glowColor = currentRarity.getColor();

        // Draw multiple translucent circles for glow (BRIGHTER!)
        for (int i = 0; i < 6; i++) {
            int size = glowSize - (i * 25);
            int alpha = 60 - (i * 8); // Increased alpha for brighter glow
            int color = (alpha << 24) | glowColor;

            graphics.fill(
                    centerX - size/2, centerY - size/2,
                    centerX + size/2, centerY + size/2,
                    color
            );
        }
    }

    private void renderDisk(GuiGraphics graphics) {
        int centerX = this.width / 2;
        int centerY = (this.height / 2) + (int) diskBobOffset;

        // Render encrypted floppy texture
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                UpgradingMod.MODID,
                "textures/gui/encrypted_floppy.png"
        );

        // Calculate which side is facing us
        float normalizedFlip = disk3DFlip % 360f;
        boolean showFront = (normalizedFlip >= 0 && normalizedFlip < 90) || (normalizedFlip >= 270 && normalizedFlip < 360);

        // Apply scale and ALL rotations together!
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 100); // PUSH FORWARD IN Z!

        // Apply zoom scale
        graphics.pose().scale(diskScale, diskScale, 1.0f);

        // Apply 3D flip on Y-axis FIRST (card flip!)
        if (disk3DFlip > 0) {
            graphics.pose().rotateAround(
                    new org.joml.Quaternionf().rotationY((float) Math.toRadians(disk3DFlip)),
                    0, 0, 0
            );
        }

        // THEN apply Z-axis rotation (click + idle) for grace!
        float totalRotation = diskRotation + diskIdleRotation;
        if (totalRotation != 0) {
            graphics.pose().rotateAround(
                    new org.joml.Quaternionf().rotationZ((float) Math.toRadians(totalRotation)),
                    0, 0, 0
            );
        }

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest(); // Ignore depth - always render on top!

        // Only render the side that's facing the camera!
        if (showFront) {
            // Front side (normal)
            graphics.blit(
                    //net.minecraft.client.renderer.RenderType::gui,
                    texture,
                    -DISK_SIZE/2,
                    -DISK_SIZE/2,
                    0, 0,
                    DISK_SIZE, DISK_SIZE,
                    DISK_SIZE, DISK_SIZE
            );
        } else {
            // Back side (flipped horizontally)
            graphics.pose().pushPose();
            graphics.pose().scale(-1.0f, 1.0f, 1.0f);
            graphics.blit(
                    //net.minecraft.client.renderer.RenderType::gui,
                    texture,
                    -DISK_SIZE/2,
                    -DISK_SIZE/2,
                    0, 0,
                    DISK_SIZE, DISK_SIZE,
                    DISK_SIZE, DISK_SIZE
            );
            graphics.pose().popPose();
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        // CRITICAL: Reset shader color to prevent bleeding into other rendering!
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        graphics.pose().popPose();
    }

    private void renderUpgradeOrbs(GuiGraphics graphics) {
        Component upgradeText = Component.translatable("gui.upgrading.encrypted_floppy.upgrade_chances");
        int textWidth = this.font.width(upgradeText);
        graphics.drawString(this.font, upgradeText,
                (this.width - textWidth) / 2, this.height - 140, // Lower position
                0xFFFFFF, true);

        // Calculate orb positions (right to left)
        int totalWidth = (ORB_SIZE * 4) + (ORB_SPACING * 3);
        int startX = (this.width - totalWidth) / 2;
        int orbY = this.height - 70;

        for (int i = 0; i < 4; i++) {
            int orbX = startX + (i * (ORB_SIZE + ORB_SPACING));

            // Clicks disappear from right to left
            int orbIndex = 3 - i;
            boolean isUsed = orbIndex >= clicksRemaining;
            boolean isCurrent = orbIndex == (clicksRemaining - 1);

            // Apply hover animation
            float hoverY = (float) Math.sin(orbHoverOffsets[i]) * 5.0f;

            renderOrb(graphics, orbX, (int)(orbY + hoverY), isUsed, isCurrent);
        }
    }

    private void renderOrb(GuiGraphics graphics, int x, int y, boolean isUsed, boolean isCurrent) {
        // Use textures
        String textureName = isUsed ? "orb_used" : (isCurrent ? "orb_highlight" : "orb_default");
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                UpgradingMod.MODID,
                "textures/gui/" + textureName + ".png"
        );

        RenderSystem.enableBlend();
        graphics.blit(
                //net.minecraft.client.renderer.RenderType::gui,
                texture,
                x, y,
                0, 0,
                ORB_SIZE, ORB_SIZE,
                ORB_SIZE, ORB_SIZE
        );
        RenderSystem.disableBlend();

        // Reset shader color!
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        if (isFlashing) return true;

        if (readyToOpen) {
            // Start white flash and claim
            startFlash();
            return true;
        }

        if (clicksRemaining > 0) {
            // Try to upgrade rarity
            boolean upgraded = attemptUpgrade();
            clicksRemaining--;

            // Animate disk - DIFFERENT SCALES
            if (upgraded) {
                diskScale = 1.65f; // Big zoom on upgrade!
            } else {
                diskScale = 1.1f; // Small zoom on normal click
            }
            diskRotation = (random.nextFloat() - 0.5f) * 30.0f;

            if (clicksRemaining == 0) {
                readyToOpen = true;
            }

            // Play click sound
            minecraft.getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK,
                            1.5f
                    )
            );

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean attemptUpgrade() {
        float roll = random.nextFloat();
        boolean upgraded = false;

        switch (currentRarity) {
            case BASIC:
                if (roll < BASIC_TO_RARE) {
                    currentRarity = DiskRarity.RARE;
                    upgraded = true;
                }
                break;
            case RARE:
                if (roll < RARE_TO_EPIC) {
                    currentRarity = DiskRarity.EPIC;
                    upgraded = true;
                }
                break;
            case EPIC:
                if (roll < EPIC_TO_LEGENDARY) {
                    currentRarity = DiskRarity.LEGENDARY;
                    upgraded = true;
                }
                break;
            case LEGENDARY:
                if (roll < LEGENDARY_TO_MYTHIC) {
                    currentRarity = DiskRarity.MYTHIC;
                    upgraded = true;
                }
                break;
            case MYTHIC:
                // Already max rarity
                break;
        }

        if (upgraded) {
            // Play upgrade sound
            minecraft.getSoundManager().play(
                    net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                            net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                            1.2f
                    )
            );

            // TRIGGER 3D FLIP! (360 degree FAST spin) + keep zoom!
            disk3DFlip = 360f;
            // DON'T override diskScale - it's already set to 1.65f from mouseClicked!
        }

        return upgraded;
    }

    private void startFlash() {
        isFlashing = true;
        flashTicks = 0;

        // Play opening sound
        minecraft.getSoundManager().play(
                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        net.minecraft.sounds.SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        1.0f
                )
        );
    }

    private void claimDisk() {
        // Send packet to server to give disk
        PacketDistributor.sendToServer(new ClaimEncryptedFloppyPacket(currentRarity.name()));

        // Close screen immediately
        this.onClose();
    }

    @Override
    public void onClose() {
        // If closing WITHOUT claiming (ESC pressed), claim current rarity anyway!
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