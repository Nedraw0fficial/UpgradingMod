package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.SoapyHandsDisk;
import com.nedraw.upgrading.disk.UpgradeDisk;
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
                        // Levels 7-11: Only main hand
                        float dropChance = soapyHands.getDropChance(level);

                        if (RANDOM.nextFloat() < dropChance) {
                            ItemStack mainHandItem = target.getMainHandItem();
                            if (!mainHandItem.isEmpty()) {
                                // Drop item manually
                                ItemEntity itemEntity = new ItemEntity(
                                        target.level(),
                                        target.getX(),
                                        target.getY(),
                                        target.getZ(),
                                        mainHandItem // Don't copy!
                                );
                                target.level().addFreshEntity(itemEntity);

                                target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

                                player.level().playSound(
                                        null, target.blockPosition(),
                                        net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                        net.minecraft.sounds.SoundSource.PLAYERS,
                                        0.5f, 1.5f
                                );
                            }
                        }
                    } else {
                        // Level 12: EVERY slot!

                        // Main hand (15%)
                        if (RANDOM.nextFloat() < 0.15f) {
                            ItemStack mainHandItem = target.getMainHandItem();
                            if (!mainHandItem.isEmpty()) {
                                ItemEntity itemEntity = new ItemEntity(
                                        target.level(),
                                        target.getX(),
                                        target.getY(),
                                        target.getZ(),
                                        mainHandItem // Don't copy - keep original data!
                                );
                                target.level().addFreshEntity(itemEntity);
                                target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);

                                player.level().playSound(
                                        null, target.blockPosition(),
                                        net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                        net.minecraft.sounds.SoundSource.PLAYERS,
                                        0.3f, 1.5f
                                );
                            }
                        }

                        // Off-hand (8%)
                        if (RANDOM.nextFloat() < 0.08f) {
                            ItemStack offHandItem = target.getOffhandItem();
                            if (!offHandItem.isEmpty()) {
                                ItemEntity itemEntity = new ItemEntity(
                                        target.level(),
                                        target.getX(),
                                        target.getY(),
                                        target.getZ(),
                                        offHandItem
                                );
                                target.level().addFreshEntity(itemEntity);
                                target.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);

                                player.level().playSound(
                                        null, target.blockPosition(),
                                        net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                        net.minecraft.sounds.SoundSource.PLAYERS,
                                        0.3f, 1.5f
                                );
                            }
                        }

                        // Armor slots (8% each)
                        EquipmentSlot[] armorSlots = {
                                EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                                EquipmentSlot.LEGS, EquipmentSlot.FEET
                        };

                        for (EquipmentSlot armorSlot : armorSlots) {
                            if (RANDOM.nextFloat() < 0.08f) {
                                ItemStack armorItem = target.getItemBySlot(armorSlot);
                                if (!armorItem.isEmpty()) {
                                    ItemEntity itemEntity = new ItemEntity(
                                            target.level(),
                                            target.getX(),
                                            target.getY(),
                                            target.getZ(),
                                            armorItem
                                    );
                                    target.level().addFreshEntity(itemEntity);
                                    target.setItemSlot(armorSlot, ItemStack.EMPTY);

                                    player.level().playSound(
                                            null, target.blockPosition(),
                                            net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                            net.minecraft.sounds.SoundSource.PLAYERS,
                                            0.3f, 1.5f
                                    );
                                }
                            }
                        }

                        // EVERY inventory slot (8% each)
                        if (target instanceof net.minecraft.world.entity.Mob mob) {
                            if (mob instanceof net.minecraft.world.Container container) {
                                for (int i = 0; i < container.getContainerSize(); i++) {
                                    if (RANDOM.nextFloat() < 0.08f) {
                                        ItemStack invItem = container.getItem(i);
                                        if (!invItem.isEmpty()) {
                                            ItemEntity itemEntity = new ItemEntity(
                                                    target.level(),
                                                    target.getX(),
                                                    target.getY(),
                                                    target.getZ(),
                                                    invItem
                                            );
                                            target.level().addFreshEntity(itemEntity);
                                            container.setItem(i, ItemStack.EMPTY);

                                            player.level().playSound(
                                                    null, target.blockPosition(),
                                                    net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                                                    net.minecraft.sounds.SoundSource.PLAYERS,
                                                    0.3f, 1.5f
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }
}