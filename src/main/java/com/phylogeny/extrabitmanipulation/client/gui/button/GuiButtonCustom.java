package com.phylogeny.extrabitmanipulation.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButtonCustom extends GuiButtonBase
{
	protected float textOffsetX, textOffsetY;
	
	public GuiButtonCustom(int buttonId, int x, int y, int widthIn, int heightIn, String text, String hoverText)
	{
		super(buttonId, x, y, widthIn, heightIn, text, hoverText);
	}
	
	public void setTextOffsetX(float textOffsetX)
	{
		this.textOffsetX = textOffsetX;
	}
	
	public void setTextOffsetY(float textOffsetY)
	{
		this.textOffsetY = textOffsetY;
	}
	
	protected void drawCustomRect() {}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		if (!visible)
			return;
		
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		GlStateManager.enableBlend(