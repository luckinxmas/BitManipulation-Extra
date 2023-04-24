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
	
	public GuiListEntryArmorItem(GuiListChiseledArmor<ArmorItem> listChiseledArmor, Arm