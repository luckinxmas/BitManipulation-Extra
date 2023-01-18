package com.phylogeny.extrabitmanipulation.client.gui;

import java.util.ArrayList;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.render.RenderState;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool.BitCount;
import com.phylogeny.extrabitmanipulation.packet.PacketCursorStack;

public class GuiListBitMappingEntry implements GuiListExtended.IGuiListEntry
{
	private final Minecraft mc;
	private final GuiBitMapping bitMappingScreen;
	private IBlockState state;
	private ArrayList<BitCount> bitCountArray;
	private boolean isManuallyMapped, isInteractive;
	private int frameCounter;
	
	public GuiListBitMappingEntry(GuiListBitMapping listBitMapping, IBlockState state,
			ArrayList<BitCount> bitCountArray, boolean isManuallyMapped, boolean isInteractive)
	{
		bitMappingScreen = listBitMapping.getGuiModelingTool();
		mc = bitMappingScreen.mc;
		this.state = state;
		this.bitCountArray = bitCountArray;
		this.isManuallyMapped = isManuallyMapped;
		this.isInteractive = isInteractive;
	}
	
	public boolean i