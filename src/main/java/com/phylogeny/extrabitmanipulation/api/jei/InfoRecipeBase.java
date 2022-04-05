package com.phylogeny.extrabitmanipulation.api.jei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.phylogeny.extrabitmanipulation.reference.Reference;

public class InfoRecipeBase implements IRecipeWrapper
{
	protected List<ItemStack> itemStacks;
	protected List<String> tooltipLines = new ArrayList<String>();
	protected ResourceLocation image;
	protected int imageWidth, imageHeight;
	protected IDrawable slotDrawable;
	protected String name, text;
	protected Rectangle imageBox;
	
	public InfoRecipeBase(IGuiHelper guiHelper, List<ItemStack> itemStacks, int imageWidth, int imageHeight, String recipeName,
			String imageName, String tooltipName, int imageLeft, int imageTop, int imageRight, int imageBottom, String catagoryName