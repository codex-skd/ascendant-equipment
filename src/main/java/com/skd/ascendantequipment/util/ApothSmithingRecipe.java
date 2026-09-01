package com.skd.ascendantequipment.util;

import com.skd.ascendantequipment.loot.LootCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

public abstract class ApothSmithingRecipe extends SmithingTransformRecipe {

    public static final int TEMPLATE = 0, BASE = 1, ADDITION = 2;

    public static final Ingredient BASE_PLACEHOLDER = new Ingredient(new AffixItemIngredient(com.skd.ascendantequipment.loot.RarityRegistry.INSTANCE.emptyHolder()));

    public ApothSmithingRecipe(Ingredient pBase, Ingredient pAddition, ItemStack pResult) {
        super(Ingredient.EMPTY, pBase, pAddition, pResult);
    }

    @Override
    public boolean isBaseIngredient(ItemStack pStack) {
        return !LootCategory.forItem(pStack).isNone();
    }
}
