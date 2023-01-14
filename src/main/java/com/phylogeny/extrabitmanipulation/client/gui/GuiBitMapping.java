
package com.phylogeny.extrabitmanipulation.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.GuiHelper;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonBase;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonGradient;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonSelect;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonTab;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonTextured;
import com.phylogeny.extrabitmanipulation.client.render.RenderState;
import com.phylogeny.extrabitmanipulation.config.ConfigHandlerExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool.BitCount;
import com.phylogeny.extrabitmanipulation.packet.PacketAddBitMapping;
import com.phylogeny.extrabitmanipulation.packet.PacketBitMappingsPerTool;
import com.phylogeny.extrabitmanipulation.packet.PacketClearStackBitMappings;
import com.phylogeny.extrabitmanipulation.packet.PacketCursorStack;
import com.phylogeny.extrabitmanipulation.packet.PacketOverwriteStackBitMappings;
import com.phylogeny.extrabitmanipulation.packet.PacketSetDesign;
import com.phylogeny.extrabitmanipulation.packet.PacketSetTabAndStateBlockButton;
import com.phylogeny.extrabitmanipulation.proxy.ProxyCommon;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;

public class GuiBitMapping extends GuiContainer
{
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/guis/modeling_tool.png");
	public static final ResourceLocation SETTINGS_MAIN = new ResourceLocation(Reference.MOD_ID, "textures/guis/settings_main.png");
	public static final ResourceLocation SETTINGS_BACK = new ResourceLocation(Reference.MOD_ID, "textures/guis/settings_back.png");
	private IChiselAndBitsAPI api;
	private GuiListBitMapping bitMappingList;
	private ItemStack previewStack, previewResultStack;
	private IBlockState[][][] stateArray;
	private Map<IBlockState, Integer> stateMap;
	private Map<IBlockState, ArrayList<BitCount>> stateToBitCountArray;
	private Map<IBlockState, IBitBrush> stateToBitMapPermanent, stateToBitMapManual, blockToBitMapPermanent, blockToBitMapManual, blockToBitMapAllBlocks;
	private GuiButtonSelect buttonStates, buttonBlocks;
	private GuiButtonTextured buttonSettings, buttonBitMapPerTool;
	private GuiButtonGradient buttonOverwriteStackMapsWithConfig, buttonOverwriteConfigMapsWithStack, buttonRestoreConfigMaps, buttonClearStackMaps;
	private GuiButtonTab[] tabButtons = new GuiButtonTab[4];
	private static final String[] TAB_HOVER_TEXT = new String[]{"Current Model", "All Saved Mappings", "All Minecraft Blocks", "Model Result"};
	private int savedTab, mouseInitialX, mouseInitialY;
	private boolean stateMauallySelected, showSettings, bitMapPerTool, designMode, previewStackBoxClicked;
	private String searchText = "";
	private GuiTextField searchField;
	private float previewStackScale;
	private Vec3d previewStackRotation, previewStackTranslation, previewStackTranslationInitial;
	private AxisAlignedBB previewStackBox;
	
	public GuiBitMapping(EntityPlayer player, boolean designMode)
	{
		super(ProxyCommon.createBitMappingContainer(player));
		this.designMode = designMode;
		api = ChiselsAndBitsAPIAccess.apiInstance;
		xSize = 254;
		ySize = 219;
		previewStackScale = 3.8F;
		previewStackRotation = new Vec3d(30, 225, 0);
		previewStackTranslation = Vec3d.ZERO;
		previewStackTranslationInitial = Vec3d.ZERO;
		if (designMode)
			return;
		
		NBTTagCompound nbt = ItemStackHelper.getNBTOrNew(player.inventory.getCurrentItem());
		stateMauallySelected = nbt.getBoolean(NBTKeys.BUTTON_STATE_BLOCK_SETTING);
		savedTab = nbt.getInteger(NBTKeys.TAB_SETTING);
		bitMapPerTool = nbt.getBoolean(NBTKeys.BIT_MAPS_PER_TOOL);
		previewStack = previewResultStack = ItemStack.EMPTY;
	}
	
