
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
	
	CONTROL("Control", Keyboard.KEY_NONE, true)
	{
		@Override
		public boolean isKeyDown()
		{
			return isKeyDown(GuiScreen.isCtrlKeyDown());
		}
		
		@Override
		public boolean isActive()
		{
			ItemStack stack = getHeldItemMainhandSafe();
			return ItemStackHelper.isSculptingToolStack(stack) || ItemStackHelper.isModelingToolStack(stack) || ItemStackHelper.isChiseledArmorStack(stack);
		}
	},
	
	ALT("Alt", Keyboard.KEY_X, false)
	{
		@Override
		public boolean isKeyDown()
		{
			return isKeyDown(GuiScreen.isAltKeyDown());
		}
		
		@Override
		public boolean isActive()
		{
			ItemStack stack = getHeldItemMainhandSafe();
			return ItemStackHelper.isSculptingToolStack(stack) || ItemStackHelper.isChiseledArmorStack(stack);
		}
		
		@Override
		public String getText()
		{
			return keyBinding.getKeyCode() == Keyboard.KEY_NONE ? description.toUpperCase() : ("[" + keyBinding.getDisplayName() + "]");
		}
	};
	
	protected KeyBinding keyBinding;
	protected String description = "";
	private boolean anyConflicts;
	
	private KeyBindingsExtraBitManipulation(String description, int defaultKeyCode)
	{
		this(description, defaultKeyCode, false);
	}
	
	private KeyBindingsExtraBitManipulation(String description, int defaultKeyCode, boolean anyConflicts)
	{
		this.description = description;
		this.anyConflicts = anyConflicts;
		keyBinding = new KeyBinding("keybinding." + Reference.MOD_ID + "." + description.toLowerCase(),
				this, getModifier(), defaultKeyCode, "itemGroup." + Reference.MOD_ID);
	}
	
	public boolean isKeyDown()
	{
		return getKeyBinding().isKeyDown();
	}
	
	protected boolean isKeyDown(boolean defaultCheck)
	{
		return getKeyBinding().getKeyCode() == Keyboard.KEY_NONE ? defaultCheck : getKeyBinding().isKeyDown();
	}
	
	public static void init()
	{
		for (KeyBindingsExtraBitManipulation keyBinding : values())
			keyBinding.registerKeyBinding();
	}
	
	protected KeyModifier getModifier()
	{
		return KeyModifier.NONE;
	}
	
	private void registerKeyBinding()
	{
		ClientRegistry.registerKeyBinding(keyBinding);
	}
	
	public String getText()
	{
		return keyBinding.isSetToDefaultValue() ? description.toUpperCase() : ("[" + keyBinding.getDisplayName() + "]");
	}
	
	public KeyBinding getKeyBinding()
	{
		return keyBinding;
	}
	
	@Override
	public boolean conflicts(IKeyConflictContext other)
	{
		return conflictsInGame(other) || other == SHIFT || other == CONTROL || (anyConflicts && (other == ALT || other == OPEN_BIT_MAPPING_GUI));
	}
	
	protected boolean conflictsInGame(IKeyConflictContext other)
	{
		return other == this || other == KeyConflictContext.IN_GAME;
	}
	
	private static ItemStack getHeldItemMainhandSafe()
	{
		return ClientHelper.getPlayer() == null ? ItemStack.EMPTY : ClientHelper.getHeldItemMainhand();
	}
	
}