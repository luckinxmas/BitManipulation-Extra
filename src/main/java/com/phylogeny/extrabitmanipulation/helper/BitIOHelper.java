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
	
	private static void stateToBitMapFromStateIdArray(Map<IBlockState, IBitBrush> stateToBitMap, int[] mapArray, IChiselAndBitsAPI api)
	{
		for (int i = 0; i < mapArray.length; i += 2)
		{
			IBlockState state = Block.getStateById(mapArray[i]);
			if (!isAir(state))
			{
				try
				{
					stateToBitMap.put(state, api.createBrushFromState(Block.getStateById(mapArray[i + 1])));
				}
				catch (InvalidBitItem e) {}
			}
		}
	}
	
	public static void writeStateToBitMapToNBT(ItemStack bitStack, String key, Map<IBlockState, IBitBrush> stateToBitMap, boolean saveStatesById)
	{
		if (!bitStack.hasTagCompound())
			return;
		
		NBTTagCompound nbt = ItemStackHelper.getNBT(bitStack);
		if (saveStatesById)
		{
			writeObjectToNBT(nbt, key + 0, stateToBitMapToStateIdArray(stateToBitMap));
			nbt.removeTag(key + 1);
			nbt.removeTag(key + 2);
			nbt.removeTag(key + 3);
		}
		else
		{
			int counter = 0;
			int n = stateToBitMap.size();
			boolean isBlockMap = key.equals(NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT);
			String[] domainArray = new String[n * 2];
			String[] pathArray = new String[n * 2];
			byte[] metaArray = new byte[isBlockMap ? n : n * 2];
			for (Entry<IBlockState, IBitBrush> entry : stateToBitMap.entrySet())
			{
				saveStateToMapArrays(domainArray, pathArray, isBlockMap ? null : metaArray, counter++, isBlockMap, entry.getKey());
				saveStateToMapArrays(domainArray, pathArray, metaArray, counter++, isBlockMap, Block.getStateById(entry.getValue().getStateID()));
			}
			nbt.removeTag(key + 0);
			writeObjectToNBT(nbt, key + 1, domainArray);
			writeObjectToNBT(nbt, key + 2, pathArray);
			writeObjectToNBT(nbt, key + 3, metaArray);
		}
	}
	
	private static void saveStateToMapArrays(String[] domainArray, String[] pathArray, byte[] metaArray, int index, boolean isBlockMap, IBlockState state)
	{
		ResourceLocation regName = state.getBlock().getRegistryName();
		if (regName == null)
			return;
		
		domainArray[index] = regName.getResourceDomain();
		pathArray[index] = regName.getResourcePath();
		if (metaArray != null)
			metaArray[isBlockMap ? index / 2 : index] = (byte) state.getBlock().getMetaFromState(state);
	}
	
	public static Map<IBlockState, IBitBrush> readStateToBitMapFromNBT(IChiselAndBitsAPI api, ItemStack bitStack, String key)
	{
		Map<IBlockState, IBitBrush> stateToBitMap = new HashMap<IBlockState, IBitBrush>();
		if (!bitStack.hasTagCompound())
			return stateToBitMap;
		
		NBTTagCompound nbt = ItemStackHelper.getNBT(bitStack);
		boolean saveStatesById = !nbt.hasKey(key + 2);
		if (saveStatesById ? !nbt.hasKey(key + 0) : !nbt.hasKey(key + 1) || !nbt.hasKey(key + 3))
			return stateToBitMap;
		
		if (saveStatesById)
		{
			int[] mapArray = (int[]) readObjectFromNBT(nbt, key + 0);
			if (mapArray == null)
				return stateToBitMap;
			
			stateToBitMapFromStateIdArray(stateToBitMap, mapArray, api);
		}
		else
		{
			String[] domainArray = (String[]) readObjectFromNBT(nbt, key + 1);
			String[] pathArray = (String[]) readObjectFromNBT(nbt, key + 2);
			byte[] metaArray = (byte[]) readObjectFromNBT(nbt, key + 3);
			if (domainArray == null || pathArray == null || metaArray == null)
				return stateToBitMap;
			
			boolean isBlockMap = key.equals(NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT);
			for (int i = 0; i < domainArray.length; i += 2)
			{
				IBlockState state = readStateFromMapArrays(domainArray, pathArray, isBlockMap ? null : metaArray, i, isBlockMap);
				if (!isAir(state))
				{
					try
					{
						stateToBitMap.put(state, api.createBrushFromState(readStateFromMapArrays(domainArray, pathArray, metaArray, i + 1, isBlockMap)));
					}
					catch (InvalidBitItem e) {}
				}
			}
		}
		return stateToBitMap;
	}
	
	private static IBlockState readStateFromMapArrays(String[] domainArray, String[] pathArray, byte[] metaArray, int index, boolean isBlockMap)
	{
		Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(domainArray[index], pathArray[index]));
		return block == null ? Blocks.AIR.getDefaultState() : (metaArray != null
				? getStateFromMeta(block, metaArray[isBlockMap ? index / 2 : index]) : block.getDefaultState());
	}
	
	private static byte[] compressObject(Object object) throws IOException
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objectStream = new ObjectOutputStream(new DeflaterOutputStream(byteStream));
		objectStream.writeObject(object);
		objectStream.close();
		return byteStream.toByteArray();
	}
	
	private static Object decompressObject(byte[] bytes) throws IOException, ClassNotFoundException
	{
		return (new ObjectInputStream(new InflaterInputStream(new ByteArrayInputStream(bytes)))).readObject();
	}
	
	private static void writeObjectToNBT(NBTTagCompound nbt, String key, Object object)
	{
		try
		{
			nbt.setByteArray(key, compressObject(object));
		}
		catch (IOException e) {}
	}
	
	private static Object readObjectFromNBT(NBTTagCompound nbt, String key)
	{
		try
		{
			return decompressObject(nbt.getByteArray(key));
		}
		catch (ClassNotFoundException e) {}
		catch (IOException e) {}
		return null;
	}
	
	private static Object objectFromBytes(ByteBuf buffer)
	{
		int length = buffer.readInt();
		if (length == 0)
			return null;
		
		try
		{
			byte[] bytes = new byte[length];
			buffer.readBytes(bytes);
			return decompressObject(bytes);
		}
		catch (ClassNotFoundException e) {}
		catch (IOException e) {}
		return null;
	}
	
	private static void objectToBytes(ByteBuf buffer, Object object)
	{
		try
		{
			byte[] bytes = compressObject(object);
			buffer.writeInt(bytes.length);
			buffer.writeBytes(bytes);
		}
		catch (IOException e)
		{
			buffer.writeInt(0);
		}
	}
	
	public static void readStatesFromNBT(NBTTagCompound nbt, Map<IBlockState, Integer> stateMap, IBlockState[][][] stateArray)
	{
		String key = NBTKeys.SAVED_STATES;
		int[] stateIDs = (int[]) readObjectFromNBT(nbt, key);
		if (stateIDs == null)
			stateIDs = new int[4096];
		
		for (int n = 0; n < stateIDs.length; n++)
		{
			int i = n / 256;
			int n2 = n % 256;
			int j = n2 / 16;
			int k = n2 % 16;
			IBlockState state = Block.getStateById(stateIDs[n]);
			stateArray[i][j][k] = state;
			if (!isAir(state))
				stateMap.put(state, 1 + (stateMap.containsKey(state) ? stateMap.get(state) : 0));
		}
		if (stateIDs.length == 0)
		{
			IBlockState air = Blocks.AIR.getDefaultState();
			for (int i = 0; i < 16; i++)
			{
				for (int j = 0; j < 16; j++)
				{
					for (int k = 0; k < 16; k++)
						stateArray[i][j][k] = air;
				}
			}
		}
	}
	
	public static void saveBlockStates(IChiselAndBitsAPI api, EntityPlayer player, World world, AxisAlignedBB box, NBTTagCompound nbt)
	{
		if (world.isRemote)
			return;
		
		int[] stateIDs = new int[4096];
		int index = 0;
		int diffX = 16 - (int) (box.m