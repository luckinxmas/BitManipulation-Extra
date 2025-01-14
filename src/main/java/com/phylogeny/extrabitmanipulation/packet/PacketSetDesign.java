
package com.phylogeny.extrabitmanipulation.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;

public class PacketSetDesign implements IMessage
{
	private ItemStack stack;
	
	public PacketSetDesign() {}
	
	public PacketSetDesign(ItemStack stack)
	{
		this.stack = stack;
	}
	
	@Override
	public void toBytes(ByteBuf buffer)
	{
		ByteBufUtils.writeItemStack(buffer, stack);
	}
	
	@Override
	public void fromBytes(ByteBuf buffer)
	{
		stack = ByteBufUtils.readItemStack(buffer);
	}
	
	public static class Handler implements IMessageHandler<PacketSetDesign, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetDesign message, final MessageContext ctx)
		{
			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
			mainThread.addScheduledTask(new Runnable()
			{
				@Override
				public void run()
				{
					BitInventoryHelper.setHeldDesignStack(ctx.getServerHandler().player, message.stack);
				}
			});
			return null;
		}
		
	}
	
}