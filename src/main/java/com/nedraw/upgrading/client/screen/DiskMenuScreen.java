package com.nedraw.upgrading.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import com.nedraw.upgrading.network.packet.EquipDiskPacket;
import com.nedraw.upgrading.network.packet.UpgradeDiskPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DiskMenuScreen extends Screen {
    // Layout constants - POLISHED
    private static final int SCREEN_WIDTH = 400;
    private static final int SCREEN_HEIGHT = 240;

    private static final int DISK_LIST_X = 15;
    private static final int DISK_LIST_Y = 30;
    private static final int DISK_LIST_WIDTH = 70;
    private static final int DISK_LIST_HEIGHT = 180;
    private static final int DISK_SIZE = 48;
    private static final int DISK_SPACING = 8;

    // CENTERED SLOTS
    private static final int SLOT_START_X = 150;
    private static final int SLOT_Y = 40;
    private static final int SLOT_SIZE = 56;
    private static final int SLOT_SPACING = 12;

    private static final int HOVER_PANEL_X = 100;
    private static final int HOVER_PANEL_Y = 140;
    private static final int HOVER_PANEL_WIDTH = 280;
    private static final int HOVER_PANEL_HEIGHT = 85;

    // State
    private PlayerDiskData diskData;
    private List<String> unlockedDiskIds;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private String heldDiskId = null;
    private String hoveredDiskId = null;
    private boolean hoveringUpgradeButton = false;

    private int leftPos;
    private int topPos;

    public DiskMenuScreen() {
        super(Component.literal("Upgrade Disks"));
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - SCREEN_WIDTH) / 2;
        this.topPos = (this.height - SCREEN_HEIGHT) / 2;

        if (minecraft != null && minecraft.player != null) {
            diskData = PlayerDiskData.get(minecraft.player);
            unlockedDiskIds = new ArrayList<>(diskData.getUnlockedDisks());

            // Calculate max scroll
            int totalHeight = unlockedDiskIds.size() * (DISK_SIZE + DISK_SPACING);
            maxScroll = Math.max(0, totalHeight - DISK_LIST_HEIGHT);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Store previous hovered disk
        String previousHoveredDisk = hoveredDiskId;

        // Reset hover state each frame
        hoveredDiskId = null;
        hoveringUpgradeButton = false;

        // Main panel
        int panelColor = 0xE0101010;
        graphics.fill(leftPos, topPos, leftPos + SCREEN_WIDTH, topPos + SCREEN_HEIGHT, panelColor);

        // Border
        graphics.fill(leftPos, topPos, leftPos + SCREEN_WIDTH, topPos + 2, 0xFF444444);
        graphics.fill(leftPos, topPos + SCREEN_HEIGHT - 2, leftPos + SCREEN_WIDTH, topPos + SCREEN_HEIGHT, 0xFF444444);
        graphics.fill(leftPos, topPos, leftPos + 2, topPos + SCREEN_HEIGHT, 0xFF444444);
        graphics.fill(leftPos + SCREEN_WIDTH - 2, topPos, leftPos + SCREEN_WIDTH, topPos + SCREEN_HEIGHT, 0xFF444444);

        // Title
        graphics.drawString(this.font, this.title, leftPos + 10, topPos + 8, 0xFFFFFF);

        // Render disk list and equipment slots (these set hoveredDiskId if hovering a disk)
        renderDiskList(graphics, mouseX, mouseY);
        renderEquipmentSlots(graphics, mouseX, mouseY);

        // CRITICAL FIX: Keep the hovered disk visible when mouse moves toward/in the hover panel area
        // We expand the zone to include the space between the slots and the panel
        if (hoveredDiskId == null && previousHoveredDisk != null) {
            int panelX = leftPos + HOVER_PANEL_X;
            int panelY = topPos + HOVER_PANEL_Y;

            // Expanded zone: from top of equipment slots down to bottom of panel, full width of panel
            int zoneTop = topPos + SLOT_Y; // Start from equipment slots
            int zoneBottom = panelY + HOVER_PANEL_HEIGHT; // End at bottom of panel

            // Check if mouse is in the expanded zone
            boolean inExpandedZone = mouseX >= panelX &&
                    mouseX <= panelX + HOVER_PANEL_WIDTH &&
                    mouseY >= zoneTop &&
                    mouseY <= zoneBottom;

            if (inExpandedZone) {
                hoveredDiskId = previousHoveredDisk;
            }
        }

        // Now render the hover info (which includes the upgrade button)
        renderHoverInfo(graphics, mouseX, mouseY);

        // Render held disk last
        if (heldDiskId != null) {
            renderDiskAt(graphics, heldDiskId, mouseX - DISK_SIZE/2, mouseY - DISK_SIZE/2, DISK_SIZE);
        }
    }

    private void renderDiskList(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos + DISK_LIST_X;
        int y = topPos + DISK_LIST_Y;

        // List background
        graphics.fill(x - 4, y - 4, x + DISK_LIST_WIDTH + 4, y + DISK_LIST_HEIGHT + 4, 0x80000000);

        // Enable scissor (clipping) so disks don't render outside
        graphics.enableScissor(x - 2, y - 2, x + DISK_LIST_WIDTH + 2, y + DISK_LIST_HEIGHT + 2);

        int index = 0;
        for (String diskId : unlockedDiskIds) {
            if (diskId.equals(heldDiskId)) {
                index++;
                continue;
            }

            int diskX = x + (DISK_LIST_WIDTH - DISK_SIZE) / 2;
            int diskY = y + (index * (DISK_SIZE + DISK_SPACING)) - scrollOffset;

            // Only render if in visible area
            if (diskY + DISK_SIZE >= y && diskY <= y + DISK_LIST_HEIGHT) {
                renderDiskAt(graphics, diskId, diskX, diskY, DISK_SIZE);

                // Check if mouse is over THIS disk - UPDATE hoveredDiskId
                if (isMouseOver(mouseX, mouseY, diskX, diskY, DISK_SIZE, DISK_SIZE)) {
                    hoveredDiskId = diskId;
                    graphics.fill(diskX - 2, diskY - 2, diskX + DISK_SIZE + 2, diskY + DISK_SIZE + 2, 0x80FFFFFF);
                }
            }

            index++;
        }

        graphics.disableScissor();
    }

    private void renderEquipmentSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int slot = 0; slot < 3; slot++) {
            int slotX = leftPos + SLOT_START_X + (slot * (SLOT_SIZE + SLOT_SPACING));
            int slotY = topPos + SLOT_Y;

            // Slot background
            graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x80333333);
            graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + 1, 0xFF555555);
            graphics.fill(slotX, slotY + SLOT_SIZE - 1, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0xFF555555);
            graphics.fill(slotX, slotY, slotX + 1, slotY + SLOT_SIZE, 0xFF555555);
            graphics.fill(slotX + SLOT_SIZE - 1, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0xFF555555);

            // Equipped disk
            String equippedDiskId = diskData.getEquippedDisk(slot);
            if (equippedDiskId != null && !equippedDiskId.equals(heldDiskId)) {
                renderDiskAt(graphics, equippedDiskId, slotX + 4, slotY + 4, DISK_SIZE);

                // Check if hovering equipped disk - UPDATE hoveredDiskId
                if (isMouseOver(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                    hoveredDiskId = equippedDiskId;
                }
            }

            // Hover highlight
            if (isMouseOver(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x4066FF66);
            }
        }
    }

    private void renderHoverInfo(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredDiskId == null) return;

        UpgradeDisk disk = DiskRegistry.getDisk(hoveredDiskId);
        if (disk == null) return;

        int x = leftPos + HOVER_PANEL_X;
        int y = topPos + HOVER_PANEL_Y;

        // Get rarity color for border
        int rarityColor = disk.getRarity().getColor();

        // Panel background
        graphics.fill(x, y, x + HOVER_PANEL_WIDTH, y + HOVER_PANEL_HEIGHT, 0xE0000000);

        // COLORED TOP BORDER (rarity color)
        graphics.fill(x, y, x + HOVER_PANEL_WIDTH, y + 2, 0xFF000000 | rarityColor);

        // Disk name
        graphics.drawString(this.font, disk.getDisplayName(), x + 5, y + 5, 0xFFFFFF);

        // Rarity and level - REFRESH FROM CURRENT DATA
        int level = diskData.getDiskLevel(hoveredDiskId);
        String rarityText = disk.getRarity().name();
        String levelText = "Lv. " + level;

        // Draw rarity with underline
        int rarityTextX = x + 5;
        int rarityTextY = y + 18;
        graphics.drawString(this.font, rarityText, rarityTextX, rarityTextY, rarityColor);

        // Draw underline for rarity
        int rarityTextWidth = this.font.width(rarityText);
        graphics.fill(rarityTextX, rarityTextY + 9, rarityTextX + rarityTextWidth, rarityTextY + 10, 0xFF000000 | rarityColor);

        graphics.drawString(this.font, levelText, x + HOVER_PANEL_WIDTH - 45, y + 18, 0xFFFF55);

        // Description - REFRESH FROM CURRENT LEVEL
        String description = disk.getDescriptionForLevel(level);

        // Support multi-line descriptions (split by \n)
        String[] lines = description.split("\n");
        int lineY = y + 32;
        for (String line : lines) {
            graphics.drawString(this.font, line, x + 5, lineY, 0xCCCCCC);
            lineY += 10; // Move down for next line
        }

        // Upgrade button
        if (disk.canUpgrade(level)) {
            int buttonX = x + 5;
            int buttonY = y + 55;
            int buttonWidth = 80;
            int buttonHeight = 20;

            boolean hovering = isMouseOver(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight);
            hoveringUpgradeButton = hovering;

            graphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight,
                    hovering ? 0xFF55AA55 : 0xFF338833);

            graphics.drawCenteredString(this.font, "UPGRADE!", buttonX + buttonWidth/2, buttonY + 6, 0xFFFFFF);

            int xpCost = disk.getRarity().getXpCostForLevel(level);
            int playerXP = diskData.getTotalXP(minecraft.player);

            String costText = "Cost: " + xpCost + " XP";
            int costColor = playerXP >= xpCost ? 0x55FF55 : 0xFF5555;

            graphics.drawString(this.font, costText, buttonX + buttonWidth + 10, buttonY + 6, costColor);

            // Show player's current XP below
            String currentXP = "You have: " + playerXP + " XP";
            graphics.drawString(this.font, currentXP, buttonX, buttonY + 22, 0xAAAAA);
        }
    }

    private void renderDiskAt(GuiGraphics graphics, String diskId, int x, int y, int size) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                UpgradingMod.MODID,
                "textures/gui/disks/" + diskId + "_disk.png"
        );

        RenderSystem.enableBlend();
        graphics.blit(
                //used in 1.21.3
                //net.minecraft.client.renderer.RenderType::gui,
                texture,
                x, y,
                0, 0,
                size, size,
                size, size
        );
        RenderSystem.disableBlend();

        // CRITICAL: Reset shader color to prevent black armor bug!
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Upgrade button
        if (hoveringUpgradeButton && hoveredDiskId != null) {
            // Get current level and check if we can upgrade
            int currentLevel = diskData.getDiskLevel(hoveredDiskId);
            UpgradeDisk disk = DiskRegistry.getDisk(hoveredDiskId);

            if (disk != null && disk.canUpgrade(currentLevel)) {
                int xpCost = disk.getRarity().getXpCostForLevel(currentLevel);
                int playerXP = diskData.getTotalXP(minecraft.player);

                // Only proceed if player has enough XP
                if (playerXP >= xpCost) {
                    // Send packet to server
                    PacketDistributor.sendToServer(new UpgradeDiskPacket(hoveredDiskId));

                    // OPTIMISTIC UPDATE: Immediately update local data
                    // This prevents the 1-2 tick delay before server sync
                    diskData.upgradeDisk(hoveredDiskId);

                    minecraft.getSoundManager().play(
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK,
                                    1.0f
                            )
                    );
                } else {
                    // Not enough XP - show message
                    minecraft.player.displayClientMessage(
                            Component.literal("Not enough XP!").withStyle(style -> style.withColor(0xFF5555)),
                            true
                    );

                    minecraft.getSoundManager().play(
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                    net.minecraft.sounds.SoundEvents.VILLAGER_NO,
                                    1.0f
                            )
                    );
                }
            }

            return true;
        }

        // Disk list clicks
        int listX = leftPos + DISK_LIST_X + (DISK_LIST_WIDTH - DISK_SIZE) / 2;
        int listY = topPos + DISK_LIST_Y;
        int index = 0;

        for (String diskId : unlockedDiskIds) {
            int diskY = listY + (index * (DISK_SIZE + DISK_SPACING)) - scrollOffset;

            if (diskY >= listY && diskY + DISK_SIZE <= listY + DISK_LIST_HEIGHT) {
                if (isMouseOver(mx, my, listX, diskY, DISK_SIZE, DISK_SIZE)) {
                    heldDiskId = (heldDiskId == null) ? diskId : null;
                    return true;
                }
            }
            index++;
        }

        // Equipment slot clicks
        for (int slot = 0; slot < 3; slot++) {
            int slotX = leftPos + SLOT_START_X + (slot * (SLOT_SIZE + SLOT_SPACING));
            int slotY = topPos + SLOT_Y;

            if (isMouseOver(mx, my, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                if (heldDiskId != null) {
                    PacketDistributor.sendToServer(new EquipDiskPacket(heldDiskId, slot, false));
                    heldDiskId = null;

                    minecraft.getSoundManager().play(
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                    net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC.value(),
                                    1.2f
                            )
                    );
                } else {
                    String equippedDisk = diskData.getEquippedDisk(slot);
                    if (equippedDisk != null) {
                        PacketDistributor.sendToServer(new EquipDiskPacket(equippedDisk, slot, true));
                        heldDiskId = equippedDisk;

                        minecraft.getSoundManager().play(
                                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                        net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                        0.8f
                                )
                        );
                    }
                }
                return true;
            }
        }

        heldDiskId = null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset -= (int) (scrollY * (DISK_SIZE + DISK_SPACING));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (minecraft != null && minecraft.player != null) {
            // ALWAYS refresh diskData to catch server updates (like upgrades)
            diskData = PlayerDiskData.get(minecraft.player);

            Set<String> currentUnlocked = diskData.getUnlockedDisks();
            if (unlockedDiskIds.size() != currentUnlocked.size() || !unlockedDiskIds.containsAll(currentUnlocked)) {
                unlockedDiskIds = new ArrayList<>(currentUnlocked);

                int totalHeight = unlockedDiskIds.size() * (DISK_SIZE + DISK_SPACING);
                maxScroll = Math.max(0, totalHeight - DISK_LIST_HEIGHT);
            }
        }
    }
}