package com.phylogeny.extrabitmanipulation.client.gui.button;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.GuiHelper;
import com.phylogeny.extrabitmanipulation.init.SoundsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.Reference;

public class GuiButtonTextured extends GuiButtonBase
{
	private static final ResourceLocation BOX_CHECKED = new ResourceLocation(Reference.MOD_ID, "textures/guis/box_checked.png");
	private static final ResourceLocation BOX_UNCHECKED = new ResourceLocation(Reference.MOD_ID, "textures/guis/box_unchecked.png");
	private ResourceLocation selectedTexture,