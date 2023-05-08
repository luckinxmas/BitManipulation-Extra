package com.phylogeny.extrabitmanipulation.client.gui.button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.SoundEvent;

import com.phylogeny.extrabitmanipulation.init.SoundsExtraBitManipulation;

public class GuiButtonBase extends GuiButton
{
	public boolean selected;
	private boolean silent, helpMode;
	private List<String> hoverText, hoverTextSelected, hoverHelpText;
	private SoundEvent soundSelect, soundDeselect;
	
	public GuiButtonBase(int buttonId, int x, int y, int width, int height, String text, String hoverText)
	{
		this(buttonId, x, y, width, height, text, hoverText, null, null);
	}
	
	public GuiButtonBase(int buttonId, int x, int y, int widthIn, int heightIn, String text,
			String hoverText, @Nullable SoundEvent soundSelect, @Nullable SoundEvent soun