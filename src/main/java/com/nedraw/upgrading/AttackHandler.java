package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.SoapyHandsDisk;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Random;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class AttackHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onEntityAttacked(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (player.level().isClientSide) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if ("soapy_hands".equals(diskId)) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof SoapyHandsDisk soapyHands) {
                    int level = diskData.getDiskLevel(diskId);

                    if (level < 12) {
                        float dropChance = soapyHands.getDropChance(level);
                        if (RANDOM.nextFloat() < dropChance) {
                            ItemStack mainHandItem = target.getMainHandItem();
                            if (!mainHandItem.isEmpty()) {
                                target.level().addFreshEntity(new ItemEntity(
                                        target.level(), target.getX(), target.getY(), target.getZ(), mainHandItem));
                                target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                                playPickupSound(player, target);
                            }
                        }
                    } else {
                        // Level 12: track how many slots are disarmed
                        int slotsDisarmed = 0;

                        // Main hand (15%)
                        if (RANDOM.nextFloat() < 0.15f) {
                            ItemStack item = target.getMainHandItem();
                            if (!item.isEmpty()) {
                                target.level().addFreshEntity(new ItemEntity(
                                        target.level(), target.getX(), target.getY(), target.getZ(), item));
                                target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                                playPickupSound(player, target);
                                slotsDisarmed++;
                            }
                        }

                        // Off-hand (8%)
                        if (RANDOM.nextFloat() < 0.08f) {
                            ItemStack item = target.getOffhandItem();
                            if (!item.isEmpty()) {
                                target.level().addFreshEntity(new ItemEntity(
                                        target.level(), target.getX(), target.getY(), target.getZ(), item));
                                target.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                                playPickupSound(player, target);
                                slotsDisarmed++;
                            }
                        }

                        // Armor slots (8% each)
                        for (EquipmentSlot armorSlot : new EquipmentSlot[]{
                                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                                EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                            if (RANDOM.nextFloat() < 0.08f) {
                                ItemStack item = target.getItemBySlot(armorSlot);
                                if (!item.isEmpty()) {
                                    target.level().addFreshEntity(new ItemEntity(
                                            target.level(), target.getX(), target.getY(), target.getZ(), item));
                                    target.setItemSlot(armorSlot, ItemStack.EMPTY);
                                    playPickupSound(player, target);
                                    slotsDisarmed++;
                                }
                            }
                        }

                        // Inventory slots (8% each, mobs with containers)
                        if (target instanceof net.minecraft.world.entity.Mob mob &&
                                mob instanceof net.minecraft.world.Container container) {
                            for (int i = 0; i < container.getContainerSize(); i++) {
                                if (RANDOM.nextFloat() < 0.08f) {
                                    ItemStack item = container.getItem(i);
                                    if (!item.isEmpty()) {
                                        target.level().addFreshEntity(new ItemEntity(
                                                target.level(), target.getX(), target.getY(), target.getZ(), item));
                                        container.setItem(i, ItemStack.EMPTY);
                                        playPickupSound(player, target);
                                        slotsDisarmed++;
                                    }
                                }
                            }
                        }

                        // Fire advancement if 3+ slots disarmed in one hit (main + offhand + at least 1 armor)
                        if (slotsDisarmed >= 3 && player instanceof ServerPlayer sp) {
                            ModAdvancementTriggers.FULL_DISARM(sp);
                        }
                    }
                }
                break;
            }
        }
    }

    private static void playPickupSound(Player player, LivingEntity target) {
        player.level().playSound(null, target.blockPosition(),
                net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.3f, 1.5f);
    }
}