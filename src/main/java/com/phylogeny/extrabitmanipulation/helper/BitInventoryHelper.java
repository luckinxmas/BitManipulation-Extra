
package com.phylogeny.extrabitmanipulation.helper;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.shape.Shape;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BitInventoryHelper
{

	@Nullable
	private static IItemHandler getItemHandler(ItemStack stack)
	{
		return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
	}

	public static Map<Integer, Integer> getInventoryBitCounts(IChiselAndBitsAPI api, EntityPlayer player)
	{
		Map<Integer, Integer> bitCounts = new HashMap<Integer, Integer>();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			Set<ItemStack> stacks;
			if (isBitStack(api, stack))
			{
				stacks = Collections.singleton(stack);
			}
			else
			{
				IItemHandler itemHandler = getItemHandler(stack);
				if (itemHandler == null)
					continue;

				stacks = new HashSet<>();
				for (int j = 0; j < itemHandler.getSlots(); j++)
					stacks.add(itemHandler.getStackInSlot(j));
			}
			for (ItemStack stackBitType : stacks)
			{
				try
				{
					int bitStateID = api.createBrush(stackBitType).getStateID();
					if (!bitCounts.containsKey(bitStateID))
						bitCounts.put(bitStateID, countInventoryBits(api, player, stackBitType));
				}
				catch (InvalidBitItem e) {}
			}
		}
		return getSortedLinkedHashMap(bitCounts, new Comparator<Object>() {
			@Override
			@SuppressWarnings("unchecked")
			public int compare(Object object1, Object object2)
			{
				return ((Comparable<Integer>) ((Map.Entry<Integer, Integer>) (object2)).getValue())
						.compareTo(((Map.Entry<Integer, Integer>) (object1)).getValue());
			}
		});
	}
	
	public static LinkedHashMap getSortedLinkedHashMap(Map bitCounts, Comparator<Object> comparator)
	{
		List<Map.Entry> bitCountsList = new LinkedList(bitCounts.entrySet());
		Collections.sort(bitCountsList, comparator);
		LinkedHashMap bitCountsSorted = new LinkedHashMap();
		for (Map.Entry entry : bitCountsList)
			bitCountsSorted.put(entry.getKey(), entry.getValue());
		
		return bitCountsSorted;
	}
	
	public static int countInventoryBits(IChiselAndBitsAPI api, EntityPlayer player, ItemStack stackBitType)
	{
		int count = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			
			count += getBitCountFromStack(api, stackBitType, stack);
			IItemHandler itemHandler = getItemHandler(stack);
			if (itemHandler == null)
				continue;
			
			for (int j = 0; j < itemHandler.getSlots(); j++)
				count += getBitCountFromStack(api, stackBitType, itemHandler.getStackInSlot(j));
		}
		return count;
	}
	
	public static int countInventoryBlocks(EntityPlayer player, Block block)
	{
		int count = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() == block)
				count += stack.getCount();
		}
		return count;
	}
	
	private static int getBitCountFromStack(IChiselAndBitsAPI api, ItemStack stackBitType, ItemStack stack)
	{
		return areBitStacksEqual(api, stackBitType, stack) ? stack.getCount() : 0;
	}
	
	private static boolean areBitStacksEqual(IChiselAndBitsAPI api, ItemStack stackBitType, ItemStack putativeBitStack)
	{
		return isBitStack(api, putativeBitStack) && ItemStack.areItemStackTagsEqual(putativeBitStack, stackBitType);
	}
	
	public static boolean isBitStack(IChiselAndBitsAPI api, ItemStack putativeBitStack)
	{
		return !putativeBitStack.isEmpty() && api.getItemType(putativeBitStack) == ItemType.CHISLED_BIT;
	}
	
	public static void removeBitsFromBlocks(IChiselAndBitsAPI api, EntityPlayer player, ItemStack bitStack, Block block, int quota)
	{
		if (quota <= 0)
			return;
		
		InventoryPlayer inventoy = player.inventory;
		for (int i = 0; i < inventoy.getSizeInventory(); i++)
		{
			ItemStack stack = inventoy.getStackInSlot(i);
			if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock))
				continue;