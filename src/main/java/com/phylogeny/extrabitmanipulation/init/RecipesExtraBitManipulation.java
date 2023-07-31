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
	void registerRecipes(RegistryEvent.Register<IRecipe> event)
	{
		registerChiseledArmorRecipes(event, ItemsExtraBitManipulation.chiseledHelmetDiamond, Items.DIAMOND_HELMET, 272);
		registerChiseledArmorRecipes(event, ItemsExtraBitManipulation.chiseledChestplateDiamond, Items.DIAMOND_CHESTPLATE, 444);
		registerChiseledArmorRecipes(event, ItemsExtraBitManipulation.chiseledLeggingsDiamond, Items.DIAMOND_LEGGINGS, 572);
		registerChiseledArmorRecipes(event, ItemsExtraBitManipulation.chiseledBootsDiamond, Items.DIAMOND_BOOTS, 272);
		registerChiseledArmorRecipes(event, ItemsExtraBitManipulation.chiseledHelmetIron, Items.IRON_HELMET, 272);
		registerChiseledArmorRecipes(event, ItemsExtraBitManipulation.chiseledChestplateIron, Items.IRON_CHESTPLATE, 444);
		registerChiseledArmorRecipes(event, ItemsExtraBitManipulation.chiseledLeggingsIron, Items.IRON_LEGGINGS, 572);
		registerChiseledArmorRecipes(event, ItemsExtraBitManipulation.chiseledBootsIron, Items.IRON_BOOTS, 272);
	}
	
	private static void registerChiseledArmorRecipes(RegistryEvent.Register<IRecipe> event, Item itemChiseled, Item itemVanilla, int bitCost)
	{
		registerChiseledArmorRecipe(event, itemChiseled, itemVanilla, bitCost);
		registerChiseledArmorRecipe(event, itemVanilla, itemChiseled, bitCost);
	}
	
	private static void registerChiseledArmorRecipe(RegistryEvent.Register<IRecipe> event, Item output, Item input, int bitCost)
	{
		ModItem