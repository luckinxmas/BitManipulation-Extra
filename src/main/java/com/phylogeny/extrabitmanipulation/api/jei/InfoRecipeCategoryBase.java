package com.phylogeny.extrabitmanipulation.api.jei;

import javax.annotation.Nullable;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;

import com.phylogeny.extrabitmanipulation.reference.Reference;

public abstract class InfoRecipeCategoryBase<T extends IRecipeWrapper> implements IRecipeCa