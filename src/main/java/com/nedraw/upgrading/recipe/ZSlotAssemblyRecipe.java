package com.nedraw.upgrading.recipe;

import com.nedraw.upgrading.item.ModItems;
import com.nedraw.upgrading.item.ZSlotComponentItem;
import com.nedraw.upgrading.item.ZSlotComponentType;
import com.nedraw.upgrading.item.ZSlotItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceLocation;
import com.nedraw.upgrading.UpgradingMod;

public class ZSlotAssemblyRecipe implements CraftingRecipe {

    public static final RecipeSerializer<ZSlotAssemblyRecipe> SERIALIZER =
            new SimpleCraftingRecipeSerializer<>(ZSlotAssemblyRecipe::new);

    private final CraftingBookCategory category;

    public ZSlotAssemblyRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 1 || input.height() != 3) return false;

        ItemStack top    = input.getItem(0, 0);
        ItemStack middle = input.getItem(0, 1);
        ItemStack bottom = input.getItem(0, 2);

        if (top.isEmpty() || middle.isEmpty() || bottom.isEmpty()) return false;

        return isFrame(top) && isChip(middle) && isBoard(bottom);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack top    = input.getItem(0, 0);
        ItemStack middle = input.getItem(0, 1);
        ItemStack bottom = input.getItem(0, 2);

        String frame = ((ZSlotComponentItem) top.getItem()).getComponentId();
        String chip  = ((ZSlotComponentItem) middle.getItem()).getComponentId();
        String board = ((ZSlotComponentItem) bottom.getItem()).getComponentId();

        ItemStack result = new ItemStack(ModItems.Z_SLOT.get());
        ZSlotItem.setComponents(result, frame, board, chip);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return new ItemStack(ModItems.Z_SLOT.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(net.minecraft.tags.ItemTags.create(
                ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "frames"))));
        list.add(Ingredient.of(net.minecraft.tags.ItemTags.create(
                ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "chips"))));
        list.add(Ingredient.of(net.minecraft.tags.ItemTags.create(
                ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "boards"))));
        return list;
    }

    private static boolean isFrame(ItemStack stack) {
        return stack.getItem() instanceof ZSlotComponentItem c
                && c.getComponentType() == ZSlotComponentType.FRAME;
    }

    private static boolean isChip(ItemStack stack) {
        return stack.getItem() instanceof ZSlotComponentItem c
                && c.getComponentType() == ZSlotComponentType.CHIP;
    }

    private static boolean isBoard(ItemStack stack) {
        return stack.getItem() instanceof ZSlotComponentItem c
                && c.getComponentType() == ZSlotComponentType.BOARD;
    }
}
