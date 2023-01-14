
package com.phylogeny.extrabitmanipulation.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.gui.GuiBitToolSettingsMenu.GuiButtonSetting;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.packet.PacketSetWrechMode;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;

public abstract class ButtonsSetting
{
	protected List<GuiButtonSetting> buttons;
	
	public ButtonsSetting()
	{
		buttons = new ArrayList<GuiButtonSetting>();
	}
	
	public List<GuiButtonSetting> getButtons()
	{
		return buttons;
	}
	
	public void addButton(GuiButtonSetting button)
	{
		button.selected = buttons.size() == getValue();
		buttons.add(button);
	}
	
	public void initButtons()
	{
		for (GuiButtonSetting button : buttons)
			button.setButtonList(buttons);
	}
	
	protected int getValue()
	{
		return 0;
	}
	
	protected abstract void setValue(EntityPlayer player, int value);
	
	public void setValueIfDiffrent()
	{
		int value = buttons.indexOf(getTargetButton());
		if (value != getValue())
			setValue(ClientHelper.getPlayer(), value);
	}
	
	protected GuiButtonSetting getTargetButton()
	{
		GuiButtonSetting buttonTarget = null;
		for (GuiButtonSetting button : buttons)
		{
			if (button.isMouseOver())
				buttonTarget = button;
		}
		if (buttonTarget == null)
		{
			for (GuiButtonSetting button : buttons)
			{
				if (button.selected)
					buttonTarget = button;
			}
		}
		return buttonTarget;
	}
	
	protected NBTTagCompound getHeldStackNBT()
	{
		return ItemStackHelper.getNBTOrNew(ClientHelper.getHeldItemMainhand());
	}
	
	private static ItemSculptingTool getSculptingTool()
	{
		ItemStack stack = ClientHelper.getHeldItemMainhand();
		return stack.isEmpty() ? null : (ItemSculptingTool) stack.getItem();
	}
	
	private static ItemChiseledArmor getChiseledArmor()
	{
		ItemStack stack = ClientHelper.getHeldItemMainhand();
		return stack.isEmpty() ? null : (ItemChiseledArmor) stack.getItem();
	}
	
	public static class WrenchMode extends ButtonsSetting
	{
		
		@Override
		protected int getValue()
		{
			return getHeldStackNBT().getInteger(NBTKeys.WRENCH_MODE);
		}
		
		@Override
		protected void setValue(EntityPlayer player, int value)
		{
			ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetWrechMode(value));
		}
		
	}
	
	public static class ModelAreaMode extends ButtonsSetting
	{
		
		@Override
		protected int getValue()
		{
			return BitToolSettingsHelper.getModelAreaMode(getHeldStackNBT());
		}
		
		@Override
		protected void setValue(EntityPlayer player, int value)
		{
			BitToolSettingsHelper.setModelAreaMode(player, player.getHeldItemMainhand(), value, Configs.modelAreaMode);
		}
		
	}
	
	public static class ModelSnapMode extends ButtonsSetting
	{
		
		@Override
		protected int getValue()
		{
			return BitToolSettingsHelper.getModelSnapMode(getHeldStackNBT());
		}
		
		@Override
		protected void setValue(EntityPlayer player, int value)
		{
			BitToolSettingsHelper.setModelSnapMode(player, player.getHeldItemMainhand(), value, Configs.modelSnapMode);