package com.phylogeny.extrabitmanipulation.proxy;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.phylogeny.extrabitmanipulation.client.ClientEventHandler;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.render.RenderEntityBit;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ModelRegistration;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.RenderLayersExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.SoundsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;

public class ProxyClient extends ProxyCommon
{
	
	@Override
	public void preinit(FMLPreInitializationEvent event)
	{
		super.preinit(event);
		ReflectionExtraBitManipulation.initReflectionFieldsClient();
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		MinecraftForge.EVENT_BUS.register(new SoundsExtraBitManipulation());
		MinecraftForge.EVENT_BUS.register(new ModelRegistration());
		RenderingRegistry.registerEntityRenderingHandler(EntityBit.class, new IRenderFactory<EntityBit>()
		{
			@Override
			public Render<EntityBit> createRenderFor(RenderManager manager)
			{
				return new RenderEntityBit(manager);
			}
		});
	}
	
	@Override
	public void init()
	{
		super.init();
		KeyBindingsExtraBitManipulation.init();
		FMLInterModComms.sendMessage(ChiselsAndBitsReferences.MOD_ID, "initkeybindi