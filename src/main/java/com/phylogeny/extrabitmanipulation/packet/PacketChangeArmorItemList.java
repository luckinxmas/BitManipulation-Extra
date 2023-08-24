package com.phylogeny.extrabitmanipulation.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.armor.ArmorItem;
import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.container.ContainerPlayerInventory;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;

public class PacketChangeArmorItemList extends PacketChangeChiseledArmorList
{
	private ItemStack stack;
	private ListOperation listOperation;
	
	public PacketChangeArmorItemList() {}
	
	public PacketChangeArmorItemList(ArmorType armorType, int indexArmorSet, int partIndex, int armorItemIndex,
			int selectedEntry, ListOperation listOperation, ItemStack stack, NBTTagCompound glOperationsNbt, boolean refreshLists, EntityPlayer player)
	{
		super(glOperationsNbt, armorType, indexArmorSet, partIndex, armorItemIndex, selectedEntry, refreshLists, player);
		this.listOperation = listOperation;
		this.stack = stack;
	}
	
	@Override
	public void toBytes(ByteBuf buffer)
	{
		super.toBytes(buffer);
		buffer.writeInt(listOperation.ordinal());
		ByteBufUtils.writeItemStack(buffer, stack);
	}
	
	@Override
	public void fromBytes(ByteBuf buffer)
	{
		super.fromBytes(buffer);
		listOperation = ListOperation.values()[buffer.readInt()];
		stack = ByteBufUtils.readItemStack(buffer);
	}
	
	public static class Handler implements IMessageHandler<PacketChangeArmorItemList, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketChangeA