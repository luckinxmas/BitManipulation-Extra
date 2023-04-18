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
		this.guiChiseledArmor = g