package com.skd.ascendantequipment.util;

import com.skd.ascendantequipment.loot.RarityRegistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public abstract class ApothSmithingRecipe implements SmithingRecipe {
    public static final int TEMPLATE = 0;
    public static final int BASE = 1;
    public static final int ADDITION = 2;

    public static final Ingredient BASE_PLACEHOLDER = new Ingredient(new AffixItemIngredient(RarityRegistry.INSTANCE.emptyHolder()));

    protected final Ingredient base;
    protected final Ingredient addition;
    protected final ItemStack result;

    public ApothSmithingRecipe(Ingredient pBase, Ingredient pAddition, ItemStack pResult) {
        this.base = pBase;
        this.addition = pAddition;
        this.result = pResult;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.empty();
    }

    @Override
    public Ingredient baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return Optional.of(this.addition);
    }
}
