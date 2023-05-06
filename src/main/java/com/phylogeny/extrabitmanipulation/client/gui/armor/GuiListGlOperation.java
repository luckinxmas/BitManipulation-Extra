package com.phylogeny.extrabitmanipulation.client.gui.armor;

import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.TextFormatting;

import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.armor.GlOperation;

public abstract class GuiListGlOperation<L> extends GuiListChiseledArmor<GlOperation>
{
	private static final String[] glComponents = new String[]{TextFormatting.BOLD + "x", TextFormatting.BOLD + "y", TextFormatting.BOLD + "z", "\u03B1"};
	
	public GuiListGlOperation(GuiChiseledArmor guiChiseledArmor, int height, int top,
			int bottom, int slotHeight, int offsetX, int listWidth, DataChiseledArmorPiece armorPiece)
	{
		super(guiChiseledArmor, height, top, bottom, slotHeight, offsetX, listWidth, armorPiece);
		setHasListHeader(true, 11);
	}
	
	public abstract List<GlOperation> getGlOperatio