package com.phylogeny.extrabitmanipulation.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.apache.commons.lang3.StringUtils;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.config.ConfigReplacementBits;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelReadData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelWriteData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.packet.PacketCreateModel;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;

public class ItemModelingTool extends ItemBitToolBase
{
	public static final String[] AREA_MODE_TITLES = new String[]{"Centered", "Corner", "Drawn"};
	public static final String[] SNAP_MODE_TITLES = new String[]{"Off", "Snap-to-Chunk XZ", "Snap-to-Chunk XYZ"};
	
	public ItemModelingTool(String name)
	{
		super(name);
	}
	
	public NBTTagCompound initialize(ItemStack stack, ModelReadData modelingData)
	{
		NBTTagCompound nbt = ItemStackHelper.initNBT(stack);
		initInt(nbt, NBTKeys.MODEL_AREA_MODE, modelingData.getAreaMode());
		initInt(nbt, NBTKeys.MODEL_SNAP_MODE, modelingData.getSnapMode());
		initBoolean(nbt, NBTKeys.MODEL_GUI_OPEN, modelingData.getGuiOpen());
		return nbt;
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world,
			BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote && stack.hasTagCompound())
		{
			@SuppressWarnings("null")
			ModelWriteData modelingData = new ModelWriteData(stack.getTagCompound().getBoolean(NBTKeys.BIT_MAPS_PER_TOOL));
			if (createModel(stack, player, world, pos, facing, modelingData) == EnumActionResult.SUCCESS)
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketCreateModel(pos, facing, modelingData));
		}
		return EnumActionResult.SUCCESS;
	}
	
	public EnumActionResult createModel(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing facing, ModelWriteData modelingData)
	{
		if (!stack.hasTagCompound())
			return EnumActionResult.FAIL;
		
		if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos))
		{
			pos = pos.offset(facing);
			if (!world.isAirBlock(pos))
				return EnumActionResult.FAIL;
		}
		world.setBlockToAir(pos);
		IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
		NBTTagCompound nbt = ItemStackHelper.getNBT(stack);
		if (!nbt.hasKey(NBTKeys.SAVED_STATES))
			return EnumActionResult.FAIL;
		
		Map<IBlockState, Integer> stateMap = new HashMap<IBlockState, Integer>();
		IBlockState[][][] stateArray = new IBlockState[16][16][16];
		BitIOHelper.readStatesFromNBT(nbt, stateMap, stateArray);
		Map<IBlockState, ArrayList<BitCount>> stateToBitCountArray = new HashMap<IBlockState, ArrayList<BitCount>>();
		Map<IBitBrush, Integer> bitMap = new HashMap<IBitBrush, Integer>();
		Map<IBlockState, Integer> missingBitMap = mapBitsToStates(api, modelingData.getReplacementBitsUnchiselable(),
				modelingData.getReplacementBitsInsufficient(), BitInventoryHelper.getInventoryBitCounts(api, player), stateMap, stateToBitCountArray,
				modelingData.getStateToBitMap(api, stack), modelingData.getBlockToBitMap(api, stack), bitMap, player.capabilities.isCreativeMode);
		if (!missingBitMap.isEmpty())
		{
			if (world.isRemote)
			{
				int missingBitCount = 0;
				for (IBlockState state : missingBitMap.keySet())
					missingBitCount += missingBitMap.get(state);
				
				sendMessage(player, "Missing " + missingBitCount + " bits to represent the following blocks:");
				for (IBlockState state : missingBitMap.keySet())
				{
					String name = getBlockName(state, new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
					sendMessage(player, "  " + missingBitMap.get(state) + " - " + name);
				}
			}
			return EnumActionResult.FAIL;
		}
		return createModel(player, world, pos, stack, api, stateArray, stateToBitCountArray, bitMap);
	}
	
	private EnumActionResult createModel(EntityPlayer player, World world, BlockPos pos, ItemStack stack, IChiselAndBitsAPI api, IBlockState[][][] stateArray,
			Map<IBlockState, ArrayList<BitCount>> stateToBitCountArray, Map<IBitBrush, Integer> bitMap)
	{
		IBitAccess bitAccess;
		try
		{
			bitAccess = api.getBitAccess(world, pos);
		}
		catch (CannotBeChiseled e)
		{
			e.printStackTrace();
			return EnumActionResult.FAIL;
		}
		try
		{
			api.beginUndoGroup(player);
			if (!createModel(player, world, stack, stateArray, stateToBitCountArray, bitAccess))
				return EnumActionResult.FAIL;
			
			bitAccess.commitChanges(true);
		}
		finally
		{
			api.endUndoGroup(player);
		}
		if (!world.isRemote && !player.capabilities.isCreativeMode)
		{
			for (IBitBrush bit : bitMap.keySet())
			{
				BitInventoryHelper.removeOrAddInventoryBits(api, player, bit.getItemStack(1), bitMap.get(bit).intValue(), false);
				player.inventoryContainer.detectAndSendChanges();
			}
		}
		damageTool(stack, player);
		return EnumActionResult.SUCCESS;
	}
	
	public boolean createModel(EntityPlayer player, World world, ItemStack stack, IBlockState[][][] stateArray,
			Map<IBlockState, ArrayList<BitCount>> stateToBitCountArray, IBitAccess bitAccess)
	{
		if (!ItemStackHelper.hasKey(stack, NBTKeys.SAVED_STATES))
			return false;
		
		for (int i = 0; i < 16; i++)
		{
			for (int j = 0; j < 16; j++)
			{
				for (int k = 0; k < 16; k++)
				{
					try
					{
						IBitBrush bit = null;
						IBlockState state = stateArray[i][j][k];
						if (!state.equals(Blocks.AIR.getDefaultState()))
						{
							for (BitCount bitCount : stateToBitCountArray.get(state))
							{
								if (bitCount.count > 0)
								{
									bitCount.count--;
									bit = bitCount.bit;
									break;
								}
							}
						}
						bitAccess.setBitAt(i, j, k, bit);
					}
					catch (SpaceOccupied e)
					{
						if (world != null && world.isRemote)
							sendMessage(player, "Multipart(s) are in the way.");
						
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public Map<IBlockState, Integer> mapBitsToStates(IChiselAndBitsAPI api, ConfigReplacementBits replacementBitsUnchiselable,
			ConfigReplacementBits replacementBitsInsufficient, Map<Integer, Integer> inventoryBitCounts, Map<IBlockState, Integer> stateMap,
			Map<IBlockState, ArrayList<BitCount>> stateToBitCountArray, Map<IBlockState, IBitBrush> manualStateToBitMap,
			Map<IBlockState, IBitBrush> manualBlockToBitMap, Map<IBitBrush, Integer> bitMap, boolean isCreative)
	{
		Map<IBlockState, Integer> missingBitMap = new HashMap<IBlockState, Integer>();
		Map<IBlockState, Integer> skippedStatesMap = new HashMap<IBlockState, Integer>();
		Map<IBlockState, ArrayList<BitCount>> skippedBitCountArrayMap = new HashMap<IBlockState, ArrayList<BitCount>>();
		for (int pass = 0; pass < 2; pass++)
		{
			for (IBlockState state : stateMap.keySet())
			{
				if (pass == 1 && !skippedStatesMap.containsKey(state))
					continue;
				
				int bitCount = stateMap.get(state);
				ArrayList<BitCount> bitCountArray = pass == 1 ? skippedBitCountArrayMap.get(state) : new ArrayList<BitCount>();
				int remainingBitCount = pass == 1 ? skippedStatesMap.get(state) : 0;
				try
				{
					if (pass == 0)
						remainingBitCount = addBitCountObject(bitCountArray, bitMap, inventoryBitCounts, manualStateToBitMap.containsKey(state)
								? manualStateToBitMap.get(state) : (manualBlockToBitMap.containsKey(state.getBlock().getDefaultState())
												? manualBlockToBitMap.get(state.getBlock().getDefaultState()) : api.createBrushFromState(state)),
												bitCount, isCreative);
					if (remainingBitCount > 0)
					{
						remainingBitCount = getReplacementBit(api, replacementBitsInsufficient, bitMap,
								inventoryBitCounts, bitCountArray, remainingBitCount, isCreative, pass);
						if (remainingBitCount < 0)
						{
							skippedStatesMap.put(state, remainingBitCount * -1);
							skippedBitCountArrayMap.put(state, bitCountArray);
						}
					}
				}
				catch (InvalidBitItem e)
				{
					remainingBitCount = getReplacementBit(api, replacementBitsUnchiselable, bitMap,
							inventoryBitCounts, bitCountArray, bitCount, isCreative, pass);
					if (remainingBitCount < 0)
					{
						skippedStatesMap.put(state, remainingBitCount * -1);
						skippedBitCountArrayMap.put(state, bitCountArray);
					}
				}
				stateToBitCountArray.put(state, bitCountArray);
				if (remainingBitCount > 0 && (pass == 1 || !skippedStatesMap.containsKey(state)))
					missingBitMap.put(state, remainingBitCount);