package com.phylogeny.extrabitmanipulation.packet;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class PacketInt implements IMessage
{
	protected int value;
	
	public PacketInt() {}
	
	public PacketIn