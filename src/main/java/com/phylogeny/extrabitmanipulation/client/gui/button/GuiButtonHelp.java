package com.phylogeny.extrabitmanipulation.client.gui.button;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import org.lwjgl.opengl.GL11;

public class GuiButtonHelp extends GuiButtonCustom
{
	List<GuiButton> buttonList;
	
	public GuiButtonHelp(int buttonId, List<GuiButton> buttonList, int x, int y, String hoverText, String hoverTextSelected)
	{
		super(buttonId, x, y, 12, 12, "?", hover