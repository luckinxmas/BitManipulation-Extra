package com.phylogeny.extrabitmanipulation.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;

public class PacketSetCollectionBox extends PacketBlockInteraction
{
	private float playerYaw;
	private boolean useBitGrid;
	private EnumFacing facingBox;
	
	public PacketSetCollectionBox() {}
	
	public PacketSetCollectionBox(float playerYaw, boolean useBitGrid, EnumFacing facingBox, BlockPos pos, EnumFacing facingPlacement, Vec3d hit