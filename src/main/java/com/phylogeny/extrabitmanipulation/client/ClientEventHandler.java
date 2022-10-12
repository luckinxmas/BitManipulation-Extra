package com.phylogeny.extrabitmanipulation.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Quadric;
import org.lwjgl.util.glu.Sphere;

import com.google.common.base.Stopwatch;
import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.armor.LayerChiseledArmor;
import com.phylogeny.extrabitmanipulation.armor.ModelPartConcealer;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.gui.GuiBitToolSettingsMenu;
import com.phylogeny.extrabitmanipulation.config.ConfigShapeRender;
import com.phylogeny.extrabitmanipulation.config.ConfigShapeRenderPair;
import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper;
import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper.ModelingBoxSet;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorBodyPartTemplateBoxData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorCollectionData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelReadData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.SculptingData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.RenderLayersExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.packet.PacketCollectArmorBlocks;
import com.phylogeny.extrabitmanipulation.packet.PacketCycleBitWrenchMode;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenBitMappingGui;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenChiseledArmorGui;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenInventoryGui;
import com.phylogeny.extrabitmanipulation.packet.PacketReadBlockStates;
import com.phylogeny.extrabitmanipulation.packet.PacketSculpt;
import com.phylogeny.extrabitmanipulation.packet.PacketSetCollectionBox;
import com.phylogeny.extrabitmanipulation.packet.PacketThrowBit;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import com.phylogeny.extrabitmanipulation.reference.Utility;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.api.ModKeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

public class ClientEventHandler
{
	private float millisecondsElapsed;
	private static Stopwatch timer;
	private Vec3d drawnStartPoint = null;
	private Vec3i drawnStartPointModelingTool = null;
	private static final ResourceLocation ARROW_HEAD = new ResourceLocation(Reference.MOD_ID, "textures/overlays/arrow_head.png");
	private static final ResourceLocation ARROW_BIDIRECTIONAL = new ResourceLocation(Reference.MOD_ID, "textures/overlays/arrow_bidirectional.png");
	private static final ResourceLocation ARROW_CYCLICAL = new ResourceLocation(Reference.MOD_ID, "textures/overlays/arrow_cyclical.png");
	private static final ResourceLocation CIRCLE = new ResourceLocation(Reference.MOD_ID, "textures/overlays/circle.png");
	private static final ResourceLocation INVERSION = new ResourceLocation(Reference.MOD_ID, "textures/overlays/inversion.png");
	private static final int[] DIRECTION_FORWARD = new int[]{2, 0, 5, 4, 1, 3};
	private static final int[] DIRECTION_BACKWARD = new int[]{1, 4, 0, 5, 3, 2};
	private static final int[] AXIS_FORWARD = new int[]{2, 3, 4, 5, 0, 1};
	private static final int[] AXIS_BACKWARD = new int[]{4, 5, 0, 1, 2, 3};
	private static final int[] SHAPE_CURVED = new int[]{1, 2, 0, 0, 0, 0, 0};
//	private static final int[] SHAPE_FLAT = new int[]{3, 3, 3, 4, 5, 6, 3}; TODO
	private static final int[] SHAPE_FLAT = new int[]{3, 3, 3, 6, 3, 3, 3};
	private boolean keyThrowBitIsDown;
	private static double BOUNDING_BOX_OFFSET = 0.0020000000949949026D;
	private static Map<UUID, ItemStack[]> invisibleArmorMap = new HashMap<>();
	private static Map<UUID, ModelPartConcealer> concealedModelPartsMap = new HashMap<>();
	
	@SubscribeEvent
	public void registerTextures(@SuppressWarnings("unused") TextureStitchEvent.Pre event)
	{
		registerTexture(ARROW_HEAD);
		registerTexture(ARROW_BIDIRECTIONAL);
		registerTexture(ARROW_CYCLICAL);
		registerTexture(CIRCLE);
		registerTexture(INVERSION);
		RenderLayersExtraBitManipulation.clearRenderMaps();
	}
	
	private void registerTexture(ResourceLocation resourceLocation)
	{
		SimpleTexture texture = new SimpleTexture(resourceLocation);
		Minecraft.getMinecraft().renderEngine.loadTexture(resourceLocation, texture);
	}
	
	@SubscribeEvent
	public void clearDisplayListsMaps(@SuppressWarnings("unused") ClientConnectedToServerEvent event)
	{
		ClientHelper.getThreadListener().addScheduledTask(new Runnable()
		{
			@Override
			public void run()
			{
				RenderLayersExtraBitManipulation.clearRenderMaps();
			}
		});
	}
	
