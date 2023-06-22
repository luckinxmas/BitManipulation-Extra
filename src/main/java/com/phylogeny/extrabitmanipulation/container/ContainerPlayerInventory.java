package com.phylogeny.extrabitmanipulation.container;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPlayerInventory extends Container
{
	
	public ContainerPlayerInventory(EntityPlayer player, int startX, int startY)
	{
		for (int i1 = 0; i1 < 9; ++i1)
		{
			addSlotToContainer(new Slot(player.inventory,