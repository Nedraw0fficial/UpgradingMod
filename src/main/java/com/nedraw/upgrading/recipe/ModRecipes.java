package com.nedraw.upgrading.recipe;

import com.nedraw.upgrading.UpgradingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, UpgradingMod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ZSlotAssemblyRecipe>> Z_SLOT_ASSEMBLY =
            RECIPE_SERIALIZERS.register("z_slot_assembly",
                    () -> ZSlotAssemblyRecipe.SERIALIZER);
}
