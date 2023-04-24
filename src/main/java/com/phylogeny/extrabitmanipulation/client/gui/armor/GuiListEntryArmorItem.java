package com.phylogeny.extrabitmanipulation.client.gui.armor;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.armor.ArmorItem;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.packet.PacketCursorStack;

public class GuiListEntryArmorItem extends GuiListEntryChiseledArmor<ArmorItem>
{
	private boolean slotHovered;
	
	public GuiListEntryArmorItem(GuiListChiseledArmor<ArmorItem> listChiseledArmor, ArmorItem armorItem)
	{
		super(listChiseledArmor, armorItem);
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks)
	{
		slotHovered = mouseX > x + 4 && mouseX < x + 23 && mouseY > y && mouseY < y + 19;
		x += 5;
		y += 1;
		RenderHelper.enableGUIStandardItemLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
		ClientHelper.bindTexture(GuiChiseledArmor.TEXTURE_GUI);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 230, 18, 18,