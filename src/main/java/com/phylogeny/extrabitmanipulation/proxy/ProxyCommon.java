package com.phylogeny.extrabitmanipulation.proxy;

import mod.chiselsandbits.core.ChiselsAndBits;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsEventHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsStorage;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.gui.GuiBitMapping;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiChiseledArmor;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiInventoryArmorSlots;
import com.phylogeny.extrabitmanipulation.config.ConfigHandlerExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.container.ContainerHeldItem;
import com.phylogeny.extrabitmanipulation.container.ContainerPlayerArmorSlots;
import com.phylogeny.extrabitmanipulation.container.ContainerPlayerInventory;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.BlocksExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.PacketRegistration;
import com.phylogeny.extrabitmanipulation.init.RecipesExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.BaublesReferences;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import com.phylogeny.extrabitmanipulation.reference.GuiIDs;
import com.phylogeny.extrabitmanipulation.reference.JeiReferences;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.Reference;

public class ProxyCommon implements IGuiHandler
{
	
	public void preinit(FMLPreInitializationEvent event)
	{
		BlocksExtraBitManipulation.blocksInit();
		ItemsExtraBitManipulation.itemsInit(event);
		BaublesReferences.isLoaded = Loader.isModLoaded(BaublesReferences.MOD_ID);
		JeiReferences.isLoaded = Loader.isModLoaded(JeiReferences.MOD_ID);
		MorePlayerModelsReference.isLoaded = Loader.isModLoaded(MorePlayerModelsReference.MOD_ID);
		CustomNPCsReferences.isLoaded = Loader.isModLoaded(CustomNPCsReferences.MOD_ID);
		ConfigHandlerExtraBitManipulation.setUpConfigs(event.getModConfigurationDirectory());
		MinecraftForge.EVENT_BUS.register(new ConfigHandlerExtraBitManipulation());
		MinecraftForge.EVENT_BUS.register(new ItemsExtraBitManipulation());
		MinecraftForge.EVENT_BUS.register(new BlocksExtraBitManipulation());
		MinecraftForge.EVENT_BUS.register(new RecipesExtraBitManipulation());
		MinecraftForge.EVENT_BUS.register(new ChiseledArmorSlotsEventHandler());
		CapabilityManager.INSTANCE.register(IChiseledArmorSlotsHandler.class, new ChiseledArmorSlotsStorage(), ChiseledArmorSlotsHandler::new);
		PacketRegistration.registerPackets();
		ResourceLocation name = new ResourceLocation(Reference.MOD_ID, "entity_bit");
		EntityRegistry.registerModEntity(name, EntityBit.class, name.toString(), 0, ExtraBitManipulation.instance, 64, 3, false);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ChiselsAndBits.getItems().itemBlockBit, new BehaviorProjectileDispense()
		{
			@Override
			protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack s