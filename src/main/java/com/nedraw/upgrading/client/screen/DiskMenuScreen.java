package com.nedraw.upgrading.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.disk.UpgradeDisk;
import com.nedraw.upgrading.network.packet.EquipDiskPacket;
import com.nedraw.upgrading.network.packet.UpgradeDiskPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DiskMenuScreen extends Screen {

    // =====================
    // TEXTURE RESOURCES
    // =====================
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/background.png");
    private static final ResourceLocation BUTTON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/button.png");
    private static final ResourceLocation XP_ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/xp_icon.png");

    private static ResourceLocation getOverlayTexture(DiskRarity rarity) {
        String name = switch (rarity) {
            case BASIC     -> "overlay_basic";
            case RARE      -> "overlay_rare";
            case EPIC      -> "overlay_epic";
            case LEGENDARY -> "overlay_legendary";
            case MYTHIC    -> "overlay_mythic";
        };
        return ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/" + name + ".png");
    }

    // Background texture size
    private static final int BG_TEX_W = 200;
    private static final int BG_TEX_H = 120;

    // =====================
    // LAYOUT CONSTANTS (game space = texture * 2)
    // =====================
    private static final int SCREEN_WIDTH  = 400;
    private static final int SCREEN_HEIGHT = 240;

    // Disk list
    private static final int DISK_LIST_X      = 12;
    private static final int DISK_LIST_Y      = 38; //34
    private static final int DISK_LIST_WIDTH  = 76;
    private static final int DISK_LIST_HEIGHT = 182; //186
    private static final int DISK_SIZE        = 64; //48
    private static final int DISK_SPACING     = 4; //8

    // Search bar
    private static final int SEARCH_X = 10;
    private static final int SEARCH_Y = 18;
    private static final int SEARCH_W = 80;
    private static final int SEARCH_H = 12;

    // Slots — fixed alignment to match background texture
    private static final int SLOT_SIZE = 56;
    private static final int SLOT_Y    = 40;
    private static final int SLOT_1_X  = 150;
    private static final int SLOT_2_X  = 218;
    private static final int SLOT_3_X  = 286;

    // Info panel — with proper padding
    private static final int INFO_PADDING = 8;
    private static final int INFO_X = 100 + INFO_PADDING;  // left edge of info area + padding
    private static final int INFO_Y = 148;                 // top of info text
    private static final int INFO_W = 290 - INFO_PADDING;  // available width

    // Description area (scrollable)
    private static final int DESC_Y = 162;
    private static final int DESC_W = 276;
    private static final int DESC_H = 34;

    // Upgrade button
    private static final int BUTTON_X = 106;
    private static final int BUTTON_Y = 198;
    private static final int BUTTON_W = 72;
    private static final int BUTTON_H = 20;

    // Button nine-slice params
    private static final int BTN_CORNER  = 4;
    private static final int BTN_TEX_W   = 9;
    private static final int BTN_STATE_NORMAL = 0;
    private static final int BTN_STATE_HOVER  = 9;

    // XP icon + cost — right next to button
    private static final int XP_ICON_X = 186;
    private static final int XP_ICON_Y = 204;
    private static final int XP_ICON_W = 8;
    private static final int XP_ICON_H = 8;

    // =====================
    // STATE
    // =====================
    private PlayerDiskData diskData;
    private List<String> sortedDiskIds  = new ArrayList<>();
    private List<String> filteredDiskIds = new ArrayList<>();

    private int scrollOffset     = 0;
    private int maxScroll        = 0;
    private int descScrollOffset = 0;
    private int descMaxScroll    = 0;

    private String  heldDiskId           = null;
    private String  hoveredDiskId        = null;
    private boolean hoveringUpgradeButton = false;
    private boolean searchFocused        = false;
    private String  searchText           = "";

    private int animationTick = 0;
    private int leftPos, topPos;

    private List<String> descLines = new ArrayList<>();

    public DiskMenuScreen() {
        super(Component.translatable("gui.upgrading.disk_menu.title"));
    }

    // =====================
    // INIT
    // =====================
    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width  - SCREEN_WIDTH)  / 2;
        this.topPos  = (this.height - SCREEN_HEIGHT) / 2;

        if (minecraft != null && minecraft.player != null) {
            diskData = PlayerDiskData.get(minecraft.player);
            refreshSortedList();
        }
    }

    private void refreshSortedList() {
        Set<String> unlocked = diskData.getUnlockedDisks();
        sortedDiskIds = unlocked.stream()
                .sorted(Comparator
                        .comparingInt((String id) -> {
                            UpgradeDisk d = DiskRegistry.getDisk(id);
                            return d == null ? 999 : d.getRarity().ordinal();
                        })
                        .thenComparing(id -> {
                            UpgradeDisk d = DiskRegistry.getDisk(id);
                            return d == null ? id : d.getDisplayName();
                        }))
                .collect(Collectors.toList());
        applySearch();
    }

    private void applySearch() {
        String query = searchText.toLowerCase().trim();
        filteredDiskIds = query.isEmpty() ? new ArrayList<>(sortedDiskIds) :
                sortedDiskIds.stream()
                        .filter(id -> {
                            UpgradeDisk d = DiskRegistry.getDisk(id);
                            return d != null && d.getDisplayName().toLowerCase().contains(query);
                        })
                        .collect(Collectors.toList());

        int totalHeight = filteredDiskIds.size() * (DISK_SIZE + DISK_SPACING);
        maxScroll = Math.max(0, totalHeight - DISK_LIST_HEIGHT);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    // =====================
    // RENDER
    // =====================
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        String previousHoveredDisk = hoveredDiskId;
        hoveredDiskId = null;
        hoveringUpgradeButton = false;

        // 1. Background texture (200x120 → 400x240, 2x scale)
        RenderSystem.enableBlend();
        graphics.blit(BACKGROUND_TEXTURE,
                leftPos, topPos, SCREEN_WIDTH, SCREEN_HEIGHT,
                0, 0, BG_TEX_W, BG_TEX_H, BG_TEX_W, BG_TEX_H);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // 2. Rarity overlay (if a disk is hovered/selected)
        String displayDisk = hoveredDiskId != null ? hoveredDiskId : previousHoveredDisk;
        if (displayDisk != null) {
            UpgradeDisk overlayDisk = DiskRegistry.getDisk(displayDisk);
            if (overlayDisk != null) {
                RenderSystem.enableBlend();
                graphics.blit(getOverlayTexture(overlayDisk.getRarity()),
                        leftPos, topPos, SCREEN_WIDTH, SCREEN_HEIGHT,
                        0, 0, BG_TEX_W, BG_TEX_H, BG_TEX_W, BG_TEX_H);
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
        }

        // 3. Search bar
        renderSearchBar(graphics, mouseX, mouseY);

        // 4. Disk list
        renderDiskList(graphics, mouseX, mouseY);

        // 5. Equipment slots
        renderEquipmentSlots(graphics, mouseX, mouseY);

        // Persist hovered disk when mouse moves into info area
        if (hoveredDiskId == null && previousHoveredDisk != null) {
            if (mouseX >= leftPos + 104 && mouseX <= leftPos + SCREEN_WIDTH - 4 &&
                    mouseY >= topPos + SLOT_Y && mouseY <= topPos + SCREEN_HEIGHT - 4) {
                hoveredDiskId = previousHoveredDisk;
            }
        }

        // 6. Info panel
        renderInfoPanel(graphics, mouseX, mouseY);

        // 7. Held disk at cursor
        if (heldDiskId != null) {
            renderDiskAt(graphics, heldDiskId, mouseX - DISK_SIZE / 2, mouseY - DISK_SIZE / 2, DISK_SIZE);
        }
    }

    // =====================
    // SEARCH BAR
    // =====================
    private void renderSearchBar(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos + SEARCH_X;
        int y = topPos  + SEARCH_Y;

        if (searchFocused) {
            graphics.fill(x - 1, y - 1, x + SEARCH_W + 1, y + SEARCH_H + 1, 0xFF888888);
        }

        String display = searchText.isEmpty() && !searchFocused ? "§7Search..." : searchText;
        if (searchFocused && (animationTick / 10) % 2 == 0) display += "§f|";

        graphics.enableScissor(x, y, x + SEARCH_W, y + SEARCH_H);
        graphics.drawString(this.font, display, x + 2, y + 2, 0xFFFFFF, false);
        graphics.disableScissor();
    }

    // =====================
    // DISK LIST
    // =====================
    private void renderDiskList(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos + DISK_LIST_X;
        int y = topPos  + DISK_LIST_Y;

        graphics.enableScissor(x, y, x + DISK_LIST_WIDTH, y + DISK_LIST_HEIGHT);

        int index = 0;
        for (String diskId : filteredDiskIds) {
            if (diskId.equals(heldDiskId)) { index++; continue; }

            int diskX = x + (DISK_LIST_WIDTH - DISK_SIZE) / 2;
            int diskY = y + (index * (DISK_SIZE + DISK_SPACING)) - scrollOffset;

            if (diskY + DISK_SIZE >= y && diskY <= y + DISK_LIST_HEIGHT) {
                renderDiskAt(graphics, diskId, diskX, diskY, DISK_SIZE);
                if (isMouseOver(mouseX, mouseY, diskX, diskY, DISK_SIZE, DISK_SIZE)) {
                    hoveredDiskId = diskId;
                    graphics.fill(diskX - 1, diskY - 1,
                            diskX + DISK_SIZE + 1, diskY + DISK_SIZE + 1, 0x60FFFFFF);
                }
            }
            index++;
        }

        graphics.disableScissor();
    }

    // =====================
    // EQUIPMENT SLOTS
    // =====================
    private void renderEquipmentSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        int[] slotXs = {leftPos + SLOT_1_X, leftPos + SLOT_2_X, leftPos + SLOT_3_X};
        int slotY = topPos + SLOT_Y;

        for (int slot = 0; slot < 3; slot++) {
            int slotX = slotXs[slot];

            String equippedDiskId = diskData.getEquippedDisk(slot);
            if (equippedDiskId != null && !equippedDiskId.equals(heldDiskId)) {
                int innerPad = 2; //4
                renderDiskAt(graphics, equippedDiskId,
                        slotX + innerPad, slotY + innerPad, SLOT_SIZE - innerPad * 2);
                if (isMouseOver(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                    hoveredDiskId = equippedDiskId;
                }
            }

            if (isMouseOver(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x4066FF66);

                // Slot 1 MYTHIC tip when empty
                if (slot == 0 && equippedDiskId == null) {
                    renderMythicTip(graphics, slotX, slotY);
                }
            }
        }
    }

    private void renderMythicTip(GuiGraphics graphics, int slotX, int slotY) {
        int tipX = slotX;
        int tipY = slotY + SLOT_SIZE + 4;
        int tipW = 170;
        int tipH = 30;

        if (tipX + tipW > leftPos + SCREEN_WIDTH - 4) tipX = leftPos + SCREEN_WIDTH - tipW - 4;

        graphics.fill(tipX, tipY, tipX + tipW, tipY + tipH, 0xE0000033);
        graphics.fill(tipX, tipY, tipX + tipW, tipY + 1, 0xFF4444FF);
        graphics.drawString(this.font, "§bMYTHIC slot", tipX + 4, tipY + 5, 0xFFFFFF, false);
        graphics.drawString(this.font, "§7Press §eX §7to activate ability", tipX + 4, tipY + 16, 0xFFFFFF, false);
    }

    // =====================
    // INFO PANEL
    // =====================
    private void renderInfoPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        String diskId = hoveredDiskId;
        if (diskId == null) return;

        UpgradeDisk disk = DiskRegistry.getDisk(diskId);
        if (disk == null) return;

        int x = leftPos + INFO_X;
        int y = topPos  + INFO_Y;
        int level     = diskData.getDiskLevel(diskId);
        int rarityColor = disk.getRarity().getColor();

        // Disk name — bold + underlined + rarity color
        String nameText = disk.getDisplayName();
        graphics.drawString(this.font,
                Component.literal(nameText)
                        .withStyle(s -> s.withBold(true).withUnderlined(true).withColor(rarityColor)),
                x, y, rarityColor, false);

        // Level — just to the right with some spacing, not all the way to the edge
        String levelText = "Lv." + level;
        int levelX = leftPos + INFO_X + INFO_W - this.font.width(levelText) - 16;
        graphics.drawString(this.font, levelText, levelX, y, 0xFFFF55, false);;

        // Description with auto line-return + scrollable
        String rawDescription = disk.getDescriptionForLevel(level);
        descLines = new ArrayList<>();
        for (String paragraph : rawDescription.split("\n")) {
            descLines.addAll(manualWrap(paragraph, DESC_W));
        }
        descMaxScroll = Math.max(0, (descLines.size() * 10) - DESC_H);

        int descX = leftPos + INFO_X;
        int descY = topPos  + DESC_Y;

        graphics.enableScissor(descX, descY, descX + DESC_W, descY + DESC_H);
        int lineY = descY - descScrollOffset;
        for (String line : descLines) {
            if (lineY + 10 >= descY && lineY <= descY + DESC_H) {
                graphics.drawString(this.font, line, descX, lineY, 0xCCCCCC, false);
            }
            lineY += 10;
        }
        graphics.disableScissor();

        // Upgrade button
        if (disk.canUpgrade(level)) {
            int xpCost   = disk.getRarity().getXpCostForLevel(level);
            int playerXP = diskData.getTotalXP(minecraft.player);
            boolean canAfford = playerXP >= xpCost;

            boolean hovering = isMouseOver(mouseX, mouseY,
                    leftPos + BUTTON_X, topPos + BUTTON_Y, BUTTON_W, BUTTON_H);
            hoveringUpgradeButton = hovering;

            int btnState = hovering ? BTN_STATE_HOVER : BTN_STATE_NORMAL;
            renderNineSlicedButton(graphics,
                    leftPos + BUTTON_X, topPos + BUTTON_Y, BUTTON_W, BUTTON_H, btnState);

            graphics.drawCenteredString(this.font,
                    Component.translatable("gui.upgrading.disk_menu.upgrade"),
                    leftPos + BUTTON_X + BUTTON_W / 2, topPos + BUTTON_Y + 6, 0xFFFFFF);

            // XP icon
            RenderSystem.enableBlend();
            graphics.blit(XP_ICON_TEXTURE,
                    leftPos + XP_ICON_X, topPos + XP_ICON_Y,
                    XP_ICON_W, XP_ICON_H,
                    0, 0, 4, 4, 4, 4);
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            // XP cost text
            int costColor = canAfford ? 0x55FF55 : 0xFF5555;
            graphics.drawString(this.font, xpCost + " XP",
                    leftPos + XP_ICON_X + XP_ICON_W + 3, topPos + XP_ICON_Y + 1, costColor, false);
        }
    }

    private List<String> manualWrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;
            if (this.font.width(test) <= maxWidth) {
                current = new StringBuilder(test);
            } else {
                if (!current.isEmpty()) lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    // =====================
    // NINE-SLICED BUTTON
    // =====================
    private void renderNineSlicedButton(GuiGraphics graphics, int x, int y, int w, int h, int vOffset) {
        int c = BTN_CORNER;
        int tw = BTN_TEX_W;
        int th = 27; // total texture height (3 states * 9)
        int iw = w - c * 2;
        int ih = h - c * 2;

        // Corners
        graphics.blit(BUTTON_TEXTURE, x,         y,         c,  c,  0,    vOffset,         c,  c,  tw, th);
        graphics.blit(BUTTON_TEXTURE, x+w-c,     y,         c,  c,  tw-c, vOffset,         c,  c,  tw, th);
        graphics.blit(BUTTON_TEXTURE, x,         y+h-c,     c,  c,  0,    vOffset+BTN_TEX_W-c, c, c, tw, th);
        graphics.blit(BUTTON_TEXTURE, x+w-c,     y+h-c,     c,  c,  tw-c, vOffset+BTN_TEX_W-c, c, c, tw, th);
        // Edges
        graphics.blit(BUTTON_TEXTURE, x+c,       y,         iw, c,  c,    vOffset,         1,  c,  tw, th);
        graphics.blit(BUTTON_TEXTURE, x+c,       y+h-c,     iw, c,  c,    vOffset+BTN_TEX_W-c, 1, c, tw, th);
        graphics.blit(BUTTON_TEXTURE, x,         y+c,       c,  ih, 0,    vOffset+c,       c,  1,  tw, th);
        graphics.blit(BUTTON_TEXTURE, x+w-c,     y+c,       c,  ih, tw-c, vOffset+c,       c,  1,  tw, th);
        // Center
        graphics.blit(BUTTON_TEXTURE, x+c,       y+c,       iw, ih, c,    vOffset+c,       1,  1,  tw, th);
    }

    // =====================
    // DISK RENDERING
    // =====================
    private void renderDiskAt(GuiGraphics graphics, String diskId, int x, int y, int size) {
        UpgradeDisk disk = DiskRegistry.getDisk(diskId);
        if (disk != null && disk.isAnimated()) {
            renderAnimatedDiskAt(graphics, disk, x, y, size);
        } else {
            renderStaticDiskAt(graphics, diskId, x, y, size);
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private void renderStaticDiskAt(GuiGraphics graphics, String diskId, int x, int y, int size) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                UpgradingMod.MODID, "textures/gui/disks/" + diskId + "_disk.png");
        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, 0, 0, size, size, size, size);
        RenderSystem.disableBlend();
    }

    private void renderAnimatedDiskAt(GuiGraphics graphics, UpgradeDisk disk, int x, int y, int size) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                UpgradingMod.MODID, "textures/gui/disks/" + disk.getId() + "_disk.png");
        int frameSize = disk.getFrameSize();
        int frameCount = disk.getFrameCount();
        int currentFrame = (animationTick / disk.getTicksPerFrame()) % frameCount;
        int vOffset = currentFrame * frameSize;

        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, size, size, 0, vOffset,
                frameSize, frameSize, frameSize, frameCount * frameSize);
        RenderSystem.disableBlend();
    }

    // =====================
    // INPUT
    // =====================
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int mx = (int) mouseX, my = (int) mouseY;

        searchFocused = isMouseOver(mx, my, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_W, SEARCH_H);

        if (hoveringUpgradeButton && hoveredDiskId != null) {
            int currentLevel = diskData.getDiskLevel(hoveredDiskId);
            UpgradeDisk disk = DiskRegistry.getDisk(hoveredDiskId);
            if (disk != null && disk.canUpgrade(currentLevel)) {
                int xpCost = disk.getRarity().getXpCostForLevel(currentLevel);
                int playerXP = diskData.getTotalXP(minecraft.player);
                if (playerXP >= xpCost) {
                    PacketDistributor.sendToServer(new UpgradeDiskPacket(hoveredDiskId));
                    diskData.upgradeDisk(hoveredDiskId);
                    minecraft.getSoundManager().play(
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                    net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0f));
                } else {
                    minecraft.player.displayClientMessage(
                            Component.translatable("message.upgrading.not_enough_xp")
                                    .withStyle(s -> s.withColor(0xFF5555)), true);
                    minecraft.getSoundManager().play(
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                    net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0f));
                }
            }
            return true;
        }

        // Disk list
        int listX = leftPos + DISK_LIST_X + (DISK_LIST_WIDTH - DISK_SIZE) / 2;
        int listY = topPos  + DISK_LIST_Y;
        int index = 0;
        for (String diskId : filteredDiskIds) {
            int diskY = listY + (index * (DISK_SIZE + DISK_SPACING)) - scrollOffset;
            if (diskY >= listY && diskY + DISK_SIZE <= listY + DISK_LIST_HEIGHT) {
                if (isMouseOver(mx, my, listX, diskY, DISK_SIZE, DISK_SIZE)) {
                    heldDiskId = diskId.equals(heldDiskId) ? null : diskId;
                    return true;
                }
            }
            index++;
        }

        // Slots
        int[] slotXs = {leftPos + SLOT_1_X, leftPos + SLOT_2_X, leftPos + SLOT_3_X};
        for (int slot = 0; slot < 3; slot++) {
            int slotX = slotXs[slot];
            int slotY = topPos + SLOT_Y;
            if (isMouseOver(mx, my, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                if (heldDiskId != null) {
                    PacketDistributor.sendToServer(new EquipDiskPacket(heldDiskId, slot, false));
                    heldDiskId = null;
                    minecraft.getSoundManager().play(
                            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                    net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC.value(), 1.2f));
                } else {
                    String equipped = diskData.getEquippedDisk(slot);
                    if (equipped != null) {
                        PacketDistributor.sendToServer(new EquipDiskPacket(equipped, slot, true));
                        heldDiskId = equipped;
                        minecraft.getSoundManager().play(
                                net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                        net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 0.8f));
                    }
                }
                return true;
            }
        }

        heldDiskId = null;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == 259) { // Backspace
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                    applySearch();
                }
                return true;
            }
            if (keyCode == 256) { searchFocused = false; return true; } // Escape
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchFocused && searchText.length() < 30) {
            searchText += codePoint;
            applySearch();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int mx = (int) mouseX, my = (int) mouseY;
        int descX = leftPos + INFO_X;
        int descY = topPos  + DESC_Y;

        if (isMouseOver(mx, my, descX, descY, DESC_W, DESC_H)) {
            descScrollOffset -= (int)(scrollY * 10);
            descScrollOffset = Math.max(0, Math.min(descScrollOffset, descMaxScroll));
        } else {
            scrollOffset -= (int)(scrollY * (DISK_SIZE + DISK_SPACING));
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void tick() {
        super.tick();
        animationTick++;
        if (minecraft != null && minecraft.player != null) {
            diskData = PlayerDiskData.get(minecraft.player);
            Set<String> current = diskData.getUnlockedDisks();
            if (!new HashSet<>(sortedDiskIds).containsAll(current) ||
                    sortedDiskIds.size() != current.size()) {
                refreshSortedList();
            }
        }
    }

    private boolean isMouseOver(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}