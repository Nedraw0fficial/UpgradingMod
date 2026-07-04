package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.BeastWhispererDisk;
import com.nedraw.upgrading.disk.DiskRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;

import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class BeastWhispererHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onAnimalBreed(BabyEntitySpawnEvent event) {
        Player player = event.getCausedByPlayer();
        if (player == null) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("beast_whisperer")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof BeastWhispererDisk beastDisk) {
                    int level = diskData.getDiskLevel(diskId);

                    if (player.level() instanceof ServerLevel serverLevel) {
                        Animal parentA = (Animal) event.getParentA();
                        Animal parentB = (Animal) event.getParentB();

                        serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                                serverLevel.getServer().getTickCount() + 1,
                                () -> {
                                    if (parentA != null) {
                                        int reducedCooldown = beastDisk.getReducedBreedingCooldown(6000, level);
                                        parentA.setAge(reducedCooldown);
                                    }
                                    if (parentB != null) {
                                        int reducedCooldown = beastDisk.getReducedBreedingCooldown(6000, level);
                                        parentB.setAge(reducedCooldown);
                                    }
                                }
                        ));

                        if (level >= 12 && event.getChild() != null && parentA != null) {
                            spawnRecursiveTwins(event.getChild(), serverLevel, parentA, 0, player);
                        }
                    }
                }
                return;
            }
        }
    }

    private static void spawnRecursiveTwins(AgeableMob originalBaby, ServerLevel level,
                                            Animal parentReference, int depth, Player player) {
        if (depth > 10) return;

        if (RANDOM.nextFloat() < 0.12f) {
            double x = parentReference.getX();
            double y = parentReference.getY();
            double z = parentReference.getZ();

            CompoundTag babyData = new CompoundTag();
            originalBaby.saveWithoutId(babyData);
            babyData.remove("UUID");

            Entity twin = originalBaby.getType().create(level);

            if (twin != null) {
                twin.load(babyData);

                if (twin instanceof AgeableMob ageableTwin) {
                    ageableTwin.setAge(-24000);
                }

                twin.setUUID(UUID.randomUUID());
                twin.moveTo(x, y, z, 0, 0);

                boolean added = level.addFreshEntity(twin);

                if (added) {
                    // Fire advancement when a twin is successfully born
                    if (player instanceof ServerPlayer sp) {
                        ModAdvancementTriggers.TWIN_BORN(sp);
                    }

                    if (twin instanceof AgeableMob ageableTwin) {
                        spawnRecursiveTwins(ageableTwin, level, parentReference, depth + 1, player);
                    }
                }
            }
        }
    }
}