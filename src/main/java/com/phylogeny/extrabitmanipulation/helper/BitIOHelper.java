package com.phylogeny.extrabitmanipulation.helper;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IMultiStateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;

public class BitIOHelper
{
	
	public static Map<IBlockState, IBitBrush> getModelBitMapFromEntryStrings(String[] entryStrings)
	{
		Map<IBlockState, IBitBrush> bitMap = new HashMap<IBlockState, IBitBrush>();
		for (String entryString : entryStrings)
		{
			if (entryString.indexOf("-") < 0 || entryString.length() < 3)
				continue;
			
			String[] entryStringArray = entryString.split("-");
			IBlockState key = getStateFromString(entryStringArray[0]);
			if (key == null || BitIOHelper.isAir(key))
				continue;
			
			IBlockState value = getStateFromString(entryStringArray[1]);
			if (value == null)
				continue;
			
			try
			{
				bitMap.put(key, ChiselsAndBitsAPIAccess.apiInstance.createBrushFromState(value));
			}
			catch (InvalidBitItem e) {}
		}
		return bitMap;
	}
	
	public static String[] getEntryStringsFromModelBitMap(Map<IBlockState, IBitBrush> bitMap)
	{
		String[] entryStrings = new String[bitMap.size()];
		int index = 0;
		for (Entry<IBlockState, IBitBrush> entry : bitMap.entrySet())
			entryStrings[index++] = getModelBitMapEntryString(entry);
		
		return entryStrings;
	}
	
	public static void stateToBitMapToBytes(ByteBuf buffer, Map<IBlockState, IBitBrush> stateToBitMap)
	{
		if (notNullToBuffer(buffer, stateToBitMap))
			objectToBytes(buffer, stateToBitMapToStateIdArray(stateToBitMap));
	}
	
	public static Map<IBlockState, IBitBrush> stateToBitMapFromBytes(ByteBuf buffer)
	{
		Map<IBlockState, IBitBrush> stateToBitMap = new HashMap<IBlockState, IBitBrush>();
		if (!buffer.readBoolean())
			return stateToBitMap;
		
		int[] mapArray = (int[]) objectFromBytes(buffer);
		if (mapArray == null)
			return stateToBitMap;
		
		stateToBitMapFromStateIdArray(stateToBitMap, mapArray, ChiselsAndBitsAPIAccess.apiInstance);
		return stateToBitMap;
	}
	
	private static int[] stateToBitMapToStateIdArray(Map<IBlockState, IBitBrush> stateToBitMap)
	{
		int counter = 0;
		int[] mapArray = new int[stateToBitMap.size() * 2];
		for (Entry<IBlockState, IBitBrush> entry : stateToBitMap.entrySet())
		{
			mapArray[counter++] = Block.getStateId(entry.getKey());
			mapArray[counter++] = entry.getValue().getStateID();
		}
		return mapArray;
	}
	
	private static void stateToBitMapFromStateIdArray(Map<IBlockState, IBitBrush> stateToBitMap, int[] mapArray, IChiselAndBitsAPI 