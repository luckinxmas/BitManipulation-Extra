package com.phylogeny.extrabitmanipulation.api.jei.icon;

import mezz.jei.api.gui.IDrawableStatic;
import net.minecraft.client.Minecraft;

public abstract class CategoryIconBase implements IDrawableStatic
{
	protected int width, height;
	
	public CategoryIconBase(int width, int height)
	{
		this.wi