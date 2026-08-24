package com.nedraw.upgrading;

import com.nedraw.upgrading.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UpgradingMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> UPGRADING_TAB = CREATIVE_MODE_TABS.register(
            "upgrading_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.upgrading"))
                    .icon(() -> new ItemStack(ModItems.BERSERKER_DISK.get()))
                    .displayItems((parameters, output) -> {
                        // Encrypted items
                        output.accept(ModItems.ENCRYPTED_FLOPPY.get());
                        output.accept(ModItems.ENCRYPTED_FRAGMENT.get());

                        // BASIC disks
                        output.accept(ModItems.EFFICIENT_DISK.get());
                        output.accept(ModItems.FEATHER_FALL_DISK.get());
                        output.accept(ModItems.LIGHTWEIGHT_DISK.get());
                        output.accept(ModItems.MAGNET_DISK.get());
                        output.accept(ModItems.MIGHTY_MINER_DISK.get());
                        output.accept(ModItems.NIGHT_VISION_DISK.get());
                        output.accept(ModItems.SEA_FISH_DISK.get());
                        output.accept(ModItems.SWIFT_FEET_DISK.get());
                        output.accept(ModItems.TANKY_DISK.get());

                        // RARE disks
                        output.accept(ModItems.BEAST_WHISPERER_DISK.get());
                        output.accept(ModItems.FLAME_WALKER_DISK.get());
                        output.accept(ModItems.HARVESTER_DISK.get());
                        output.accept(ModItems.IRON_GRIP_DISK.get());
                        output.accept(ModItems.PAWNBROKER_DISK.get());
                        output.accept(ModItems.STEP_ASSIST_DISK.get());
                        output.accept(ModItems.TREASURE_SENSE_DISK.get());

                        // EPIC disks
                        output.accept(ModItems.BASHER_DISK.get());
                        output.accept(ModItems.GLUTTON_DISK.get());
                        output.accept(ModItems.MOUNTAIN_GOAT_DISK.get());
                        output.accept(ModItems.SOAPY_HANDS_DISK.get());
                        output.accept(ModItems.WARCHEMIST_DISK.get());

                        // LEGENDARY disks
                        output.accept(ModItems.ARCHITECTS_MIND_DISK.get());
                        output.accept(ModItems.BERSERKER_DISK.get());
                        output.accept(ModItems.PYROCLASM_DISK.get());

                        // MYTHIC disks
                        output.accept(ModItems.NECRO_ARCHER_DISK.get());

                        // =====================
                        // Z-SLOT COMPONENTS
                        // =====================

                        // Frames (flat progression)
                        output.accept(ModItems.FRAME_FABRIC.get());
                        output.accept(ModItems.FRAME_WOODEN.get());
                        output.accept(ModItems.FRAME_COPPER.get());
                        output.accept(ModItems.FRAME_IRON.get());
                        output.accept(ModItems.FRAME_GOLDEN.get());
                        output.accept(ModItems.FRAME_AMETHYST.get());

                        // Frames (special)
                        output.accept(ModItems.FRAME_CACTUS.get());
                        output.accept(ModItems.FRAME_GLASS.get());
                        output.accept(ModItems.FRAME_ROSE_GOLD.get());
                        output.accept(ModItems.FRAME_SPONGE.get());
                        output.accept(ModItems.FRAME_MUSHROOM.get());
                        output.accept(ModItems.FRAME_VOID.get());

                        // Boards
                        output.accept(ModItems.BOARD_BASIC.get());
                        output.accept(ModItems.BOARD_ENDER.get());
                        output.accept(ModItems.BOARD_ENCHANTED.get());
                        output.accept(ModItems.BOARD_PISTON.get());
                        output.accept(ModItems.BOARD_WOOL.get());
                        output.accept(ModItems.BOARD_CORRUPTED.get());

                        // Chips
                        output.accept(ModItems.CHIP_BASIC.get());
                        output.accept(ModItems.CHIP_HEART.get());
                        output.accept(ModItems.CHIP_DIAMOND.get());
                        output.accept(ModItems.CHIP_SPADE.get());
                        output.accept(ModItems.CHIP_CLUB.get());
                        output.accept(ModItems.CHIP_FOOD.get());
                        output.accept(ModItems.CHIP_PORTAL.get());
                        output.accept(ModItems.CHIP_DARK.get());
                    })
                    .build()
    );
}