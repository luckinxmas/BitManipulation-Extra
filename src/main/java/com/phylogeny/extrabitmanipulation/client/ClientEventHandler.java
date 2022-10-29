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
			}
		}
		else if (event.getButton() == 0)
		{
			if (!player.capabilities.allowEdit)
				return;
			
			ItemStack stack = player.getHeldItemMainhand();
			Item item = stack.getItem();
			if (event.isButtonstate() && ItemStackHelper.isChiseledArmorItem(item))
			{
				RayTraceResult target = ClientHelper.getObjectMouseOver();
				if (target != null && target.typeOfHit == RayTraceResult.Type.BLOCK)
				{
					NBTTagCompound nbt = ItemStackHelper.getNBTOrNew(stack);
					int mode = BitToolSettingsHelper.getArmorMode(nbt);
					if (mode == 0)
					{
						ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetCollectionBox(player.rotationYaw, player.isSneaking(),
								player.getHorizontalFacing().getOpposite(), target.getBlockPos(), target.sideHit, target.hitVec));
						ClientHelper.printChatMessageWithDeletion("Set collection reference area for a " +
								ItemChiseledArmor.getPartAndScaleText(BitToolSettingsHelper.getArmorMovingPart(nbt,
										(ItemChiseledArmor) item), BitToolSettingsHelper.getArmorScale(nbt)));
						event.setCanceled(true);
					}
				}
			}
			if (event.isButtonstate() && ItemStackHelper.isBitWrenchItem(item))
			{
				event.setCanceled(true);
			}
			else if ((ItemStackHelper.isChiseledArmorItem(item) && BitToolSettingsHelper.getArmorMode(ItemStackHelper.getNBTOrNew(stack)) == 1)
					|| ItemStackHelper.isSculptingToolItem(item))
			{
				boolean isArmor = ItemStackHelper.isChiseledArmorItem(item);
				boolean drawnMode = isArmor ? true : BitToolSettingsHelper.getSculptMode(stack.getTagCompound()) == 2;
				if (!drawnMode)
					drawnStartPoint = null;
				
				if (event.isButtonstate() || (drawnMode && drawnStartPoint != null))
				{
					boolean removeBits = isArmor ? true : ((ItemSculptingTool) item).removeBits();
					RayTraceResult target = ClientHelper.getObjectMouseOver();
					boolean shiftDown = KeyBindingsExtraBitManipulation.SHIFT.isKeyDown();
					if (target != null && target.typeOfHit != RayTraceResult.Type.MISS)
					{
						if (target.typeOfHit == RayTraceResult.Type.BLOCK)
						{
							BlockPos pos = target.getBlockPos();
							EnumFacing side = target.sideHit;
							Vec3d hit = target.hitVec;
							boolean swingTool = true;
							if (drawnMode && event.isButtonstate() && drawnStartPoint != null)
							{
								event.setCanceled(true);
								return;
							}
							if ((isArmor || !shiftDown) && drawnMode && event.isButtonstate())
							{
								if (isArmor && !ItemStackHelper.getNBTOrNew(stack).hasKey(NBTKeys.ARMOR_HIT))
								{
									ClientHelper.printChatMessageWithDeletion("You must set a bodypart template reference area in 'Template Creation' " +
											"mode before collecting block copies.");
									swingTool = false;
								}
								else
								{
									IBitLocation bitLoc = ChiselsAndBitsAPIAccess.apiInstance.getBitPos((float) hit.x - pos.getX(),
											(float) hit.y - pos.getY(), (float) hit.z - pos.getZ(), side, pos, false);
									if (bitLoc != null)
									{
										int x = pos.getX();
										int y = pos.getY();
										int z = pos.getZ();
										float x2 = x + bitLoc.getBitX() * Utility.PIXEL_F;
										float y2 = y + bitLoc.getBitY() * Utility.PIXEL_F;
										float z2 = z + bitLoc.getBitZ() * Utility.PIXEL_F;
										if (!removeBits)
										{
											x2 += side.getFrontOffsetX() * Utility.PIXEL_F;
											y2 += side.getFrontOffsetY() * Utility.PIXEL_F;
											z2 += side.getFrontOffsetZ() * Utility.PIXEL_F;
										}
										drawnStartPoint = new Vec3d(x2, y2, z2);
										if (isArmor && player.isSneaking())
										{
											Vec3d vec = new Vec3d(side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ());
											if (BitToolSettingsHelper.areArmorBitsTargeted(ItemStackHelper.getNBTOrNew(stack)))
												vec = vec.scale(Utility.PIXEL_D);
											
											drawnStartPoint = drawnStartPoint.add(vec);
										}
									}
									else
									{
										drawnStartPoint = null;
										swingTool = false;
									}
								}
							}
							else
							{
								if (shiftDown && !isArmor)
								{
									IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
									IBitLocation bitLoc = api.getBitPos((float) hit.x - pos.getX(), (float) hit.y - pos.getY(),
											(float) hit.z - pos.getZ(), side, pos, false);
									if (bitLoc != null)
									{
										try
										{
											IBitAccess bitAccess = api.getBitAccess(player.world, pos);
											IBitBrush bit = bitAccess.getBitAt(bitLoc.getBitX(), bitLoc.getBitY(), bitLoc.getBitZ());
											BitToolSettingsHelper.setBitStack(player, stack, removeBits, bit,
													removeBits ? Configs.sculptSetBitWire : Configs.sculptSetBitSpade);
											if ((removeBits ? Configs.sculptSetBitWire : Configs.sculptSetBitSpade).shouldDisplayInChat())
											{
												ClientHelper.printChatMessageWithDeletion((removeBits ? "Removing only " : "Sculpting with ")
														+ BitToolSettingsHelper.getBitName(bit.getItemStack(1)));
											}
										}
										catch (CannotBeChiseled e)
										{
											event.setCanceled(true);
											return;
										}
									}
								}
								else if (!shiftDown || removeBits || drawnMode)
								{
									if (isArmor)
									{
										NBTTagCompound nbt = ItemStackHelper.getNBTOrNew(stack);
										ArmorCollectionData collectionData = new ArmorCollectionData(nbt, (ItemChiseledArmor) item,
												getDrawnArmorCollectionBox(player, nbt, side, pos, hit));
										swingTool = ItemChiseledArmor.collectArmorBlocks(player, collectionData);
										ExtraBitManipulation.packetNetwork.sendToServer(new PacketCollectArmorBlocks(collectionData));
									}
									else
									{
										SculptingData sculptingData = new SculptingData(stack.getTagCompound(), (ItemSculptingTool) item);
										swingTool = ((ItemSculptingTool) item).sculptBlocks(stack, player, player.world, pos, side, hit, drawnStartPoint, sculptingData);
										ExtraBitManipulation.packetNetwork.sendToServer(new PacketSculpt(pos, side, hit, drawnStartPoint, sculptingData));
									}
								}
								if (drawnMode && !event.isButtonstate())
									drawnStartPoint = null;
							}
							if (swingTool)
								player.swingArm(EnumHand.MAIN_HAND);
							
							event.setCanceled(true);
						}
					}
					else if (shiftDown && event.isButtonstate() && removeBits && !isArmor)
					{
						BitToolSettingsHelper.setBitStack(player, stack, true, null, Configs.sculptSetBitWire);
						if ((removeBits ? Configs.sculptSetBitWire : Configs.sculptSetBitSpade).shouldDisplayInChat())
							ClientHelper.printChatMessageWithDeletion("Removing any/all bits");
					}
					else if (drawnMode)
					{
						drawnStartPoint = null;
					}
				}
			}
		}
		else if (event.getButton() == 1 && event.isButtonstate())
		{
			ItemStack stack = player.getHeldItemMainhand();
			if (ItemStackHelper.isModelingToolStack(stack) && ItemStackHelper.hasKey(stack, NBTKeys.SAVED_STATES)
					&& KeyBindingsExtraBitManipulation.SHIFT.isKeyDown())
			{
				openBitMappingGui();
				event.setCanceled(true);
			}
		}
		if (!event.isCanceled() && event.getButton() == 1 && event.isButtonstate())
		{
			ItemStack stack = player.getHeldItemMainhand();
			if (ItemStackHelper.isSculptingToolStack(stack))
				cycleSculptMode(player, stack, !KeyBindingsExtraBitManipulation.SHIFT.isKeyDown());	
		}
		if (event.getDwheel() != 0)
		{
			ItemStack stack = player.getHeldItemMainhand();
			boolean isArmor = ItemStackHelper.isChiseledArmorStack(stack);
			if (isArmor || ItemStackHelper.isModelingToolStack(stack))
			{
				boolean forward = event.getDwheel() < 0;
				boolean controlDown = KeyBindingsExtraBitManipulation.CONTROL.isKeyDown();
				if (controlDown || (isArmor ? KeyBindingsExtraBitManipulation.ALT.isKeyDown() : KeyBindingsExtraBitManipulation.SHIFT.isKeyDown()))
				{
					if (controlDown)
					{
						if (isArmor)
							cycleArmorScale(player, stack, forward);
						else
							cycleModelSnapMode(player, stack, forward);
					}
					else
					{
						if (isArmor)
							cycleArmorMovingPart(player, stack, forward);
						else
							cycleModelAreaMode(player, stack, forward);
					}
					event.setCanceled(true);
				}
			}
			else
			{
				drawnStartPointModelingTool = null;
			}
		}
		else if ((KeyBindingsExtraBitManipulation.CONTROL.isKeyDown() || KeyBindingsExtraBitManipulation.ALT.isKeyDown()) && event.isButtonstate())
		{
			ItemStack stack = player.getHeldItemMainhand();
			boolean isArmor = ItemStackHelper.isChiseledArmorStack(stack);
			boolean controlDown = KeyBindingsExtraBitManipulation.CONTROL.isKeyDown();
			if (isArmor || (ItemStackHelper.isModelingToolStack(stack) && controlDown))
			{
				if (event.getButton() == 1)
				{
					if (isArmor)
					{
						if (controlDown)
							toggleArmorMode(player, stack);
						else
							toggleArmorBitsTargeted(player, stack);
					}
					else
					{
						toggleModelGuiOpen(player, stack);
					}
				}
				event.setCanceled(true);
			}
		}
		else if (event.getButton() == 0)
		{
			ItemStack stack = player.getHeldItemMainhand();
			if (ItemStackHelper.isModelingToolStack(stack))
			{
				Item item = stack.getItem();
				if (item != null)
				{
					boolean drawnMode = BitToolSettingsHelper.getModelAreaMode(stack.getTagCompound()) == 2;
					if (!drawnMode)
						drawnStartPointModelingTool = null;
					
					if (event.isButtonstate() || (drawnMode && drawnStartPointModelingTool != null))
					{
						RayTraceResult target = ClientHelper.getObjectMouseOver();
						if (target != null && target.typeOfHit != RayTraceResult.Type.MISS)
						{
							if (target.typeOfHit == RayTraceResult.Type.BLOCK)
							{
								BlockPos pos = target.getBlockPos();
								Vec3d hit = target.hitVec;
								boolean swingTool = true;
								if (drawnMode && event.isButtonstate() && drawnStartPointModelingTool != null)
								{
									event.setCanceled(true);
									return;
								}
								if (!KeyBindingsExtraBitManipulation.SHIFT.isKeyDown() && drawnMode && event.isButtonstate())
								{
									drawnStartPointModelingTool = new Vec3i(pos.getX(), pos.getY(), pos.getZ());
								}
								else
								{
									if (!KeyBindingsExtraBitManipulation.SHIFT.isKeyDown() || drawnMode)
									{
										ModelReadData modelingData = new ModelReadData(stack.getTagCompound());
										swingTool = BitAreaHelper.readBlockStates(stack, player, player.world, pos, hit,
												drawnStartPointModelingTool, modelingData);
										ExtraBitManipulation.packetNetwork.sendToServer(new PacketReadBlockStates(pos, hit,
												drawnStartPointModelingTool, modelingData));
									}
									if (drawnMode && !event.isButtonstate())
										drawnStartPointModelingTool = null;
								}
								if (swingTool)
									player.swingArm(EnumHand.MAIN_HAND);
								
								event.setCanceled(true);
							}
						}
						else if (drawnMode)
						{
							drawnStartPointModelingTool = null;
						}
					}
				}
			}
		}
	}
	
	private void toggleArmorMode(EntityPlayer player, ItemStack stack)
	{
		int mode = BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getArmorMode(stack.getTagCompound()), true, ItemChiseledArmor.MODE_TITLES.length);
		BitToolSettingsHelper.setArmorMode(player, stack, mode, Configs.armorMode);
		if (Configs.armorMode.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getArmorModeText(mode));
	}
	
	private void toggleArmorBitsTargeted(EntityPlayer player, ItemStack stack)
	{
		boolean targetBits = !BitToolSettingsHelper.areArmorBitsTargeted(stack.getTagCompound());
		BitToolSettingsHelper.setArmorBitsTargeted(player, stack, targetBits, Configs.armorTargetBits);
		if (Configs.armorTargetBits.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getArmorBitsTargetedText(targetBits));
	}
	
	private void cycleArmorScale(EntityPlayer player, ItemStack stack, boolean forward)
	{
		int scale = BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getArmorScale(stack.getTagCompound()),
				forward, ItemChiseledArmor.SCALE_TITLES.length);
		BitToolSettingsHelper.setArmorScale(player, stack, scale, Configs.armorScale);
		if (Configs.armorScale.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getArmorScaleText(scale));
	}
	
	private void cycleArmorMovingPart(EntityPlayer player, ItemStack stack, boolean forward)
	{
		ItemChiseledArmor armorPiece = (ItemChiseledArmor) stack.getItem();
		int partIndex = BitToolSettingsHelper.getArmorMovingPart(stack.getTagCompound(), armorPiece).getPartIndex();
		partIndex = BitToolSettingsHelper.cycleData(partIndex, forward, armorPiece.MOVING_PART_TITLES.length);
		BitToolSettingsHelper.setArmorMovingPart(player, stack, armorPiece, partIndex);
		if (BitToolSettingsHelper.getArmorMovingPartConfig(armorPiece.armorType).shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getArmorMovingPartText(armorPiece.MOVING_PARTS[partIndex], armorPiece));
	}
	
	private void cycleModelAreaMode(EntityPlayer player, ItemStack stack, boolean forward)
	{
		int mode = BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getModelAreaMode