	@SubscribeEvent
	public void preventArmorAndPlayerModelPartRendering(RenderLivingEvent.Pre event)
	{
		boolean isPlayerModelAlt = LayerChiseledArmor.isPlayerModelAlt(event.getEntity(), event.getPartialRenderTick());
		Entity entity = isPlayerModelAlt ? Minecraft.getMinecraft().player : event.getEntity();
		if (!(entity instanceof EntityPlayer))
			return;
		
		EntityPlayer player = (EntityPlayer) entity;
		IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player);
		if (cap == null || !cap.hasArmor() || !cap.hasArmorSet(0))
			return;
		
		ItemStack[] armor = new ItemStack[4];
		NonNullList<ItemStack> armorInventory = player.inventory.armorInventory;
		boolean found = false;
		for (int i = 0; i < 4; i++)
		{
			ItemStack stack = armorInventory.get(i);
			ItemStack stackVanity = cap.getStackInSlot(3 - i);
			if (!stackVanity.isEmpty() && !stack.isEmpty())
			{
				armor[i] = stack;
				armorInventory.set(i, ItemStack.EMPTY);
				found = true;
			}
		}
		if (found)
			invisibleArmorMap.put(player.getUniqueID(), armor);
		
		ModelBase model = event.getRenderer().getMainModel();
		if (!(model instanceof ModelBiped))
			return;
		
