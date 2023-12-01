package com.phylogeny.extrabitmanipulation.recipe;

import java.util.Random;

import mod.chiselsandbits.items.ItemChisel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.phylogeny.extrabitmanipulation.reference.Reference;

public class RecipeChiseledArmor extends ShapelessOreRecipe
{
	private Random rand = new Random();
	private int bitCost;
	
	public RecipeChiseledArmor(NonNullList<Ingredient> ingredients, Item output, Item input, int bitCost)
	{
		super(null, ingredients, new ItemStack(output));
		this.bitCost = bitCost;
		setRegistryName(Reference.MOD_ID, "chiseledarmor" + "_chisel" + "+" + getItemName(input) + "=" + getItemName(output));
	}
	
	private String getItemName(Item item)
	{
		ResourceLocation name = item.getRegistryName();
		return name != null ? name.toString().substring(name.toString().indexOf(":") + 1).replace("_", "") : "";
