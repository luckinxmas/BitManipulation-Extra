package com.phylogeny.extrabitmanipulation.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;

public class PacketPlaceEntityBit implements IMessage
{
	private ItemStack bitStack;
	private BlockPos pos;
	private Vec3d hitVec;
	private EnumFacing sideHit;
	
	public PacketPlaceEntityBit() {}
	
	public PacketPlaceEntityBit(ItemStack bitStack, BlockPos pos, RayTraceRe