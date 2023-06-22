package com.phylogeny.extrabitmanipulation.container;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;

public class ContainerPlayerArmorSlots extends ContainerPlayer
{
	
	public ContainerPlayerArmorSlots(InventoryPlayer playerInventory, boolean localWorld, EntityPlayer player)
	{
		super(playerInventory, localWorld, player);
		inventorySlots.get(inventorySlots.size() - 1).xPos += 18;
		IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player);
		for (int i = 0; i < ChiseledArmorSlotsHandler.COUNT_SETS; i++)
		{
			for (int j = 0; j < ChiseledArmorSlotsHandler.COUNT_TYPES; j++)
			{
				SlotChiseledArmor slot = new SlotChiseledArmor(cap, i * ChiseledArmorSlotsHandler.COUNT_TYPES + j, 77 + 18 * i + (i == 0 ? 0 : 21), 8 + j * 18);
				addSlotToContainer(slot);
			}
		}
		
		for (int i = 0; i < inventorySlots.size(); i++)
		{
			Slot slot = inventorySlots.get(i);
			if (slot.inventory instanceof InventoryCrafting || slot.inventory instanceof InventoryCraftResult)
			{
				SlotNull slotNull = new SlotNull();
				slotNull.slotNumber = slot.slotNumber;
				inventorySlots.set(i, slotNull);
			}
		}
	}
	
	public class SlotNull extends Slot
	{
		
		public SlotNull()
		{
			super(new InventoryBasic("", false, 1), 0, -10000, -10000);
		}
		
		@Override
		public boolean canTakeStack(EntityPlayer playerIn)
		{
			return false;
		}
		
		@Override
		public boolean isEnabled()
		{
			return false;
		}
		
		@Override
		publ