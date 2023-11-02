package com.phylogeny.extrabitmanipulation.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemBitWrench;

public class PacketUseWrench extends PacketBlockInteraction implements IMessage
{
	private boolean bitRequirement, invertDirection;
	
	public PacketUseWrench() {}
	
	public PacketUseWrench(BlockPos pos, EnumFacing side, boolean bitRequirement, boolean invertDirection)
	{
		super(pos, side, new Vec3d(0, 0, 0));
		this.bitRequirement