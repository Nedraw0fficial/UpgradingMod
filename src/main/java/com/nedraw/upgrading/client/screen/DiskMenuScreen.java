package com.nedraw.upgrading.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DiskMenuScreen extends Screen {
    // Layout constants
    private static final int SCREEN_WIDTH = 300;
    private static final int SCREEN_HEIGHT = 200;

    private static final int DISK_LIST_X = 10;
    private static final int DISK_LIST_Y = 20;
    private static final int DISK_LIST_WIDTH = 80;
    private static final int DISK_SIZE = 48;
    private static final int DISK_SPACING = 4;

    private static final int SLOT_START_X = 120;
    private static final int SLOT_Y = 20;
    private static final int SLOT_SIZE = 64;
    private static final int SLOT_SPACING = 10;

    private static final int HOVER_PANEL_X = 100;
    private static final int HOVER_PANEL_Y = 120;
    private static final int HOVER_PANEL_WIDTH = 180;
    private static final int HOVER_PANEL_HEIGHT = 70;

    // State
    private PlayerDiskData diskData;
    private List<String> unlockedDiskIds;
    private int scrollOffset = 0;
    private String heldDiskId = null; // Disk being moved
    private String hoveredDiskId = null;
    private boolean hoveringUpgradeButton = false;

    // Calculated positions
    private int leftPos;
    private int topPos;

    public DiskMenuScreen() {
        super(Component.translatable("gui.upgrading.disk_menu.title"));
    }

    @Override
    protected void init() {
        super.init();

        // Center the screen
        this.leftPos = (this.width - SCREEN_WIDTH) / 2;
        this.topPos = (this.height - SCREEN_HEIGHT) / 2;

        // Load player data
        if (minecraft != null && minecraft.player != null) {
            diskData = PlayerDiskData.get(minecraft.player);
            unlockedDiskIds = new ArrayList<>(diskData.getUnlockedDisks());

            // DEBUG: Print unlocked disks
            System.out.println("=== DISK MENU DEBUG ===");
            System.out.println("Unlocked disks: " + unlockedDiskIds);
            System.out.println("Number of disks: " + unlockedDiskIds.size());
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render dark background
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Render main panel background
        graphics.fill(leftPos, topPos, leftPos + SCREEN_WIDTH, topPos + SCREEN_HEIGHT, 0xDD000000);

        // Render components
        renderDiskList(graphics, mouseX, mouseY);
        renderEquipmentSlots(graphics, mouseX, mouseY);
        renderHoverInfo(graphics, mouseX, mouseY);

        // Render held disk on top of everything
        if (heldDiskId != null) {
            renderDiskAt(graphics, heldDiskId, mouseX - 24, mouseY - 24, 48);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderDiskList(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos + DISK_LIST_X;
        int y = topPos + DISK_LIST_Y;

        // Draw scrollable list background
        graphics.fill(x - 2, y - 2, x + DISK_LIST_WIDTH + 2, topPos + SCREEN_HEIGHT - 10, 0x88222222);

        // Render each unlocked disk
        int index = 0;
        for (String diskId : unlockedDiskIds) {
            if (diskId.equals(heldDiskId)) continue; // Don't render if being held

            int diskY = y + (index * (DISK_SIZE + DISK_SPACING)) - scrollOffset;

            // Only render if visible
            if (diskY + DISK_SIZE > y - DISK_SIZE && diskY < topPos + SCREEN_HEIGHT) {
                renderDiskAt(graphics, diskId, x + 16, diskY, DISK_SIZE);

                // Check if hovered
                if (isMouseOver(mouseX, mouseY, x, diskY, DISK_SIZE, DISK_SIZE)) {
                    hoveredDiskId = diskId;
                    graphics.fill(x, diskY, x + DISK_SIZE, diskY + DISK_SIZE, 0x44FFFFFF);
                }
            }

            index++;
        }
    }

    private void renderEquipmentSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int slot = 0; slot < 3; slot++) {
            int slotX = leftPos + SLOT_START_X + (slot * (SLOT_SIZE + SLOT_SPACING));
            int slotY = topPos + SLOT_Y;

            // Draw slot background
            graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x88444444);

            // Draw equipped disk
            String equippedDiskId = diskData.getEquippedDisk(slot);
            if (equippedDiskId != null && !equippedDiskId.equals(heldDiskId)) {
                renderDiskAt(graphics, equippedDiskId, slotX, slotY, SLOT_SIZE);
            }

            // Highlight if hovered
            if (isMouseOver(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x44FFFFFF);
            }
        }
    }

    private void renderHoverInfo(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredDiskId == null) return;

        UpgradeDisk disk = DiskRegistry.getDisk(hoveredDiskId);
        if (disk == null) return;

        int x = leftPos + HOVER_PANEL_X;
        int y = topPos + HOVER_PANEL_Y;

        // Background
        graphics.fill(x, y, x + HOVER_PANEL_WIDTH, y + HOVER_PANEL_HEIGHT, 0xDD000000);
        graphics.fill(x, y, x + HOVER_PANEL_WIDTH, y + 1, 0xFFFFFFFF); // Top border

        // Disk name
        graphics.drawString(this.font, disk.getDisplayName(), x + 5, y + 5, 0xFFFFFF);

        // Rarity and level
        int level = diskData.getDiskLevel(hoveredDiskId);
        String rarityText = disk.getRarity().name();
        String levelText = "Lv. " + level;

        graphics.drawString(this.font, rarityText, x + 5, y + 18, disk.getRarity().getColor());
        graphics.drawString(this.font, levelText, x + HOVER_PANEL_WIDTH - 40, y + 18, 0xFFFF55);

        // Description
        String description = disk.getDescriptionForLevel(level);
        graphics.drawString(this.font, description, x + 5, y + 32, 0xCCCCCC);

        // Upgrade button (if can upgrade)
        if (disk.canUpgrade(level)) {
            int buttonX = x + 5;
            int buttonY = y + 50;
            int buttonWidth = 80;
            int buttonHeight = 15;

            boolean hoveringButton = isMouseOver(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight);
            hoveringUpgradeButton = hoveringButton;

            // Button background
            graphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight,
                    hoveringButton ? 0xFF55AA55 : 0xFF338833);

            // Button text
            graphics.drawString(this.font, "UPGRADE!", buttonX + 5, buttonY + 4, 0xFFFFFF);

            // XP cost
            int xpCost = disk.getRarity().getXpCostForLevel(level);
            String costText = "Cost: " + xpCost + " XP";
            graphics.drawString(this.font, costText, buttonX + buttonWidth + 10, buttonY + 4, 0xFFFF55);
        }
    }

    private void renderDiskAt(GuiGraphics graphics, String diskId, int x, int y, int size) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                UpgradingMod.MODID,
                "textures/gui/disks/" + diskId + "_disk.png"
        );

        RenderSystem.enableBlend();
        graphics.blit(
                net.minecraft.client.renderer.RenderType::guiTextured,
                texture,
                x, y,
                0, 0,
                size, size,
                size, size
        );
        RenderSystem.disableBlend();
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button); // Left click only

        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Check if clicking upgrade button
        if (hoveringUpgradeButton && hoveredDiskId != null) {
            // TODO: Implement upgrade logic
            return true;
        }

        // Check if clicking a disk in the list
        int listX = leftPos + DISK_LIST_X;
        int listY = topPos + DISK_LIST_Y;
        int index = 0;

        for (String diskId : unlockedDiskIds) {
            int diskY = listY + (index * (DISK_SIZE + DISK_SPACING)) - scrollOffset;

            if (isMouseOver(mx, my, listX, diskY, DISK_SIZE, DISK_SIZE)) {
                if (heldDiskId == null) {
                    heldDiskId = diskId; // Pick up disk
                } else {
                    // TODO: Swap logic if needed
                }
                return true;
            }
            index++;
        }

        // Check if clicking an equipment slot
        for (int slot = 0; slot < 3; slot++) {
            int slotX = leftPos + SLOT_START_X + (slot * (SLOT_SIZE + SLOT_SPACING));
            int slotY = topPos + SLOT_Y;

            if (isMouseOver(mx, my, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                if (heldDiskId != null) {
                    // Place disk in slot
                    diskData.equipDisk(heldDiskId, slot);
                    heldDiskId = null;
                } else {
                    // Pick up from slot
                    String equippedDisk = diskData.getEquippedDisk(slot);
                    if (equippedDisk != null) {
                        heldDiskId = equippedDisk;
                        diskData.unequipSlot(slot);
                    }
                }
                return true;
            }
        }

        // Clicked empty space - drop held disk
        if (heldDiskId != null) {
            heldDiskId = null;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll the disk list
        scrollOffset -= (int) (scrollY * 10);
        scrollOffset = Math.max(0, scrollOffset);
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game
    }
}