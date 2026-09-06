package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.BrewmasterDisk;
import com.nedraw.upgrading.disk.DiskRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.*;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class BrewmasterHandler {

    private static final Random RANDOM = new Random();
    private static final Map<BlockPos, UUID> BREWING_STAND_USERS = new HashMap<>();
    private static final Map<Integer, List<MobEffectInstance>> SAVED_EFFECTS = new HashMap<>();

    @SubscribeEvent
    public static void onBrewingStandInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        BlockPos pos = event.getPos();
        if (event.getLevel().getBlockState(pos).getBlock() instanceof BrewingStandBlock) {
            BREWING_STAND_USERS.put(pos, player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPreBrew(PotionBrewEvent.Pre event) {
        SAVED_EFFECTS.clear();
        for (int i = 0; i < event.getLength(); i++) {
            ItemStack stack = event.getItem(i);
            if (stack.isEmpty()) continue;
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null || contents.customEffects().isEmpty()) continue;
            SAVED_EFFECTS.put(i, new ArrayList<>(contents.customEffects()));
        }
    }

    @SubscribeEvent
    public static void onBrew(PotionBrewEvent.Post event) {

        for (Map.Entry<Integer, List<MobEffectInstance>> entry : SAVED_EFFECTS.entrySet()) {
            int i = entry.getKey();
            ItemStack stack = event.getItem(i);
            if (stack.isEmpty()) continue;
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null) continue;

            List<MobEffectInstance> merged = new ArrayList<>(entry.getValue());
            contents.customEffects().forEach(e -> {
                boolean alreadyPresent = merged.stream().anyMatch(ex ->
                        BuiltInRegistries.MOB_EFFECT.getKey(ex.getEffect().value())
                                .equals(BuiltInRegistries.MOB_EFFECT.getKey(e.getEffect().value())));
                if (!alreadyPresent) merged.add(e);
            });

            ItemStack restored = stack.copy();
            restored.set(DataComponents.POTION_CONTENTS, new PotionContents(
                    contents.potion(), Optional.empty(), merged));
            event.setItem(i, restored);
        }
        SAVED_EFFECTS.clear();

        net.minecraft.server.MinecraftServer server =
                net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        ServerPlayer brewingPlayer = null;
        PlayerDiskData brewingDiskData = null;
        int brewingSlot = -1;
        BlockPos usedPos = null;

        for (Map.Entry<BlockPos, UUID> entry : BREWING_STAND_USERS.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getValue());
            if (player == null) continue;

            PlayerDiskData diskData = PlayerDiskData.get(player);
            for (int slot = 0; slot < 3; slot++) {
                String diskId = diskData.getEquippedDisk(slot);
                if ("brewmaster".equals(diskId)) {
                    brewingPlayer = player;
                    brewingDiskData = diskData;
                    brewingSlot = slot;
                    usedPos = entry.getKey();
                    break;
                }
            }
            if (brewingPlayer != null) break;
        }

        if (usedPos != null) BREWING_STAND_USERS.remove(usedPos);
        if (brewingPlayer == null) return;

        var disk = DiskRegistry.getDisk("brewmaster");
        if (!(disk instanceof BrewmasterDisk brewmaster)) return;

        int level = brewingDiskData.getDiskLevel("brewmaster");
        float efficiency = ZSlotEffects.getEfficiencyMultiplier(brewingPlayer, brewingSlot);
        float chance = brewmaster.getChance(level, efficiency);
        int maxExtra = brewmaster.getMaxExtraEffects(level);

        for (int i = 0; i < event.getLength(); i++) {
            ItemStack stack = event.getItem(i);
            if (stack.isEmpty()) continue;
            if (!stack.has(DataComponents.POTION_CONTENTS)) continue;
            ItemStack result = applyBrewmasterEffect(stack, chance, maxExtra);
            if (result != stack) event.setItem(i, result);
        }
    }

    private static ItemStack applyBrewmasterEffect(ItemStack potion, float chance, int maxExtra) {
        PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return potion;

        boolean isPositive = isPositivePotion(contents);

        List<MobEffectInstance> customEffects = new ArrayList<>(contents.customEffects());
        List<MobEffectInstance> allEffects = new ArrayList<>();
        contents.getAllEffects().forEach(allEffects::add);

        int slotsAvailable = 4 - allEffects.size();
        if (slotsAvailable <= 0) return potion;

        int toAdd = Math.min(maxExtra, slotsAvailable);

        int baseDuration = allEffects.isEmpty() ? 3600
                : allEffects.stream().mapToInt(MobEffectInstance::getDuration).max().orElse(3600);

        int added = 0;
        for (int i = 0; i < toAdd; i++) {
            if (RANDOM.nextFloat() >= chance) break;

            List<MobEffect> pool = BuiltInRegistries.MOB_EFFECT.stream()
                    .filter(e -> e.isBeneficial() == isPositive)
                    .collect(Collectors.toList());

            if (pool.isEmpty()) break;
            MobEffect effect = pool.get(RANDOM.nextInt(pool.size()));

            Optional<MobEffectInstance> existingAll = allEffects.stream()
                    .filter(ex -> BuiltInRegistries.MOB_EFFECT.getKey(ex.getEffect().value())
                            .equals(BuiltInRegistries.MOB_EFFECT.getKey(effect)))
                    .findFirst();

            if (existingAll.isPresent()) {

                Optional<MobEffectInstance> existingCustom = customEffects.stream()
                        .filter(ex -> BuiltInRegistries.MOB_EFFECT.getKey(ex.getEffect().value())
                                .equals(BuiltInRegistries.MOB_EFFECT.getKey(effect)))
                        .findFirst();

                if (existingCustom.isPresent()) {
                    int idx = customEffects.indexOf(existingCustom.get());
                    MobEffectInstance old = customEffects.get(idx);
                    if (old.getAmplifier() < 4) {
                        customEffects.set(idx, new MobEffectInstance(
                                old.getEffect(), old.getDuration(),
                                old.getAmplifier() + 1,
                                old.isAmbient(), old.isVisible(), old.showIcon()));
                        added++;
                    }
                } else {

                    MobEffectInstance upgraded = new MobEffectInstance(
                            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect),
                            baseDuration, 1);
                    customEffects.add(upgraded);
                    allEffects.add(upgraded);
                    added++;
                }
            } else {
                MobEffectInstance newEffect = new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect),
                        baseDuration, 0);
                customEffects.add(newEffect);
                allEffects.add(newEffect);
                added++;
            }
        }

        if (added == 0) return potion;

        int splitDuration = baseDuration / allEffects.size();

        List<MobEffectInstance> splitCustomEffects = customEffects.stream()
                .map(e -> new MobEffectInstance(e.getEffect(), splitDuration, e.getAmplifier(),
                        e.isAmbient(), e.isVisible(), e.showIcon()))
                .collect(Collectors.toList());

        ItemStack result = potion.copy();
        result.set(DataComponents.POTION_CONTENTS, new PotionContents(
                contents.potion(),
                Optional.empty(),
                splitCustomEffects));
        return result;
    }

    @SubscribeEvent
    public static void onDrinkPotion(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getItem().has(DataComponents.POTION_CONTENTS)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);
        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (!"brewmaster".equals(diskId)) continue;

            var disk = DiskRegistry.getDisk(diskId);
            if (!(disk instanceof BrewmasterDisk brewmaster)) continue;

            int level = diskData.getDiskLevel(diskId);
            float efficiency = ZSlotEffects.getEfficiencyMultiplier(player, slot);
            float multiplier = brewmaster.getDurationMultiplier(level, efficiency);

            if (multiplier > 1.0f) {
                for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {
                    int newDuration = (int)(effect.getDuration() * multiplier);
                    player.addEffect(new MobEffectInstance(
                            effect.getEffect(), newDuration,
                            effect.getAmplifier(), effect.isAmbient(),
                            effect.isVisible(), effect.showIcon()));
                }
            }

            if (player.getActiveEffects().size() >= 4) {
                ModAdvancementTriggers.POTION_HOLIC(player);
            }
            return;
        }
    }

    private static boolean isPositivePotion(PotionContents contents) {
        List<MobEffectInstance> effects = new ArrayList<>();
        contents.getAllEffects().forEach(effects::add);
        if (effects.isEmpty()) return true;
        return effects.stream().allMatch(e -> e.getEffect().value().isBeneficial());
    }
}