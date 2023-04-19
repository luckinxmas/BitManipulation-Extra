package com.phylogeny.extrabitmanipulation.client.gui.armor;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.client.GuiHelper;

public class GuiListChiseledArmor<E> extends GuiListExtended
{
	protected final GuiChiseledArmor guiChiseledArmor;
	protected final List<GuiListEntryChiseledArmor<E>> entries = Lists.<GuiListEntryChiseledArmor<E>>newArrayList();
	protected DataChiseledArmorPiece armorPiece;
	private int selectedIndex = 0;
	private boolean drawEntries;
	
	public GuiListChiseledArmor(GuiChiseledArmor guiChiseledArmor, int height,
			int top, int bottom, int slotHeight, int offsetX, int listWidth, DataChiseledArmorPiece armorPiece)
	{
		super(guiChiseledArmor.mc, 150, height, top, bottom, slotHeight);
		this.guiChiseledArmor = guiChiseledArmor;
		drawEntries = true;
		headerPadding -= 1;
		left = guiChiseledArmor.getGuiLeft() + offsetX;
		right = left + listWidth;
		this.armorPiece = armorPiece;
	}
	
	public void refreshList()
	{
		entries.clear();
	}
	
	public void setDrawEntries(boolean drawEntries)
	{
		this.drawEntries = drawEntries;
	}
	
	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent)
	{
		if (!isMouseYWithinSlotBounds(mouseY))
			return false;
		
		int i = getSlotIndexFromScreenCoords(mouseX, mouseY);
		if (i < 0)
			return false;
		
		if (getListEntry(i).mousePressed(i, mouseX, mouseY, mouseEvent, mouseX - left,
				mouseY - top + getAmountScrolled() - i * slotHeight - headerPadding - 1))
		{
			setEnabled(false);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseReleased(int mouseX, int mouseY, int mouseEvent)
	{
		int relativeX = mouseX - left;
		int relativeY = mouseY - top + getAmountScrolled() - headerPadding - 1;
		for (int i = 0; i < getSize(); ++i)
			getListEntry(i).mouseReleased(i, mouseX, mouseY, mouseEvent, relativeX, relativeY - i * slotHeight);
		
		setEnabled(true);
		return false;
	}
	
	@Override
	public int getSlotIndexFromScreenCoords(int posX, int posY)
	{
		int y = posY - top - headerPadding + (int)amountScrolled - 1;
		int index = y / slotHeight;
		return posX < getScrollBarX(