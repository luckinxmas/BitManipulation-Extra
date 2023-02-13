package com.phylogeny.extrabitmanipulation.client.gui.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;

import org.apache.commons.lang3.tuple.Pair;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonBase;
import com.phylogeny.extrabitmanipulation.client.render.RenderState;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorMovingPart;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenInventoryGui;

public class GuiButtonArmorSlots extends GuiButtonBase
{
	private GuiContainer gui;
	private int mouseInitialX, offsetX, offsetY, posX, posY;
	
	public GuiButtonArmorSlots(GuiContainer gui, String buttonText)
	{
		super(384736845, 0, 0, 12, 10, buttonText, "");
		setHoverHelpText("While holding SHIFT + CONTROL + ALT:\n" + GuiChiseledArmor.getPointSub("1) ") +
				"Click & drag to change position.\n" + GuiChiseledArmor.getPointSub("2) ") + "Press R to reset position.");
		this.gui = gui;
		setPosition();
	}
	
	public void setPosition()
	{
		resetOffsets();
		Pair<Integer, Integer> pos = BitToolSettingsHelper.getArmorButtonPosition();
		posX = pos.getLeft();
		posY = pos.getRight();
		setPosisionAbsolute();
	}
	
	public static boolean shouldMoveButton()
	{
		return GuiScreen.isShiftKeyDown() && GuiScreen.isCtrlKeyDown() && GuiScreen.isAltKeyDown();
	}
	
	private void setPosisionAbsolute()
	{
		x = gui.getGuiLeft() + posX;
		y = gui.getGuiTop() + posY;
	}
	
	private void resetOffsets()
	{
		offsetX = offsetY = mouseInitialX = 0;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		if (ClientHelper.getPlayer().capabilities.isCreativeMode)
		{
			visible = false;
			return;
		}
		setPosisionAbsolute();
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		RenderHelper.enableGUIS