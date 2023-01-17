
package com.phylogeny.extrabitmanipulation.client.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiSlider.ISlider;
import net.minecraftforge.fml.client.config.GuiUtils;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonBase;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonSelect;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemBitWrench;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.shape.Shape;

public class GuiBitToolSettingsMenu extends GuiScreen implements ISlider
{
	private List<ButtonsSetting> buttonsSettingList = Lists.<ButtonsSetting>newArrayList();
	private List<SliderSetting> sliderSettingList = Lists.<SliderSetting>newArrayList();
	private int buttonCount, lineCount;
	private boolean closing;
	private float visibility;
	private Stopwatch timer = Stopwatch.createStarted();
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void initGui()
	{
		ItemStack stack = ClientHelper.getHeldItemMainhand();
		if (stack.isEmpty())
			return;
		
		if (ItemStackHelper.isBitWrenchStack(stack))
		{
			lineCount = 1;
			addButtonsSettings(new ButtonsSetting.WrenchMode(), "Mode", ItemBitWrench.MODE_TEXT);
		}
		else if (ItemStackHelper.isModelingToolStack(stack))
		{
			lineCount = 3;
			addButtonsSettings(new ButtonsSetting.ModelAreaMode(), "Area Mode", ItemModelingTool.AREA_MODE_TITLES);
			String[] snapTexts = ItemModelingTool.SNAP_MODE_TITLES;
			String[] snapTextsNew = new String[snapTexts.length];
			for (int i = 0; i < snapTexts.length; i++)
				snapTextsNew[i] = snapTexts[i].replace("Snap-to-Chunk ", "");
			
			addButtonsSettings(new ButtonsSetting.ModelSnapMode(), "Chunk Snap", snapTextsNew);
			addButtonsSettings(new ButtonsSetting.ModelGuiOpen(), "Open GUI", "On Read", "Off");
		}
		else if (ItemStackHelper.isSculptingToolStack(stack))