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
	
	public boolean isInteractive()
	{
		return isInteractive;
	}
	
	public IBlockState getState()
	{
		return state;
	}
	
	private BitCount getBitCountObject()
	{
		if (bitCountArray.isEmpty())
			return null;
		
		return bitCountArray.get(frameCounter % (bitCountArray.size() * 120) / 120);
	}
	
	public ArrayList<BitCount> getBitCountArray()
	{
		return bitCountArray;
	}
	
	private IBitBrush getBit()
	{
		BitCount bitCount = getBitCountObject();
		if (bitCount == null)
			return null;
		
		return bitCount.getBit();
	}
	
	public ItemStack getBitStack()
	{
		IBitBrush bit = getBit();
		return bit == null ? ItemStack.EMPTY : bit.getItemStack(1);
	}
	
	public boolean isAir()
	{
		IBitBrush bit = getBit();
		return bit != null && bit.isAir();
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
	{
		frameCounter++;
		x -= 43;
		y -= 1;
		int guiTop = bitMappingScreen.getGuiTop();
		if (y > guiTop && y < guiTop + 125)
		{
			if (isManuallyMapped)
			{
				bitMappingScreen.drawRect(x, y - 1, x + listWidth, y + slotHeight + 1, 2110310655);
				GlStateManager.enableBlend();
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			ClientHelper.bindTexture(bitMappingScreen.GUI_TEXTURE);
			bitMappingScreen.drawTexturedModalRect(x, y, 0, 219, listWidth, slotHeight);
			RenderHelper.enableGUIStandardItemLighting();
			if (!getBitStack().isEmpty())
			{
				mc.getRenderItem().renderItemIntoGUI(getBitStack(), x + 44, y + 2);
			}
			else if (getBit() == null)
			{
				drawCross(x, y);
			}
			RenderState.renderStateIntoGUI(state, x, y);
			RenderHelper.disableStandardItemLighting();
		}
	}
	
	private void drawCross(int x, int y)
	{
		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.color(1, 0, 0);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tesse