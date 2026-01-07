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
                        // Encrypted Disk
                        output.accept(ModItems.ENCRYPTED_FLOPPY.get());

                        // BASIC disks (alphabetical)
                        output.accept(ModItems.MAGNET_DISK.get());
                        output.accept(ModItems.MIGHTY_MINER_DISK.get());
                        output.accept(ModItems.SEA_FISH_DISK.get());
                        output.accept(ModItems.SWIFT_FEET_DISK.get());

                        // RARE disks (alphabetical)
                        output.accept(ModItems.FLAME_WALKER_DISK.get());
                        output.accept(ModItems.HARVESTER_DISK.get());
                        output.accept(ModItems.STEP_ASSIST_DISK.get());

                        // EPIC disks (alphabetical)
                        output.accept(ModItems.GLUTTON_DISK.get());
                        output.accept(ModItems.SOAPY_HANDS_DISK.get());

                        // LEGENDARY disks (alphabetical)
                        output.accept(ModItems.BERSERKER_DISK.get());
                        output.accept(ModItems.PYROCLASM_DISK.get());
                    })
                    .build()
    );
}