package com.phylogeny.extrabitmanipulation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ClientHelper
{
	
	private static Minecraft getMinecraft()
	{
		return Minecraft.getMinecraft();
	}
	
	public static IThreadListener getThreadListener()
	{
		return getMinecraft();
	}
	
	public static World getWorld()
	{
		return getMinecraft().world;
	}
	
	public static EntityPlayer getPlayer()
	{
		return getMinec