	private void constructManualMaps()
	{
		stateToBitMapManual = new LinkedHashMap<IBlockState, IBitBrush>();
		blockToBitMapManual = new LinkedHashMap<IBlockState, IBitBrush>();
		blockToBitMapAllBlocks = new LinkedHashMap<IBlockState, IBitBrush>();
		if (stateMap.isEmpty())
			return;
		
		if (!designMode && tabButtons[2].selected)
		{
			for (Block block : Block.REGISTRY)
			{
				ResourceLocation regName = block.getRegistryName();
				if (regName == null)
					continue;
				
				if (regName.getResourceDomain().equals(ChiselsAndBitsReferences.MOD_ID))
				{
					Item item = Item.getItemFromBlock(block);
					if (item != Items.AIR)
					{
						ItemType itemType = api.getItemType(new ItemStack(item));
						if (itemType != null && itemType == ItemType.CHISLED_BLOCK)
							continue;
					}
				}
				if (BitIOHelper.isAir(block))
					continue;
				
				IBlockState state = block.getDefaultState();
				addBitToManualMap(state.getBlock().getDefaultState(), blockToBitMapPermanent, blockToBitMapAllBlocks);
			}
			blockToBitMapAllBlocks = getSortedLinkedBitMap(blockToBitMapAllBlocks);
		}
		for (IBlockState state : stateMap.keySet())
		{
			addBitToManualMap(state, stateToBitMapPermanent, stateToBitMapManual);
			if (!designMode)
				addBitToManualMap(state.getBlock().getDefaultState(), blockToBitMapPermanent, blockToBitMapManual);
		}
		stateToBitMapManual = getSortedLinkedBitMap(stateToBitMapManual);
		blockToBitMapManual = getSortedLinkedBitMap(blockToBitMapManual);
	}
	
	private void addBitToManualMap(IBlockState state, Map<IBlockState, IBitBrush> bitMapPermanent, Map<IBlockState, IBitBrush> bitMapManual)
	{
		IBitBrush bit = null;
		if (bitMapPermanent.containsKey(state))
		{
			bit = bitMapPermanent.get(state);
		}
		else
		{
			try
			{
				bit = api.createBrushFromState(state);
			}
			catch (InvalidBitItem e) {}
		}
		bitMapManual.put(state, bit);
	}
	
	public void addPermanentMapping(IBlockState state, IBitBrush bit)
	{
		Map<IBlockState, IBitBrush> bitMapPermanent = getBitMapPermanent();
		if (bit != null)
		{
			bitMapPermanent.put(state, bit);
			Map<IBlockState, IBitBrush> blockToBitMap = getBitMapManual();
			if (blockToBitMap.containsKey(state))
				blockToBitMap.put(state, bit);
		}
		else
		{
			bitMapPermanent.remove(state);
			constructManualMaps();
		}
		if (designMode)
		{
			refreshList();
			return;
		}
		if (bitMapPerTool)
		{
			String nbtKey = buttonStates.selected ? NBTKeys.STATE_TO_BIT_MAP_PERMANENT : NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT;
			ExtraBitManipulation.packetNetwork.sendToServer(new PacketAddBitMapping(nbtKey, state, bit, Configs.saveStatesById));
		}
		else
		{
			Map<IBlockState, IBitBrush> bitMap = buttonStates.selected ? Configs.modelStateToBitMap : Configs.modelBlockToBitMap;
			if (bit != null)
			{
				bitMap.put(state, bit);
			}
			else
			{
				bitMap.remove(state);
			}
			String[] entryStrings = BitIOHelper.getEntryStringsFromModelBitMap(bitMap);
			if (buttonStates.selected)
			{
				Configs.modelStateToBitMapEntryStrings = entryStrings;
			}
			else
			{
				Configs.modelBlockToBitMapEntryStrings = entryStrings;
			}
			BitToolSettingsHelper.setBitMapProperty(buttonStates.selected, entryStrings);
		}
		refreshList();
	}
	
