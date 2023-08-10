
package com.phylogeny.extrabitmanipulation.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.api.KeyBindingContext;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.armor.ArmorItem;
import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.armor.GlOperation;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.CreativeTabExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper;
import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorBodyPartTemplateData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorCollectionData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.BlocksExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ModelRegistration;
import com.phylogeny.extrabitmanipulation.packet.PacketCreateBodyPartTemplate;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import com.phylogeny.extrabitmanipulation.reference.Utility;

@KeyBindingContext("menuitem")
public class ItemChiseledArmor extends ItemArmor
{
	public static final String[] MODE_TITLES = new String[]{"Template Creation", "Block Collection"};
	public static final String[] SCALE_TITLES = new String[]{"1:1", "1:2", "1:4"};
	public final ArmorMovingPart[] MOVING_PARTS;
	public final String[] MOVING_PART_TITLES;
	public final ArmorType armorType;
	@SideOnly(Side.CLIENT)
	private ModelResourceLocation itemModelLocation;
	
	@SuppressWarnings("null")
	public ItemChiseledArmor(String name, ArmorMaterial material, ArmorType armorType, ArmorMovingPart... movingParts)
	{
		super(material, 0, armorType.getEquipmentSlot());
		setRegistryName(name);
		setUnlocalizedName(getRegistryName().toString());
		setCreativeTab(CreativeTabExtraBitManipulation.CREATIVE_TAB);
		this.armorType = armorType;
		MOVING_PARTS = movingParts;
		MOVING_PART_TITLES = new String[MOVING_PARTS.length];
		for (int i = 0; i < MOVING_PARTS.length; i++)
			MOVING_PART_TITLES[i] = MOVING_PARTS[i].getName();
	}
	
	@SuppressWarnings("null")
	@SideOnly(Side.CLIENT)
	public ResourceLocation initItemModelLocation()
	{
		ResourceLocation loc = new ResourceLocation(getRegistryName().getResourceDomain(),
				getRegistryName().getResourcePath() + "_default");
		itemModelLocation = new ModelResourceLocation(loc, "inventory");
		return loc;
	}
	
	@SideOnly(Side.CLIENT)
	public ModelResourceLocation getItemModelLocation()
	{
		return itemModelLocation;
	}
	
	@SideOnly(Side.CLIENT)
	public IBakedModel getItemModel()
	{
		return ClientHelper.getBlockModelShapes().getModelManager().getModel(itemModelLocation);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	@Nullable
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped modeldefault)
	{
		return ModelRegistration.getArmorModel(stack, slot, entity);
	}
	
	@Override
	@Nullable
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
	{
		return ModelRegistration.getArmorTexture(stack, getArmorMaterial());
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return slotChanged || oldStack.hasTagCompound() != newStack.hasTagCompound() || (oldStack.hasTagCompound() && newStack.hasTagCompound()
				&& !ItemStackHelper.getArmorData(oldStack.getTagCompound()).equals(ItemStackHelper.getArmorData(newStack.getTagCompound())));
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote)
		{
			ArmorBodyPartTemplateData templateData = new ArmorBodyPartTemplateData(ItemStackHelper.getNBTOrNew(stack), this);
			Vec3d hit = new Vec3d(hitX, hitY, hitZ);
			if (createBodyPartTemplate(player, world, pos, facing, hit, templateData) == EnumActionResult.SUCCESS)
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketCreateBodyPartTemplate(pos, facing, hit, templateData));
		}
		return EnumActionResult.SUCCESS;
	}
	
	public static EnumActionResult createBodyPartTemplate(EntityPlayer player, World world, BlockPos pos,
			EnumFacing facing, Vec3d hit, ArmorBodyPartTemplateData templateData)
	{
		NBTTagCompound nbt = ItemStackHelper.getNBTOrNew(player.getHeldItemMainhand());
		if (templateData.getMode() != 0)
			return EnumActionResult.PASS;
		
		IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
		IBitBrush bitBodyPartTemplate = null;
		try
		{
			bitBodyPartTemplate = api.createBrushFromState(BlocksExtraBitManipulation.bodyPartTemplate.getDefaultState());
		}
		catch (InvalidBitItem e)
		{
			return EnumActionResult.FAIL;
		}
		ItemStack bitStack = bitBodyPartTemplate.getItemStack(1);
		hit = hit.addVector(pos.getX(), pos.getY(), pos.getZ());
		AxisAlignedBB box = getBodyPartTemplateBox(player, facing, pos, hit, templateData.getScale(), templateData.getMovingPart());
		boolean creativeMode = player.capabilities.isCreativeMode;
		if (!creativeMode)
		{
			int bitsMissing = (int) (Math.round((box.maxX - box.minX) / Utility.PIXEL_D) * Math.round((box.maxY - box.minY) / Utility.PIXEL_D)
					* Math.round((box.maxZ - box.minZ) / Utility.PIXEL_D)) - BitInventoryHelper.countInventoryBits(api, player, bitStack.copy())
					- BitInventoryHelper.countInventoryBlocks(player, BlocksExtraBitManipulation.bodyPartTemplate) * 4096;
			if (bitsMissing > 0)
			{
				if (world.isRemote)
					ClientHelper.printChatMessageWithDeletion("There are insufficient Bodypart Template blocks/bits in your inventory. Obtain " + bitsMissing
							+ " Bodypart Template bits or blocks worth of bits (1 block = 4096 bits).");
				
				return EnumActionResult.FAIL;
			}
		}
		int bitsPlaced = 0;
		AxisAlignedBB boxBlocks = new AxisAlignedBB(Math.floor(box.minX), Math.floor(box.minY), Math.floor(box.minZ),
				Math.ceil(box.maxX), Math.ceil(box.maxY), Math.ceil(box.maxZ));
		try
		{
			api.beginUndoGroup(player);
			for (int i = (int) boxBlocks.minX; i <= boxBlocks.maxX; i++)
			{
				for (int j = (int) boxBlocks.minY; j <= boxBlocks.maxY; j++)
				{
					for (int k = (int) boxBlocks.minZ; k <= boxBlocks.maxZ; k++)
						bitsPlaced = placeBodyPartTemplateBits(world, new BlockPos(i, j, k), api, box, bitBodyPartTemplate, bitsPlaced);
				}
			}
		}
		finally
		{
			api.endUndoGroup(player);
			if (!world.isRemote && !creativeMode)
			{
				bitsPlaced = BitInventoryHelper.removeOrAddInventoryBits(api, player, bitStack.copy(), bitsPlaced, false);