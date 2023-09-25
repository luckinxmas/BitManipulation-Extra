package com.phylogeny.extrabitmanipulation.packet;

import io.netty.buffer.ByteBuf;

import java.util.Map;

import mod.chiselsandbits.api.IBitBrush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;

public class PacketOverwriteStackBitMappings extends PacketBitMapIO
{
	private Map<IBlockState, IBitBrush> bitMap;
	
	public PacketOverwriteStackBitMappings() {}
	
	public PacketOverwriteStackBitMappings(Map<IBlockState, IBitBrush> bitMap, String nbtKey, boolean saveStatesById)
	{
		super(nbtKey, saveStatesById);
		this.bitMap = bit