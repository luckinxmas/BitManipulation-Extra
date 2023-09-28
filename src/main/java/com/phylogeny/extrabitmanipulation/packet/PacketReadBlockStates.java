package com.phylogeny.extrabitmanipulation.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelReadData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;

public class PacketReadBlockStates extends PacketBlockInteraction implements IMessage
{
	private Vec3i drawnStartPoint;
	private ModelReadData modelingData = new ModelReadData();
	
	public PacketReadBlockStates() {}
	
	public PacketReadBlockStates(BlockPos pos, Vec3d hit, Vec3i drawnStartPoint, ModelReadData modelingData)
	{
		super(pos, EnumFacing.UP, hit);
		this.drawnStartPoint = drawnStartPoint;
		this.modelingData = modelingData;
	}
	
	@Override
	public void toBytes(ByteBuf buffer)
	{
		super.toBytes(buffer);
		if (BitIOHelper.notNullToBuffer(buffer, drawnStartPoint))
		{
			buffer.writeInt