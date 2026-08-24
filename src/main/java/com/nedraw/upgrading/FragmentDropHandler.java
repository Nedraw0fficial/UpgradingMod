package com.nedraw.upgrading;

import com.nedraw.upgrading.item.ModItems;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Random;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class FragmentDropHandler {

    private static final Random RANDOM = new Random();
    private static final float DROP_CHANCE = 0.05f;

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (mob.level().isClientSide) return;

        if (!(event.getSource().getEntity() instanceof Player)) return;

        MobCategory category = mob.getType().getCategory();

        if (category != MobCategory.MONSTER) return;

        if (RANDOM.nextFloat() < DROP_CHANCE) {
            ItemStack fragment = new ItemStack(ModItems.ENCRYPTED_FRAGMENT.get());
            ItemEntity itemEntity = new ItemEntity(
                    mob.level(),
                    mob.getX(), mob.getY(), mob.getZ(),
                    fragment
            );
            mob.level().addFreshEntity(itemEntity);
        }
    }
}