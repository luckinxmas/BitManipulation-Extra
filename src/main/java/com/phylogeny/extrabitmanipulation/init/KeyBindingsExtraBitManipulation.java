
package com.phylogeny.extrabitmanipulation.init;

import mod.chiselsandbits.api.ItemType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import org.lwjgl.input.Keyboard;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;

public enum KeyBindingsExtraBitManipulation implements IKeyConflictContext
{
	EDIT_DESIGN("design", Keyboard.KEY_R)
	{
		@Override
		public boolean isActive()
		{
			return ItemStackHelper.isDesignStack(getHeldItemMainhandSafe());
		}
		
		@Override
		public boolean conflicts(IKeyConflictContext other)
		{
			return conflictsInGame(other);
		}
	},
	
	THROW_BIT("throw.bit", Keyboard.KEY_R)
	{
		@Override
		public boolean isActive()
		{
			return ChiselsAndBitsAPIAccess.apiInstance.getItemType(getHeldItemMainhandSafe()) == ItemType.CHISLED_BIT;
		}
		
		@Override
		public boolean conflicts(IKeyConflictContext other)
		{
			return conflictsInGame(other);
		}
	},
	
	THROW_BIT_BIT_BAG("throw.bit.bitbag", Keyboard.KEY_R)
	{
		@Override
		public boolean isActive()
		{
			return ChiselsAndBitsAPIAccess.apiInstance.getItemType(getHeldItemMainhandSafe()) == ItemType.BIT_BAG;
		}
		
		@Override
		public boolean conflicts(IKeyConflictContext other)
		{
			return conflictsInGame(other);
		}
	},
	
	OPEN_CHISELED_ARMOR_GUI("chiseledarmor", Keyboard.KEY_G)
	{
		@Override
		public boolean isActive()
		{
			return true;
		}
		
		@Override
		public boolean conflicts(IKeyConflictContext other)
		{
			return conflictsInGame(other);
		}
	},
	
	OPEN_CHISELED_ARMOR_SLOTS_GUI("chiseledarmor.slots", Keyboard.KEY_H)
	{
		@Override
		public boolean isActive()
		{
			return true;
		}
		
		@Override
		public boolean conflicts(IKeyConflictContext other)
		{
			return conflictsInGame(other);
		}
	},
	
	OPEN_BIT_MAPPING_GUI("bitmapping", Keyboard.KEY_R, false)
	{
		@Override
		public boolean isActive()
		{
			return ItemStackHelper.isModelingToolStack(getHeldItemMainhandSafe());
		}
	},
	
	SHIFT("Shift", Keyboard.KEY_NONE, true)
	{
		@Override
		public boolean isKeyDown()
		{
			return isKeyDown(GuiScreen.isShiftKeyDown());
		}
		
		@Override
		public boolean isActive()
		{
			ItemStack stack = getHeldItemMainhandSafe();
			return ItemStackHelper.isSculptingToolStack(stack) || ItemStackHelper.isModelingToolStack(stack) || ItemStackHelper.isBitWrenchStack(stack);
		}
	},
	