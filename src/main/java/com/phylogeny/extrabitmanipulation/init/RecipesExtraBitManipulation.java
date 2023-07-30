package com.phylogeny.extrabitmanipulation.init;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import com.phylogeny.extrabitmanipulation.recipe.RecipeChiseledArmor;
import com.phylogeny.extrabitmanipulation.reference.Configs;

public class RecipesExtraBitManipulation
{
	public static void registerOres()
	{
		if (!Configs.disableDiamondNuggetOreDict)
			OreDictionary.registerOre("nuggetDiamond", ItemsExtraBitManipulation.diamondNugget);
	}
	
	@SubscribeEvent
	void registerRecipes(RegistryEvent.Register<IRecip