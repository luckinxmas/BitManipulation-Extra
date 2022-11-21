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
		int mode = BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getModelAreaMode(stack.getTagCompound()),
				forward, ItemModelingTool.AREA_MODE_TITLES.length);
		BitToolSettingsHelper.setModelAreaMode(player, stack, mode, Configs.modelAreaMode);
		if (Configs.modelAreaMode.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getModelAreaModeText(mode));
	}
	
	private void cycleModelSnapMode(EntityPlayer player, ItemStack stack, boolean forward)
	{
		int mode = BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getModelSnapMode(stack.getTagCompound()),
				forward, ItemModelingTool.SNAP_MODE_TITLES.length);
		BitToolSettingsHelper.setModelSnapMode(player, stack, mode, Configs.modelSnapMode);
		if (Configs.modelSnapMode.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getModelSnapModeText(mode));
	}
	
	private void toggleModelGuiOpen(EntityPlayer player, ItemStack stack)
	{
		boolean modelGuiOpen = !BitToolSettingsHelper.getModelGuiOpen(stack.getTagCompound());
		BitToolSettingsHelper.setModelGuiOpen(player, stack, modelGuiOpen, Configs.modelGuiOpen);
		if (Configs.modelGuiOpen.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getModelGuiOpenText(modelGuiOpen));
	}
	
	private void cycleSculptMode(EntityPlayer player, ItemStack stack, boolean forward)
	{
		int mode = BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getSculptMode(stack.getTagCompound()), forward, ItemSculptingTool.MODE_TITLES.length);
		BitToolSettingsHelper.setSculptMode(player, stack, mode, Configs.sculptMode);
		if (Configs.sculptMode.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getSculptModeText(mode));
	}
	
	private void cycleDirection(EntityPlayer player, ItemStack stack, boolean forward)
	{
		NBTTagCompound nbt = ItemStackHelper.getNBTOrNew(stack);
		int direction = BitToolSettingsHelper.getDirection(nbt);
		int shapeType = BitToolSettingsHelper.getShapeType(nbt, ((ItemSculptingTool) stack.getItem()).isCurved());
		int rotation = direction / 6;
		direction %= 6;
		if (!(shapeType == 4 && (forward ? rotation != 1 : rotation != 0)) && !(shapeType == 5 && (forward ? rotation != 3 : rotation != 0)))
		{
			direction = shapeType == 2 || shapeType > 3 ? (forward ? DIRECTION_FORWARD[direction] : DIRECTION_BACKWARD[direction])
					: (forward ? AXIS_FORWARD[direction] : AXIS_BACKWARD[direction]);
			rotation = forward ? 0 : (shapeType == 4 ? 1 : 3);
		}
		else
		{
			rotation = shapeType == 4 ? (rotation == 0 ? 1 : 0) : BitToolSettingsHelper.cycleData(rotation, forward, 4);
		}
		direction += 6 * rotation;
		BitToolSettingsHelper.setDirection(player, stack, direction, Configs.sculptDirection);
		if (Configs.sculptDirection.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getDirectionText(direction, shapeType == 4 || shapeType == 5));
	}
	
	private void cycleShapeType(EntityPlayer player, ItemStack stack, Item item)
	{
		boolean isCurved = ((ItemSculptingTool) item).isCurved();
		NBTTagCompound nbt = ItemStackHelper.getNBTOrNew(stack);
		int shapeType = BitToolSettingsHelper.getShapeType(nbt, isCurved);
		shapeType = isCurved ? SHAPE_CURVED[shapeType] : SHAPE_FLAT[shapeType];
		BitToolSettingsHelper.setShapeType(player, stack, isCurved, shapeType, isCurved ? Configs.sculptShapeTypeCurved : Configs.sculptShapeTypeFlat);
		if ((isCurved ? Configs.sculptShapeTypeCurved : Configs.sculptShapeTypeFlat).shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getShapeTypeText(shapeType));
	}
	
	private void toggleBitGridTargeted(EntityPlayer player, ItemStack stack)
	{
		boolean targetBitGrid = !BitToolSettingsHelper.isBitGridTargeted(stack.getTagCompound());
		BitToolSettingsHelper.setBitGridTargeted(player, stack, targetBitGrid, Configs.sculptTargetBitGridVertexes);
		if (Configs.sculptTargetBitGridVertexes.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getBitGridTargetedText(targetBitGrid));
	}
	
	private void cycleSemiDiameter(EntityPlayer player, ItemStack stack, boolean forward)
	{
		int semiDiameter = BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getSemiDiameter(stack.getTagCompound()),
				forward, Configs.maxSemiDiameter);
		BitToolSettingsHelper.setSemiDiameter(player, stack, semiDiameter, Configs.sculptSemiDiameter);
		if (Configs.sculptSemiDiameter.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getSemiDiameterText(stack.getTagCompound(), semiDiameter));
	}
	
	private void toggleHollowShape(EntityPlayer player, ItemStack stack, Item item)
	{
		boolean isWire = ((ItemSculptingTool) item).removeBits();
		boolean isHollowShape = !BitToolSettingsHelper.isHollowShape(stack.getTagCompound(), isWire);
		BitToolSettingsHelper.setHollowShape(player, stack, isWire, isHollowShape, isWire ? Configs.sculptHollowShapeWire : Configs.sculptHollowShapeSpade);
		if ((isWire ? Configs.sculptHollowShapeWire : Configs.sculptHollowShapeSpade).shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getHollowShapeText(isHollowShape));
	}
	
	private void toggleOpenEnds(EntityPlayer player, ItemStack stack)
	{
		boolean areEndsOpen = !BitToolSettingsHelper.areEndsOpen(stack.getTagCompound());
		BitToolSettingsHelper.setEndsOpen(player, stack, areEndsOpen, Configs.sculptOpenEnds);
		if (Configs.sculptOpenEnds.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getOpenEndsText(areEndsOpen));
	}
	
	private void cycleWallThickness(EntityPlayer player, ItemStack stack, boolean forward)
	{
		int wallThickness = BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getWallThickness(stack.getTagCompound()),
				forward, Configs.maxWallThickness);
		BitToolSettingsHelper.setWallThickness(player, stack, wallThickness, Configs.sculptWallThickness);
		if (Configs.sculptWallThickness.shouldDisplayInChat())
			ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getWallThicknessText(wallThickness));
	}
	
	@SubscribeEvent
	public void cancelBoundingBoxDraw(DrawBlockHighlightEvent event)
	{
		ItemStack stack = event.getPlayer().getHeldItemMainhand();
		if (ItemStackHelper.isSculptingToolStack(stack) && BitToolSettingsHelper.getSculptMode(stack.getTagCompound()) == 1)
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void renderBoxesSpheresAndOverlays(RenderWorldLastEvent event)
	{
		EntityPlayer player = ClientHelper.getPlayer();
		World world = player.world;
		ItemStack stack = player.getHeldItemMainhand();
		if (stack.isEmpty())
			return;
		
		RayTraceResult target = ClientHelper.getObjectMouseOver();
		Item item = stack.getItem();
		boolean hitBlock = target != null && target.typeOfHit.equals(RayTraceResult.Type.BLOCK);
		boolean isArmor = ItemStackHelper.isChiseledArmorItem(item);
		if (!isArmor && (!hitBlock || !ItemStackHelper.isBitToolItem(item)))
			return;
		
		IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
		float ticks = event.getPartialTicks();
		double playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * ticks;
		double playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * ticks;
		double playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * ticks;
		Tessellator t = Tessellator.getInstance();
		BufferBuilder buffer = t.getBuffer();
		if (isArmor)
		{
			NBTTagCompound nbt = ItemStackHelper.getNBTOrNew(stack);
			if (nbt.hasKey(NBTKeys.ARMOR_HIT))
			{
				ArmorBodyPartTemplateBoxData boxData = new ArmorBodyPartTemplateBoxData(nbt, (ItemChiseledArmor) item);
				renderBodyPartTemplate(playerX, playerY, playerZ, boxData.getFacingBox(), t, buffer, boxData.getBox(), 0.0F);
			}
			if (!hitBlock)
				return;
		}
		@SuppressWarnings("null")
		EnumFacing dir = target.sideHit;
		BlockPos pos = target.getBlockPos();
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		double diffX = playerX - x;
		double diffY = playerY - y;
		double diffZ = playerZ - z;
		Vec3d hit = target.hitVec;
		if (ItemStackHelper.isBitWrenchItem(item) && api.isBlockChiseled(world, target.getBlockPos()) && !Configs.disableOverlays)
		{
			int mode = ItemStackHelper.getNBTOrNew(stack).getInteger(NBTKeys.WRENCH_MODE);
			if (timer == null)
				timer = Stopwatch.createStarted();
			
			millisecondsElapsed = timer.elapsed(TimeUnit.MILLISECONDS);
			int side = dir.ordinal();
			boolean upDown = side <= 1;
			boolean eastWest = side >= 4;
			boolean northSouth = !upDown && !eastWest;
			AxisAlignedBB box = new AxisAlignedBB(eastWest ? hit.x : x, upDown ? hit.y : y, northSouth ? hit.z : z,
					eastWest ? hit.x : x + 1, upDown ? hit.y : y + 1, northSouth ? hit.z : z + 1);
			
			int offsetX = Math.abs(dir.getFrontOffsetX());
			int offsetY = Math.abs(dir.getFrontOffsetY());
			int offsetZ = Math.abs(dir.getFrontOffsetZ());
			double invOffsetX = offsetX ^ 1;
			double invOffsetY = offsetY ^ 1;
			double invOffsetZ = offsetZ ^ 1;
			
			boolean invertDirection = KeyBindingsExtraBitManipulation.SHIFT.isKeyDown();
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableTexture2D();
			GlStateManager.pushMatrix();
			double angle = getInitialAngle(mode);
			if (mode == 3)
			{
				if (side % 2 == 1)
					angle += 180;
				
				if (side >= 4)
					angle -= 90;
			}
			else
			{
				if (mode == 0)
				{
					if (side % 2 == (invertDirection ? 0 : 1))
						angle *= -1;
				}
				else
				{
					if (side < 2 || side > 3)
						angle *= -1;
				}
				if (eastWest)
					angle += 90;
				
				if (side == (mode == 1 ? 1 : 0) || side == 3 || side == 4)
					angle += 180;
			}
			double offsetX2 = 0.5 * invOffsetX;
			double offsetY2 = 0.5 * invOffsetY;
			double offsetZ2 = 0.5 * invOffsetZ;
			
			double mirTravel = mode == 1 ? Configs.mirrorAmplitude * Math.cos(Math.PI * 2 * millisecondsElapsed / Configs.mirrorPeriod) : 0;
			double mirTravel1 = mirTravel;
			double mirTravel2 = 0;
			boolean mirrorInversion = invertDirection && mode == 1;
			if (mirrorInversion && side <= 1 && player.getHorizontalFacing().ordinal() > 3)
			{
				angle += 90;
				mirTravel1 = 0;
				mirTravel2 = mirTravel;
			}
			translateAndRotateTexture(playerX, playerY, playerZ, dir, upDown, eastWest, offsetX, offsetY,
					offsetZ, angle, diffX, diffY, diffZ, offsetX2, offsetY2, offsetZ2, mirTravel1, mirTravel2);
			
			Minecraft.getMinecraft().renderEngine.bindTexture(mode == 0 ? ARROW_CYCLICAL
					: (mode == 1 ? ARROW_BIDIRECTIONAL : (mode == 2 ? CIRCLE : INVERSION)));
			float minU = 0;
			float maxU = 1;
			float minV = 0;
			float maxV = 1;
			if (mode == 0)
			{
				if (invertDirection)
				{
					float minU2 = minU;
					minU = maxU;
					maxU = minU2;
				}
			}
			else if (mode == 2)
			{
				EnumFacing dir2 = side <= 1 ? EnumFacing.WEST : (side <= 3 ? EnumFacing.WEST : EnumFacing.DOWN);
				box = contractBoxOrRenderArrows(true, t, buffer, side, northSouth, dir2, box, invOffsetX,
						invOffsetY, invOffsetZ, invertDirection, minU, maxU, minV, maxV);
			}
			
			renderTexturedSide(t, buffer, side, northSouth, box, minU, maxU, minV, maxV, 1);
			GlStateManager.popMatrix();
			
			AxisAlignedBB box3 = world.getBlockState(pos).getSelectedBoundingBox(world, pos);
			for (int s = 0; s < 6; s++)
			{
				if (s != side)
				{
					GlStateManager.pushMatrix();
					upDown = s <= 1;
					eastWest = s >= 4;
					northSouth = !upDown && !eastWest;
					dir = EnumFacing.getFront(s);
					box = new AxisAlignedBB(eastWest ? (s == 5 ? box3.maxX : box3.minX) : x,
														upDown ? (s == 1 ? box3.maxY : box3.minY) : y,
														northSouth ? (s == 3 ? box3.maxZ : box3.minZ) : z,
														east