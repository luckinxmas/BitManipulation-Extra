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
		super(buttonId, x, y, 12, 12, "?", hoverText);
		this.buttonList = buttonList;
		setHoverTextSelected(hoverTextSelected);
		setTextOffsetX(0.5F);
		setTextOffsetY(0.5F);
	}
	
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
	{
		boolean pressed = super.mousePressed(mc, mouseX, mouseY);
		if (pressed)
		{
			boolean helpMode = !selected;
			selected = helpMode;
			for (GuiButton button : buttonList)
			{
				if (button != this && button instanceof GuiButtonBase)
					((GuiButtonBase) button).setHelpMode(helpMode);
			}
		}
		return pressed;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		int x = this.x + 6;
		