	private void refreshList()
	{
		constructStateToBitCountArray();
		Map<IBlockState, IBitBrush> bitMapPermanent = getBitMapPermanent();
		bitMappingList.refreshList(designMode || tabButtons[0].selected || tabButtons[2].selected ? getBitMapManual()
				: (isResultsTabSelected() ? null : bitMapPermanent), bitMapPermanent, isResultsTabSelected() ? stateToBitCountArray : null,
						searchField.getText(), designMode || buttonStates.selected);
		setPreviewStack();
		if (!designMode)
			tabButtons[0].setIconStack(previewStack);
	}
	
	@SuppressWarnings("null")
	private void constructStateToBitCountArray()
	{
		stateToBitCountArray = new LinkedHashMap<IBlockState, ArrayList<BitCount>>();
		if (designMode)
		{
			for (Entry<IBlockState, Integer> entry : stateMap.entrySet())
			{
				ArrayList<BitCount> bitCountArray = new ArrayList<BitCount>();
				try
				{
					bitCountArray.add(new BitCount(api.createBrushFromState(entry.getKey()), entry.getValue()));
					stateToBitCountArray.put(entry.getKey(), bitCountArray);
				}
				catch (InvalidBitItem e) {}
			}
			return;
		}
		Map<IBitBrush, Integer> bitMap = new HashMap<IBitBrush, Integer>();
		EntityPlayer player = mc.player;
		ItemModelingTool itemModelingTool = (ItemModelingTool) getHeldStack().getItem();
		if (itemModelingTool.mapBitsToStates(api, Configs.replacementBitsUnchiselable, Configs.replacementBitsInsufficient,
				BitInventoryHelper.getInventoryBitCounts(api, player), stateMap, stateToBitCountArray,
				stateToBitMapPermanent, blockToBitMapPermanent, bitMap, player.capabilities.isCreativeMode).isEmpty())
		{
			stateToBitCountArray = getSortedLinkedBitMap(stateToBitCountArray);
			IBitAccess bitAccess = api.createBitItem(ItemStack.EMPTY);
			Map<IBlockState, ArrayList<BitCount>> stateToBitCountArrayCopy = new HashMap<IBlockState, ArrayList<BitCount>>();
			for (Entry<IBlockState, ArrayList<BitCount>> entry : stateToBitCountArray.entrySet())
			{
				ArrayList<BitCount> bitCountArray = new ArrayList<BitCount>();
				for (BitCount bitCount : entry.getValue())
					bitCountArray.add(new BitCount(bitCount.getBit(), bitCount.getCount()));
				
				stateToBitCountArrayCopy.put(entry.getKey(), bitCountArray);
			}
			previewResultStack = itemModelingTool.createModel(null, null, getHeldStack(), stateArray, stateToBitCountArrayCopy, bitAccess)
					? bitAccess.getBitsAsItem(null, ItemType.CHISLED_BLOCK, false) : ItemStack.EMPTY;
		}
		else
		{
			previewResultStack = ItemStack.EMPTY;
		}
	}
	
	private Map<IBlockState, IBitBrush> getBitMapManual()
	{
		if (designMode)
			return stateToBitMapManual;
		
		return tabButtons[2].selected ? blockToBitMapAllBlocks : (buttonStates.selected ? stateToBitMapManual : blockToBitMapManual);
	}
	
	private Map<IBlockState, IBitBrush> getBitMapPermanent()
	{
		return designMode || buttonStates.selected ? stateToBitMapPermanent : blockToBitMapPermanent;
	}
	
