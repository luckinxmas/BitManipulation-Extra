package com.phylogeny.extrabitmanipulation.armor.capability;

import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Maps;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiButtonArmorSlots;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiInventoryArmorSlots;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.Reference;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandReplaceItem;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ChiseledArmorSlotsEventHandler
{
	private static final Map<String, Integer> COMMAND_VANITY_SLOTS = Maps.newHashMap();
	
	static
	{
		for (int i = 0; i < ChiseledArmorSlotsHandler.COUNT_SLOTS_TOTAL; i++)
		{
			COMMAND_VANITY_SLOTS.put("slot.vanity.set" + i / ChiseledArmorSlotsHandler.COUNT_TYPES +
					"." + EntityEquipmentSlot.values()[5 - i % ChiseledArmorSlotsHandler.COUNT_TYPES].toString().toLowerCase(), i);
		}
	}
	
	public static void addCommandTabCompletions()
	{
		ReflectionExtraBitManipulation.addShortcutsToCommandReplace