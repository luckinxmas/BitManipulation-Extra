
package com.phylogeny.extrabitmanipulation.armor.capability;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.armor.ModelPartConcealer;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.packet.PacketSyncArmorSlot;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.ItemStackHandler;

public class ChiseledArmorSlotsHandler extends ItemStackHandler implements ICapabilityProvider, IChiseledArmorSlotsHandler
{
	public static final int COUNT_TYPES = 4;
	public static final int COUNT_SETS = 4;
	public static final int COUNT_SLOTS_TOTAL = COUNT_TYPES * COUNT_SETS;
	private boolean[] syncedSlots, hasArmorSet, hasArmorType;
	private boolean hasArmor;
	private ModelPartConcealer modelPartConcealer;
	
	@CapabilityInject(IChiseledArmorSlotsHandler.class)
	public static final Capability<IChiseledArmorSlotsHandler> ARMOR_SLOTS_CAP = null;
	
	public ChiseledArmorSlotsHandler()
	{
		super(COUNT_SLOTS_TOTAL);
		syncedSlots = new boolean[COUNT_SLOTS_TOTAL];
		hasArmorSet = new boolean[COUNT_SETS];
		hasArmorType = new boolean[COUNT_TYPES];
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		 return ARMOR_SLOTS_CAP != null && capability == ARMOR_SLOTS_CAP;
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		return capability == ARMOR_SLOTS_CAP ? ARMOR_SLOTS_CAP.<T>cast(this) : null;
	}
	
	public static IChiseledArmorSlotsHandler getCapability(EntityPlayer player)
	{
		return player.getCapability(ARMOR_SLOTS_CAP, null);
	}
	
	@Override
	public void syncAllSlots(EntityPlayer player)
	{
		Collection<EntityPlayer> players = null;
		for (int i = 0; i < COUNT_SLOTS_TOTAL; i++)
		{
			if (syncedSlots[i])
				continue;
			
			if (players == null)
			{
				players = new HashSet<>();
				players.addAll(((WorldServer) player.world).getEntityTracker().getTrackingPlayers(player));
				players.add(player);
			}
			for (EntityPlayer player2 : players)
				ExtraBitManipulation.packetNetwork.sendTo(new PacketSyncArmorSlot(player.getUniqueID(), getStackInSlot(i), i), (EntityPlayerMP) player2);
			
			syncedSlots[i] = true;
		}
	}
	
	@Override
	public void markAllSlotsDirty()
	{
		syncedSlots = new boolean[COUNT_SLOTS_TOTAL];
	}
	
	@Override
	public void markSlotDirty(int index)
	{
		syncedSlots[index] = false;
	}
	
	@Override
	@Nullable
	public ModelPartConcealer getAndApplyModelPartConcealer(ModelBiped model)
	{
		return modelPartConcealer == null ? null : modelPartConcealer.copy().applyToModel(model);
	}
	
	@Override
	public void onContentsChanged(int slot)
	{
		markSlotDirty(slot);
		int index = slot / COUNT_TYPES;
		hasArmorSet[index] = false;
		int start = index * COUNT_TYPES;
		for (int i = start; i < start + COUNT_TYPES; i++)
		{
			if (!stacks.get(i).isEmpty())
			{
				hasArmorSet[index] = true;
				break;
			}
		}
		hasArmor = false;
		for (boolean setHasArmor : hasArmorSet)
		{
			if (setHasArmor)
			{
				hasArmor = true;
				break;
			}
		}
		index = slot % COUNT_TYPES;
		hasArmorType[index] = !stacks.get(index).isEmpty();
		ModelPartConcealer modelPartConcealer = new ModelPartConcealer();
		for (int i = 0; i < COUNT_SLOTS_TOTAL && !modelPartConcealer.isFull(); i++)
		{
			ItemStack stack = stacks.get(i);
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt == null)
				continue;
			
			modelPartConcealer.merge(ModelPartConcealer.loadFromNBT(nbt));
		}
		this.modelPartConcealer = !modelPartConcealer.isEmpty() ? modelPartConcealer : null;
	}
	
	public static int findNextArmorSetIndex(int startIndex)
	{
		int indexNext = startIndex;
		do
		{
			indexNext = (indexNext + 1) % (ChiseledArmorSlotsHandler.COUNT_TYPES + 1);
			if (setHasArmor(indexNext))
				return indexNext;
		}
		while (indexNext != startIndex);
		return -1;
	}
	
	public static boolean setHasArmor(int index)
	{
		for (ArmorType armorType : ArmorType.values())
		{
			if (ItemStackHelper.isChiseledArmorStack(ItemStackHelper.getChiseledArmorStack(ClientHelper.getPlayer(), armorType, index)))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean hasArmor()
	{
		return hasArmor;
	}
	
	@Override
	public boolean hasArmorSet(int indexSet)
	{
		return hasArmorSet[indexSet];
	}
	
	@Override
	public boolean hasArmorType(int indexType)
	{
		return hasArmorType[indexType];
	}
	
	@Override
	public int getSlotLimit(int slot)
	{
		return 1;
	}
	
	@Override
	public void setSize(int size)
	{
		super.setSize(COUNT_SLOTS_TOTAL);
	}
	
	public static boolean isItemValid(int slot, ItemStack stack)
	{
		return ItemStackHelper.isChiseledArmorStack(stack) && ((ItemChiseledArmor) stack.getItem()).armorType.ordinal() == slot % COUNT_TYPES
				&& ItemStackHelper.isChiseledArmorNotEmpty(stack);
	}
	
	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack)
	{
		if (stack.isEmpty() || isItemValid(slot, stack))
			super.setStackInSlot(slot, stack);
	}
	
	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
	{
		return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
	}
	
}