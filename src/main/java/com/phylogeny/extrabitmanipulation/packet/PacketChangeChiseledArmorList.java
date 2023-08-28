package com.phylogeny.extrabitmanipulation.packet;

import javax.annotation.Nullable;

import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.GuiHelper;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiChiseledArmor;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.RenderLayersExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public abstract class PacketChangeChiseledArmorList extends PacketArmorSlotInt
{
	protected int armorItemIndex, selectedEntry;
	protected boolean refreshLists;
	protected NBTTagCompound nbt = new NBTTagCompound();
	
	public PacketChangeChiseledArmorList() {}
	
	public PacketChangeChiseledArmorList(NBTTagCompound nbt, ArmorType armorType, int indexArmorSet,
			int partIndex, int armorItemIndex, int selectedEntry, boolean refreshLists, @Nullable EntityPlayer player)
	{
		super(armorType, indexArmorSet, partIndex);
		this.nbt = nbt;
		this.armorItemIndex = armorItemIndex;
		this.selectedEntry = selectedEntry;
		this.refreshLists = refreshLists;
		if (indexArmorSet > 0 && player instanceof EntityPlayerMP)
		{
			IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player);
			if (cap != null)
				cap.markSlotDirty(armorType.getSlotIn