	@SuppressWarnings("null")
	public void setPreviewStack()
	{
		IBitAccess bitAccess = api.createBitItem(ItemStack.EMPTY);
		IBitBrush defaultBit = null;
		try
		{
			defaultBit = api.createBrushFromState((Configs.replacementBitsUnchiselable.getDefaultReplacementBit().getDefaultState()));
		}
		catch (InvalidBitItem e) {}
		for (int i = 0; i < 16; i++)
		{
			for (int j = 0; j < 16; j++)
			{
				for (int k = 0; k < 16; k++)
				{
					IBlockState state = stateArray[i][j][k];
					if (designMode)
					{
						try
						{
							bitAccess.setBitAt(i, j, k, stateToBitMapManual.get(state));
						}
						catch (SpaceOccupied e) {}
						continue;
					}
					IBlockState state2 = state.getBlock().getDefaultState();
					boolean stateFound = stateToBitMapManual.containsKey(state);
					boolean savedStateFound = stateToBitMapPermanent.containsKey(state);
					boolean savedBlockFound = blockToBitMapPermanent.containsKey(state2);
					if (stateFound || savedBlockFound)
					{
						IBitBrush bit = savedBlockFound && !savedStateFound ? blockToBitMapPermanent.get(state2) : stateToBitMapManual.get(state);
						try
						{
							bitAccess.setBitAt(i, j, k, bit != null ? bit : defaultBit);
						}
						catch (SpaceOccupied e) {}
					}
				}
			}
		}
		previewStack = bitAccess.getBitsAsItem(null, ItemType.CHISLED_BLOCK, false);
	}
	
	public ItemStack getHeldStack()
	{
		return mc.player.getHeldItemMainhand();
	}
	
	@Override
	public int getGuiLeft()
	{
		return guiLeft + 24;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		guiLeft -= 12;
		int l = guiLeft + 128;
		int t = guiTop + 21;
		previewStackBox = new AxisAlignedBB(l, t, -1, l + 107, t + 100, 1);
		searchField = new GuiTextField(6, fontRenderer, guiLeft + 44, guiTop + 8, 65, 9);
		searchField.setEnableBackgroundDrawing(false);
		searchField.setTextColor(-1);
		searchField.setText(searchText);
		int slotHeight = 24;
		if (designMode)
		{
			stateToBitMapPermanent = new HashMap<IBlockState, IBitBrush>();
			blockToBitMapPermanent = new HashMap<IBlockState, IBitBrush>();
			initDesignMode();
			String buttonText = "Save Changes";
			int buttonWidth = fontRenderer.getStringWidth(buttonText) + 6;
			buttonList.add(new GuiButtonExt(0, guiLeft + xSize - buttonWidth - 5, guiTop + 5, buttonWidth, 14, buttonText));
		}
		else
		{
			buttonSettings = new GuiButtonTextured(7, guiLeft + 237, guiTop + 6, 12, 12, "Bit Mapping Settings", SETTINGS_BACK, SETTINGS_MAIN, null, null);
			buttonSettings.setHoverTextSelected("Back To Preview");
			buttonBitMapPerTool = GuiButtonTextured.createCheckBox(8, guiLeft + 143, guiTop + 26, 12, 12, "Save/access mappings per tool or per client config");
			if (showSettings)
				buttonSettings.selected = true;
			
			int y = guiTop + 44;
			int offsetY = 19;
			String hovertext = "Overwrite mappings saved in 1 with the mappings saved in 2";
			String stackText = "this Modeling Tool's NBT";
			String configText = "the client config file";
			buttonOverwriteStackMapsWithConfig = new GuiButtonGradient(9, guiLeft + 130, y, 102, 14,
					"Write Config->Stack", hovertext.replace("1", stackText).replace("2", configText));
			buttonOverwriteConfigMapsWithStack = new GuiButtonGradient(10, guiLeft + 130, y + offsetY, 102, 14,
					"Write Stack->Config", hovertext.replace("2", stackText).replace("1", configText));
			buttonRestoreConfigMaps = new GuiButtonGradient(11, guiLeft + 130, y + offsetY * 2, 102, 14,
					"Reset Config Maps", "Reset " + configText + " mapping data to their default values");
			buttonClearStackMaps = new GuiButtonGradient(12, guiLeft + 130, y + offsetY * 3, 102, 14,
					"Clear Stack Data", "Delete all saved mappping data from " + stackText);
			updateButtons();
			if (bitMapPerTool)
			{
				buttonBitMapPerTool.selected = true;
				stateToBitMapPermanent = BitIOHelper.readStateToBitMapFromNBT(api, getHeldStack(), NBTKeys.STATE_TO_BIT_MAP_PERMANENT);