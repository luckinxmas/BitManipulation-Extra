package com.phylogeny.extrabitmanipulation.api.jei.armor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import com.phylogeny.extrabitmanipulation.api.jei.InfoRecipeBase;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.GuiHelper;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;

public class ChiseledArmorInfoRecipe extends InfoRecipeBase
{
	public static final String[] GRAPHIC_NAMES = new String[]{"chiseled_helmet", "creation", "collection", "chiseled_armor_gui", "chiseled_armor_slots_gui"};
	private int imageIndex;
	
	public static List<ChiseledArmorInfoRecipe> create(IGuiHelper guiHelper, List<ItemStack> sculptingStacks)
	{
		List<ChiseledArmorInfoRecipe> recipes = new ArrayList<ChiseledArmorInfoRecipe>();
		for (int i = 0; i < GRAPHIC_NAMES.length; i++)
		{
			recipes.add(new ChiseledArmorInfoRecipe(guiHelper, sculptingStacks, ChiseledArmorInfoRecipeCategory.NAME,
					GRAPHIC_NAMES[i], i == 0 ? 89 : (i == 3 ? 76 : (i == 4 ? 75 : 78)), i == 0 ? 21 : (i == 3 ? 32 : (i == 4 ? 41 : 19)),
							i == 0 ? 187 : (i == 3 ? 186 : (i ==