		ModelPartConcealer modelPartConcealer = cap.getAndApplyModelPartConcealer((ModelBiped) model);
		if (modelPartConcealer != null && !modelPartConcealer.isEmpty())
		{
			concealedModelPartsMap.put(player.getUniqueID(), modelPartConcealer);
			RenderLayersExtraBitManipulation.forceUpdateModels(!isPlayerModelAlt);
		}
	}
	
	@SubscribeEvent
	public void preventArmorAndPlayerModelPartRendering(RenderLivingEvent.Post event)
	{
		boolean isPlayerModelAlt = LayerChiseledArmor.isPlayerModelAlt(event.getEntity(), event.getPartialRenderTick());
		Entity entity = isPlayerModelAlt ? Minecraft.getMinecraft().player : event.getEntity();
		if (!(entity instanceof EntityPlayer))
			return;
		
		EntityPlayer player = (EntityPlayer) entity;
		IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player);
		if (cap == null)
			return;
		
		ItemStack[] armor = invisibleArmorMap.get(player.getUniqueID());
		if (armor != null)
		{
			for (int i = 0; i < 4; i++)
			{
				if (armor[i] != null)
					player.inventory.armorInventory.set(i, armor[i]);
			}
			invisibleArmorMap.remove(player.getUniqueID());
		}
		ModelPartConcealer modelPartConcealer = concealedModelPartsMap.get(player.getUniqueID());
		if (modelPartConcealer == null)
			return;
		
		ModelBase model = event.getRenderer().getMainModel();
		if (model instanceof ModelBiped)
			modelPartConcealer.restoreModelPartVisiblity((ModelBiped) model);
		
		concealedModelPartsMap.remove(player.getUniqueID());
		RenderLayersExtraBitManipulation.forceUpdateModels(!isPlayerModelAlt);
	}
	
	@SubscribeEvent
	public void onKeyInput(@SuppressWarnings("unused") InputEvent.KeyInputEvent event)
	{
		if (timer == null)
			timer = Stopwatch.createStarted();
		
		ItemStack stack = ClientHelper.getHeldItemMainhand();
		keyThrowBitIsDown = (ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack) == ItemType.CHISLED_BIT ?
				KeyBindingsExtraBitManipulation.THROW_BIT : KeyBindingsExtraBitManipulation.THROW_BIT_BIT_BAG).isKeyDown();
		if (ChiselsAndBitsAPIAccess.apiInstance.getKeyBinding(ModKeyBinding.MODE_MENU).isKeyDown()
				|| KeyBindingsExtraBitManipulation.OPEN_BIT_MAPPING_GUI.isKeyDown()
				|| KeyBindingsExtraBitManipulation.EDIT_DESIGN.isKeyDown()
				|| KeyBindingsExtraBitManipulation.OPEN_CHISELED_ARMOR_GUI.isKeyDown()
				|| KeyBindingsExtraBitManipulation.OPEN_CHISELED_ARMOR_SLOTS_GUI.isKeyDown())
		{
			if (KeyBindingsExtraBitManipulation.OPEN_BIT_MAPPING_GUI.isKeyDown())
			{
				if (ItemStackHelper.isModelingToolStack(stack) && ItemStackHelper.hasKey(stack, NBTKeys.SAVED_STATES))
					openBitMappingGui();
			}
			else if (KeyBindingsExtraBitManipulation.EDIT_DESIGN.isKeyDown())
			{
				if (stack.hasTagCompound() && ItemStackHelper.isDesignStack(stack))
					openBitMappingGui();
			}
			else if (KeyBindingsExtraBitManipulation.OPEN_CHISELED_ARMOR_GUI.isKeyDown())
			{
				if (ChiseledArmorSlotsHandler.findNextArmorSetIndex(ChiseledArmorSlotsHandler.COUNT_SETS) >= 0)
					ExtraBitManipulation.packetNetwork.sendToServer(new PacketOpenChiseledArmorGui());
				else
					ClientHelper.printChatMessageWithDeletion("You must be wearing at least one piece of Chiseled Armor to open the Chiseled Armor GUI.");
			}
			else if (KeyBindingsExtraBitManipulation.OPEN_CHISELED_ARMOR_SLOTS_GUI.isKeyDown())
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketOpenInventoryGui(false));
			}
			else if (ItemStackHelper.isBitToolStack(stack) || ItemStackHelper.isChiseledArmorStack(stack))
			{
				Minecraft.getMinecraft().displayGuiScreen(new GuiBitToolSettingsMenu());
			}
		}
	}
	
	@SubscribeEvent
	public void throwBit(TickEvent.PlayerTickEvent event)
	{
		if (event.phase != Phase.START || !keyThrowBitIsDown)
			return;
		
		ItemStack stack = ClientHelper.getHeldItemMainhand();
		boolean isBit = ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack) == ItemType.CHISLED_BIT;
		if (!stack.isEmpty() && (ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack) == ItemType.BIT_BAG
				|| (timer.elapsed(TimeUnit.MILLISECONDS) > 150 && isBit)))
		{
			if (isBit)
				timer = Stopwatch.createStarted();
			
			ExtraBitManipulation.packetNetwork.sendToServer(new PacketThrowBit());
		}
	}
	
	private void openBitMappingGui()
	{
		ExtraBitManipulation.packetNetwork.sendToServer(new PacketOpenBitMappingGui());
	}

	@SubscribeEvent
	public void interceptMouseInput(MouseEvent event)
	{
		EntityPlayer player = ClientHelper.getPlayer();
		if (event.getDwheel() != 0)
		{
			ItemStack stack = player.getHeldItemMainhand();
			if (ItemStackHelper.isBitToolStack(stack))
			{
				boolean forward = event.getDwheel() < 0;
				if (KeyBindingsExtraBitManipulation.SHIFT.isKeyDown())
				{
					if (ItemStackHelper.isBitWrenchItem(stack.getItem()))
					{
						ExtraBitManipulation.packetNetwork.sendToServer(new PacketCycleBitWrenchMode(forward));
					}
					else
					{
						cycleSemiDiameter(player, stack, forward);
					}
					event.setCanceled(true);
				}
				else if (ItemStackHelper.isSculptingToolItem(stack.getItem())
						&& (KeyBindingsExtraBitManipulation.CONTROL.isKeyDown()
								|| KeyBindingsExtraBitManipulation.ALT.isKeyDown()))
				{
					if (KeyBindingsExtraBitManipulation.CONTROL.isKeyDown())
					{
						cycleDirection(player, stack, forward);
					}
					else
					{
						cycleWallThickness(player, stack, forward);
					}
					event.setCanceled(true);
				}
			}
			else
			{
				drawnStartPoint = null;
			}
		}
		else if ((KeyBindingsExtraBitManipulation.CONTROL.isKeyDown() || KeyBindingsExtraBitManipulation.ALT.isKeyDown()) && event.isButtonstate())
		{
			ItemStack stack = player.getHeldItemMainhand();
			Item item = stack.getItem();
			if (ItemStackHelper.isSculptingToolItem(item))
			{
				if (KeyBindingsExtraBitManipulation.CONTROL.isKeyDown())
				{
					if (event.getButton() == 1)
						cycleShapeType(player, stack, item);
					
					if (event.getButton() == 0)
						toggleBitGridTargeted(player, stack);
				}
				else
				{
					if (event.getButton() == 1)
						toggleHollowShape(player, stack, item);
					
					if (event.getButton() == 0)
						toggleOpenEnds(player, stack);
				}
				event.setCanceled(true);
		