package com.phylogeny.extrabitmanipulation.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.GuiHelper;

public class GuiButtonSelectTextured extends GuiButtonSelect
{
	private ResourceLocation texture;
	private float rightOffsetX;
	private boolean shiftRight;
	private static int scaleFactor;
	
	public GuiButtonSelectTextured(int buttonId, int x, int y, int width, int height, String text,
			String hoverText, int colorFirst, int colorSecond, ResourceLocation texture)
	{
		super(buttonId, x, y, width, height, text, hoverText, colorFirst, colorSecond);
		this.texture = texture;
		scaleFactor = GuiHelper.getScaleFactor();
	}
	
	public void setRightOffsetX(float textureOffsetX, boolean shiftRight)
	{
		this.rightOffsetX = textureOffsetX;
		this.shiftRight = shiftRight;
	}
	
	private void shiftRight()
	{
		if (shiftRight)
			GlStateManager.translate(rightOffsetX, 0, 0);
	}
	
	@Override
	protected void d