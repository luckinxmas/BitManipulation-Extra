package com.phylogeny.extrabitmanipulation.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemBitWrench;

public class PacketCycleBitWrenchMode extends PacketBoolean
{
	public PacketCycleBitWrenchMode() {}
	
	public PacketCycleBitWrenchMode(boolean forward)
	{
		super(forward);
	}
	
	public static class Handler implements IMessageHandler<PacketCycleBitWrenchMode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketCycleBitWrenchMode message, final MessageContext ctx)
		{
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.worl