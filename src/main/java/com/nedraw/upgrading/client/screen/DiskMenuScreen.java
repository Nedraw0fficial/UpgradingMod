package com.nedraw.upgrading.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.disk.UpgradeDisk;
import com.nedraw.upgrading.item.ZSlotItem;
import com.nedraw.upgrading.network.packet.EquipDiskPacket;
import com.nedraw.upgrading.network.packet.EquipZSlotPacket;
import com.nedraw.upgrading.network.packet.UpgradeDiskPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class DiskMenuScreen extends Screen {

    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/background.png");
    private static final ResourceLocation BUTTON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/button.png");
    private static final ResourceLocation XP_ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/xp_icon.png");
    private static final ResourceLocation FRAGMENT_ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/item/encrypted_fragment.png");
    private static final ResourceLocation SOCKET_OFF_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/socket_off.png");
    private static final ResourceLocation SOCKET_ON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/socket_on.png");
    private static final ResourceLocation SOCKET_MYTHIC_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "textures/gui/socket_mythic.png");

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

    private static ResourceLocation getPlatinumOverlay(UpgradeDisk disk) {
        return switch (disk.getRarity()) {
            case BASIC, RARE, EPIC ->
                    ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID,
                            "textures/gui/platinum/platinum_overlay_" + disk.getRarity().name().toLowerCase() + ".png");
            case LEGENDARY, MYTHIC ->
                    ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID,
                            "textures/gui/platinum/platinum_overlay_" + disk.getId() + ".png");
        };
    }

    private static ResourceLocation getZSlotLayerTexture(String type, String id) {
        return ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID,
                "textures/gui/zslot_layers/" + type + "s/" + type + "_" + id + ".png");
    }

    private static final int BG_TEX_W = 200;
    private static final int BG_TEX_H = 120;
    private static final int LAYER_TEX_SIZE = 32;

    private static final int SCREEN_WIDTH  = 400;
    private static final int SCREEN_HEIGHT = 240;

    private static final int DISK_LIST_X      = 12;
    private static final int DISK_LIST_Y      = 38;
    private static final int DISK_LIST_WIDTH  = 76;
    private static final int DISK_LIST_HEIGHT = 182;
    private static final int DISK_SIZE        = 64;
    private static final int DISK_SPACING     = 4;

    private static final int SEARCH_X = 10;
    private static final int SEARCH_Y = 18;
    private static final int SEARCH_W = 80;
    private static final int SEARCH_H = 12;

    private static final int SLOT_SIZE = 56;
    private static final int SLOT_Y    = 40;
    private static final int SLOT_1_X  = 150;
    private static final int SLOT_2_X  = 218;
    private static final int SLOT_3_X  = 286;

    private static final int INFO_PADDING = 8;
    private static final int INFO_X = 100 + INFO_PADDING;
    private static final int INFO_Y = 148;
    private static final int INFO_W = 290 - INFO_PADDING;

    private static final int DESC_Y = 162;
    private static final int DESC_W = 268;
    private static final int DESC_H = 34;

    private static final int BUTTON_X = 106;
    private static final int BUTTON_Y = 198;
    private static final int BUTTON_W = 72;
    private static final int BUTTON_H = 20;

    private static final int BTN_CORNER  = 4;
    private static final int BTN_TEX_W   = 9;
    private static final int BTN_STATE_NORMAL = 0;
    private static final int BTN_STATE_HOVER  = 9;

    private static final int XP_ICON_X = 186;
    private static final int XP_ICON_Y = 204;
    private static final int XP_ICON_W = 8;
    private static final int XP_ICON_H = 8;

    // Socket: 22x17 in BG texture space → 44x34 in game space (2x)
    private static final int SOCKET_TEX_W = 22;
    private static final int SOCKET_TEX_H = 17;
    private static final int SOCKET_W = SOCKET_TEX_W * 2; // 44
    private static final int SOCKET_H = SOCKET_TEX_H * 2; // 34
    // Item inside socket: 10x10 in BG texture space → 20x20 in game space
    private static final int SOCKET_ITEM_TEX = 10;
    private static final int SOCKET_ITEM_SIZE = SOCKET_ITEM_TEX * 2; // 20

    private PlayerDiskData diskData;
    private List<String> sortedDiskIds   = new ArrayList<>();
    private List<String> filteredDiskIds = new ArrayList<>();

    private int scrollOffset     = 0;
    private int maxScroll        = 0;
    private int descScrollOffset = 0;
    private int descMaxScroll    = 0;

    private String  heldDiskId            = null;
    private String  hoveredDiskId         = null;
    private boolean hoveringUpgradeButton = false;
    private boolean searchFocused         = false;
    private String  searchText            = "";

    private int animationTick = 0;
    private int leftPos, topPos;

    private List<String> descLines = new ArrayList<>();

    public DiskMenuScreen() {
        super(Component.translatable("gui.upgrading.disk_menu.title"));
    }

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

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        String previousHoveredDisk = hoveredDiskId;
        hoveredDiskId = null;
        hoveringUpgradeButton = false;

        RenderSystem.enableBlend();
        graphics.blit(BACKGROUND_TEXTURE,
                leftPos, topPos, SCREEN_WIDTH, SCREEN_HEIGHT,
                0, 0, BG_TEX_W, BG_TEX_H, BG_TEX_W, BG_TEX_H);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

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

        renderSearchBar(graphics, mouseX, mouseY);
        renderDiskList(graphics, mouseX, mouseY);
        renderZSlotSockets(graphics, mouseX, mouseY);
        renderEquipmentSlots(graphics, mouseX, mouseY);

        if (hoveredDiskId == null && previousHoveredDisk != null) {
            if (mouseX >= leftPos + 104 && mouseX <= leftPos + SCREEN_WIDTH - 4 &&
                    mouseY >= topPos + SLOT_Y && mouseY <= topPos + SCREEN_HEIGHT - 4) {
                hoveredDiskId = previousHoveredDisk;
            }
        }

        renderInfoPanel(graphics, mouseX, mouseY);

        if (heldDiskId != null) {
            renderDiskAt(graphics, heldDiskId, mouseX - DISK_SIZE / 2, mouseY - DISK_SIZE / 2, DISK_SIZE);
        }
    }

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

    private void renderEquipmentSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        int[] slotXs = {leftPos + SLOT_1_X, leftPos + SLOT_2_X, leftPos + SLOT_3_X};
        int slotY = topPos + SLOT_Y;
        int innerPad = 2;

        for (int slot = 0; slot < 3; slot++) {
            int slotX = slotXs[slot];
            int layerX = slotX;
            int layerY = slotY;
            int layerSize = SLOT_SIZE;

            // Always render Z-Slot layers (even if no disk equipped)
            ItemStack zSlot = diskData.getZSlot(slot);
            if (!zSlot.isEmpty()) {
                String frame = ZSlotItem.getFrame(zSlot);
                String board = ZSlotItem.getBoard(zSlot);
                String chip  = ZSlotItem.getChip(zSlot);

                RenderSystem.enableBlend();
                int layerRenderSize = 64;
                int layerOffset = (SLOT_SIZE - layerRenderSize) / 2;
                graphics.blit(getZSlotLayerTexture("board", board),
                        slotX + layerOffset, slotY + layerOffset,
                        layerRenderSize, layerRenderSize,
                        0, 0, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE);
                graphics.blit(getZSlotLayerTexture("chip", chip),
                        slotX + layerOffset, slotY + layerOffset,
                        layerRenderSize, layerRenderSize,
                        0, 0, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE);
                graphics.blit(getZSlotLayerTexture("frame", frame),
                        slotX + layerOffset, slotY + layerOffset,
                        layerRenderSize, layerRenderSize,
                        0, 0, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }

            // Render disk ON TOP (only if equipped)
            String equippedDiskId = diskData.getEquippedDisk(slot);
            if (equippedDiskId != null && !equippedDiskId.equals(heldDiskId)) {
                renderDiskAt(graphics, equippedDiskId, slotX + innerPad, slotY + innerPad, SLOT_SIZE - innerPad * 2);
                if (isMouseOver(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                    hoveredDiskId = equippedDiskId;
                    graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x4066FF66);
                }
            } else if (isMouseOver(mouseX, mouseY, slotX, slotY, SLOT_SIZE, SLOT_SIZE)) {
                graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x4066FF66);
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

    private void renderZSlotSockets(GuiGraphics graphics, int mouseX, int mouseY) {
        int[] slotXs = {leftPos + SLOT_1_X, leftPos + SLOT_2_X, leftPos + SLOT_3_X};
        int slotY = topPos + SLOT_Y;

        for (int slot = 0; slot < 3; slot++) {
            int slotX = slotXs[slot];
            int socketX = slotX + (SLOT_SIZE - SOCKET_W) / 2;
            int socketY = slotY - SOCKET_H;

            ItemStack zSlot = diskData.getZSlot(slot);
            boolean hasZSlot = !zSlot.isEmpty();
            boolean isMythic = hasZSlot && ZSlotItem.isMythic(zSlot);

            ResourceLocation socketTex = isMythic ? SOCKET_MYTHIC_TEXTURE
                    : hasZSlot ? SOCKET_ON_TEXTURE
                    : SOCKET_OFF_TEXTURE;

            RenderSystem.enableBlend();

            graphics.blit(socketTex, socketX, socketY, SOCKET_W, SOCKET_H,
                    0, 0, SOCKET_TEX_W, SOCKET_TEX_H, SOCKET_TEX_W, SOCKET_TEX_H);
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            // Render Z-Slot item layers inside socket at 2x scale
            if (hasZSlot) {
                String frame = ZSlotItem.getFrame(zSlot);
                String board = ZSlotItem.getBoard(zSlot);
                String chip  = ZSlotItem.getChip(zSlot);

                int itemX = socketX + (SOCKET_W - SOCKET_ITEM_SIZE) / 2;
                int itemY = socketY + (SOCKET_H - SOCKET_ITEM_SIZE) / 2;

                RenderSystem.enableBlend();
                graphics.blit(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID,
                        "textures/item/board_" + board + ".png"),
                        itemX, itemY, SOCKET_ITEM_SIZE, SOCKET_ITEM_SIZE,
                        0, 0, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE);
                graphics.blit(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID,
                                "textures/item/chip_" + chip + "_zslot.png"),
                        itemX, itemY, SOCKET_ITEM_SIZE, SOCKET_ITEM_SIZE,
                        0, 0, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE);
                graphics.blit(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID,
                                "textures/item/frame_" + frame + ".png"),
                        itemX, itemY, SOCKET_ITEM_SIZE, SOCKET_ITEM_SIZE,
                        0, 0, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE, LAYER_TEX_SIZE);
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }

            if (isMouseOver(mouseX, mouseY, socketX, socketY, SOCKET_W, SOCKET_H)) {
                int intX = socketX + 6 * 2;
                int intY = socketY + 4 * 2;
                int intSize = 10 * 2;
                graphics.fill(intX, intY, intX + intSize, intY + intSize, 0x60FFFFFF);
            }
        }
    }

    private void renderInfoPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        String diskId = hoveredDiskId;
        if (diskId == null) return;

        UpgradeDisk disk = DiskRegistry.getDisk(diskId);
        if (disk == null) return;

        int x = leftPos + INFO_X;
        int y = topPos  + INFO_Y;
        int level       = diskData.getDiskLevel(diskId);
        int rarityColor = disk.getRarity().getColor();

        String nameText = disk.getDisplayName();
        graphics.drawString(this.font,
                Component.literal(nameText)
                        .withStyle(s -> s.withBold(true).withUnderlined(true).withColor(rarityColor)),
                x, y, rarityColor, false);

        String levelText = "Lv." + level;
        int levelX = leftPos + INFO_X + INFO_W - this.font.width(levelText) - 18;
        graphics.drawString(this.font, levelText, levelX, y, 0xFFFF55, false);

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

            RenderSystem.enableBlend();
            graphics.blit(XP_ICON_TEXTURE,
                    leftPos + XP_ICON_X, topPos + XP_ICON_Y,
                    XP_ICON_W, XP_ICON_H,
                    0, 0, 4, 4, 4, 4);
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            int costColor = canAfford ? 0x55FF55 : 0xFF5555;
            graphics.drawString(this.font, xpCost + " XP",
                    leftPos + XP_ICON_X + XP_ICON_W + 3, topPos + XP_ICON_Y + 1, costColor, false);

            if (level == 11) {
                int fragmentCost = UpgradeDiskPacket.getFragmentCost(disk.getRarity());
                int playerFragments = countPlayerFragments();
                boolean hasFragments = playerFragments >= fragmentCost;

                int fragIconX = leftPos + XP_ICON_X + XP_ICON_W + 3 + this.font.width(xpCost + " XP") + 6;
                int fragIconY = topPos + XP_ICON_Y;

                RenderSystem.enableBlend();
                graphics.blit(FRAGMENT_ICON_TEXTURE, fragIconX, fragIconY,
                        XP_ICON_W, XP_ICON_H, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

                int fragColor = hasFragments ? 0x55FF55 : 0xFF5555;
                graphics.drawString(this.font, fragmentCost + " Fragments",
                        fragIconX + XP_ICON_W + 3, fragIconY + 1, fragColor, false);
            }
        }
    }

    private int countPlayerFragments() {
        if (minecraft.player == null) return 0;
        int count = 0;
        for (ItemStack stack : minecraft.player.getInventory().items) {
            if (stack.is(com.nedraw.upgrading.item.ModItems.ENCRYPTED_FRAGMENT.get())) {
                count += stack.getCount();
            }
        }
        return count;
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

    private void renderNineSlicedButton(GuiGraphics graphics, int x, int y, int w, int h, int vOffset) {
        int c = BTN_CORNER;
        int tw = BTN_TEX_W;
        int th = 27;
        int iw = w - c * 2;
        int ih = h - c * 2;

        graphics.blit(BUTTON_TEXTURE, x,     y,     c,  c,  0,    vOffset,             c, c, tw, th);
        graphics.blit(BUTTON_TEXTURE, x+w-c, y,     c,  c,  tw-c, vOffset,             c, c, tw, th);
        graphics.blit(BUTTON_TEXTURE, x,     y+h-c, c,  c,  0,    vOffset+BTN_TEX_W-c, c, c, tw, th);
        graphics.blit(BUTTON_TEXTURE, x+w-c, y+h-c, c,  c,  tw-c, vOffset+BTN_TEX_W-c, c, c, tw, th);
        graphics.blit(BUTTON_TEXTURE, x+c,   y,     iw, c,  c,    vOffset,             1, c, tw, th);
        graphics.blit(BUTTON_TEXTURE, x+c,   y+h-c, iw, c,  c,    vOffset+BTN_TEX_W-c, 1, c, tw, th);
        graphics.blit(BUTTON_TEXTURE, x,     y+c,   c,  ih, 0,    vOffset+c,           c, 1, tw, th);
        graphics.blit(BUTTON_TEXTURE, x+w-c, y+c,   c,  ih, tw-c, vOffset+c,           c, 1, tw, th);
        graphics.blit(BUTTON_TEXTURE, x+c,   y+c,   iw, ih, c,    vOffset+c,           1, 1, tw, th);
    }

    private void renderDiskAt(GuiGraphics graphics, String diskId, int x, int y, int size) {
        UpgradeDisk disk = DiskRegistry.getDisk(diskId);
        if (disk == null) return;

        if (disk.isAnimated()) {
            renderAnimatedDiskAt(graphics, disk, x, y, size);
        } else {
            renderStaticDiskAt(graphics, diskId, x, y, size);
        }

        if (diskData.getDiskLevel(diskId) >= 12) {
            renderPlatinumOverlay(graphics, disk, x, y, size);
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
        int frameSize  = disk.getFrameSize();
        int frameCount = disk.getFrameCount();
        int currentFrame = (animationTick / disk.getTicksPerFrame()) % frameCount;
        int vOffset = currentFrame * frameSize;

        RenderSystem.enableBlend();
        graphics.blit(texture, x, y, size, size, 0, vOffset,
                frameSize, frameSize, frameSize, frameCount * frameSize);
        RenderSystem.disableBlend();
    }

    private void renderPlatinumOverlay(GuiGraphics graphics, UpgradeDisk disk, int x, int y, int size) {
        ResourceLocation overlayTexture = getPlatinumOverlay(disk);
        RenderSystem.enableBlend();

        if (disk.isAnimated()) {
            int frameSize  = disk.getFrameSize();
            int frameCount = disk.getFrameCount();
            int currentFrame = (animationTick / disk.getTicksPerFrame()) % frameCount;
            int vOffset = currentFrame * frameSize;
            graphics.blit(overlayTexture, x, y, size, size, 0, vOffset,
                    frameSize, frameSize, frameSize, frameCount * frameSize);
        } else {
            graphics.blit(overlayTexture, x, y, 0, 0, size, size, size, size);
        }

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int mx = (int) mouseX, my = (int) mouseY;

        searchFocused = isMouseOver(mx, my, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_W, SEARCH_H);

        // Socket clicks
        int[] socketSlotXs = {leftPos + SLOT_1_X, leftPos + SLOT_2_X, leftPos + SLOT_3_X};
        for (int slot = 0; slot < 3; slot++) {
            int slotX = socketSlotXs[slot];
            int socketX = slotX + (SLOT_SIZE - SOCKET_W) / 2;
            int socketY = topPos + SLOT_Y - SOCKET_H - 1;

            if (isMouseOver(mx, my, socketX, socketY, SOCKET_W, SOCKET_H)) {
                ItemStack held = minecraft.player.getMainHandItem();
                ItemStack zSlotInSocket = diskData.getZSlot(slot);

                if (!held.isEmpty() && held.getItem() instanceof com.nedraw.upgrading.item.ZSlotItem) {
                    PacketDistributor.sendToServer(new EquipZSlotPacket(slot, true));
                } else if (!zSlotInSocket.isEmpty()) {
                    PacketDistributor.sendToServer(new EquipZSlotPacket(slot, false));
                }
                return true;
            }
        }

        if (hoveringUpgradeButton && hoveredDiskId != null) {
            int currentLevel = diskData.getDiskLevel(hoveredDiskId);
            UpgradeDisk disk = DiskRegistry.getDisk(hoveredDiskId);
            if (disk != null && disk.canUpgrade(currentLevel)) {
                int xpCost   = disk.getRarity().getXpCostForLevel(currentLevel);
                int playerXP = diskData.getTotalXP(minecraft.player);
                if (playerXP >= xpCost) {
                    PacketDistributor.sendToServer(new UpgradeDiskPacket(hoveredDiskId));
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
            if (keyCode == 259) {
                if (!searchText.isEmpty()) {
                    searchText = searchText.substring(0, searchText.length() - 1);
                    applySearch();
                }
                return true;
            }
            if (keyCode == 256) { searchFocused = false; return true; }
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