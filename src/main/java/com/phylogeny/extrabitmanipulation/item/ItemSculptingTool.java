
package com.phylogeny.extrabitmanipulation.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.config.ConfigProperty;
import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper;
import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.SculptingData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import com.phylogeny.extrabitmanipulation.shape.AsymmetricalShape;
import com.phylogeny.extrabitmanipulation.shape.Cone;
import com.phylogeny.extrabitmanipulation.shape.ConeElliptic;
import com.phylogeny.extrabitmanipulation.shape.Cube;
import com.phylogeny.extrabitmanipulation.shape.Cuboid;
import com.phylogeny.extrabitmanipulation.shape.Cylinder;
import com.phylogeny.extrabitmanipulation.shape.CylinderElliptic;
import com.phylogeny.extrabitmanipulation.shape.Ellipsoid;
import com.phylogeny.extrabitmanipulation.shape.PrismIsoscelesTriangular;
import com.phylogeny.extrabitmanipulation.shape.PyramidIsoscelesTriangular;
import com.phylogeny.extrabitmanipulation.shape.PyramidRectangular;
import com.phylogeny.extrabitmanipulation.shape.PyramidSquare;
import com.phylogeny.extrabitmanipulation.shape.Shape;
import com.phylogeny.extrabitmanipulation.shape.Sphere;
import com.phylogeny.extrabitmanipulation.shape.SymmetricalShape;

public class ItemSculptingTool extends ItemBitToolBase
{
	public static final String[] MODE_TITLES = new String[]{"Local", "Global", "Drawn"};
	private boolean curved, removeBits;
	
	public ItemSculptingTool(boolean curved, boolean removeBits, String name)
	{
		super(name);
		this.curved = curved;
		this.removeBits = removeBits;
	}
	
	public boolean isCurved()
	{
		return curved;
	}
	
	public boolean removeBits()
	{
		return removeBits;
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return ItemStackHelper.hasKey(stack, NBTKeys.REMAINING_USES) && ItemStackHelper.getNBT(stack).getInteger(NBTKeys.REMAINING_USES)
				< ((ConfigProperty) Configs.itemPropertyMap.get(this)).maxDamage;
	}
	
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return 1 - getDurability(stack);
	}
	
	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack)
	{
		return MathHelper.hsvToRGB((float) (Math.max(0.0F, getDurability(stack)) / 3.0F), 1.0F, 1.0F);
	}
	
	private double getDurability(ItemStack stack)
	{
		return ItemStackHelper.getNBTOrNew(stack).getInteger(NBTKeys.REMAINING_USES)
				/ ((double) ((ConfigProperty) Configs.itemPropertyMap.get(this)).maxDamage);
	}
	
	@Override
	public boolean initialize(ItemStack stack)
	{
		super.initialize(stack);
		NBTTagCompound nbt = stack.getTagCompound();
		initInt(nbt, NBTKeys.REMAINING_USES, ((ConfigProperty) Configs.itemPropertyMap.get(this)).maxDamage);
		return true;
	}
	
	public NBTTagCompound initialize(ItemStack stack, SculptingData sculptingData)
	{
		NBTTagCompound nbt = ItemStackHelper.initNBT(stack);
		initInt(nbt, NBTKeys.REMAINING_USES, ((ConfigProperty) Configs.itemPropertyMap.get(this)).maxDamage);
		initInt(nbt, NBTKeys.SCULPT_MODE, sculptingData.getSculptMode());
		initInt(nbt, NBTKeys.SCULPT_SEMI_DIAMETER, sculptingData.getSemiDiameter());
		initInt(nbt, NBTKeys.DIRECTION, sculptingData.getDirection());
		initBoolean(nbt, NBTKeys.TARGET_BIT_GRID_VERTEXES, sculptingData.isBitGridTargeted());
		initInt(nbt, NBTKeys.SHAPE_TYPE, sculptingData.getShapeType());
		initBoolean(nbt, NBTKeys.SCULPT_HOLLOW_SHAPE, sculptingData.isHollowShape());
		initBoolean(nbt, NBTKeys.OPEN_ENDS, sculptingData.areEndsOpen());
		initInt(nbt, NBTKeys.WALL_THICKNESS, sculptingData.getWallThickness());
		if (!nbt.hasKey(NBTKeys.SET_BIT) && !sculptingData.getBitStack().isEmpty())
		{
			NBTTagCompound nbt2 = new NBTTagCompound();
			sculptingData.getBitStack().writeToNBT(nbt2);
			nbt.setTag(NBTKeys.SET_BIT, nbt2);
		}
		initBoolean(nbt, NBTKeys.OFFSET_SHAPE, sculptingData.isShapeOffset());
		return nbt;
	}
	
	public boolean sculptBlocks(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
			EnumFacing side, Vec3d hit, Vec3d drawnStartPoint, SculptingData sculptingData)
	{
		ItemStack setBitStack = sculptingData.getBitStack();
		if (setBitStack.isEmpty() && !removeBits)
			return false;
		
		if (!world.isRemote)
		{
			initialize(stack);
			player.inventoryContainer.detectAndSendChanges();
		}
		NBTTagCompound nbt = initialize(stack, sculptingData);
		IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
		boolean inside = wasInsideClicked(side, hit, pos);
		if (!removeBits && !inside)
			pos = pos.offset(side);
		
		boolean globalMode = sculptingData.getSculptMode() == 1;
		if (drawnStartPoint != null || globalMode || isValidBlock(api, world, pos))
		{
			float hitX = (float) hit.x - pos.getX();
			float hitY = (float) hit.y - pos.getY();
			float hitZ = (float) hit.z - pos.getZ();
			IBitLocation bitLoc = api.getBitPos(hitX, hitY, hitZ, side, pos, false);
			if (bitLoc != null)
			{
				int direction = sculptingData.getDirection();
				int shapeType = sculptingData.getShapeType();
				boolean hollowShape = sculptingData.isHollowShape();
				boolean openEnds = sculptingData.areEndsOpen();
				float wallThickness = sculptingData.getWallThickness() * Utility.PIXEL_F;
				float padding = sculptingData.getSemiDiameterPadding();
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				float x2 = x + bitLoc.getBitX() * Utility.PIXEL_F;
				float y2 = y + bitLoc.getBitY() * Utility.PIXEL_F;
				float z2 = z + bitLoc.getBitZ() * Utility.PIXEL_F;
				Shape shape;
				AxisAlignedBB box;
				if (shapeType != 4 && shapeType != 5)
					direction %= 6;
				
				if (drawnStartPoint != null)
				{
					switch(shapeType)
					{
						case 1: shape = new CylinderElliptic(); break;
						case 2: shape = new ConeElliptic(); break;
						case 3: shape = new Cuboid(); break;
						case 4: shape = new PrismIsoscelesTriangular(); break;
						case 5: shape = new PyramidIsoscelesTriangular(); break;
						case 6: shape = new PyramidRectangular(); break;
						default: shape = new Ellipsoid(); break;
					}
					float x3 = (float) drawnStartPoint.x;
					float y3 = (float) drawnStartPoint.y;
					float z3 = (float) drawnStartPoint.z;
					float minX = addPaddingToMin(x2, x3, padding);
					float minY = addPaddingToMin(y2, y3, padding);
					float minZ = addPaddingToMin(z2, z3, padding);
					float maxX = addPaddingToMax(x2, x3, padding);
					float maxY = addPaddingToMax(y2, y3, padding);
					float maxZ = addPaddingToMax(z2, z3, padding);
					box = new AxisAlignedBB(Math.floor(minX), Math.floor(minY), Math.floor(minZ),
							Math.ceil(maxX), Math.ceil(maxY), Math.ceil(maxZ));
					float f = 0.5F;
					minX *= f;
					minY *= f;
					minZ *= f;
					maxX *= f;
					maxY *= f;
					maxZ *= f;
					((AsymmetricalShape) shape).init(maxX + minX, maxY + minY, maxZ + minZ, maxX - minX, maxY - minY, maxZ - minZ,
							direction, hollowShape, wallThickness, openEnds);
				}
				else
				{
					switch(shapeType)
					{
						case 1: shape = new Cylinder(); break;
						case 2: shape = new Cone(); break;
						case 3: shape = new Cube(); break;
						case 4: shape = new PrismIsoscelesTriangular(); break;
						case 5: shape = new PyramidIsoscelesTriangular(); break;
						case 6: shape = new PyramidSquare(); break;
						default: shape = new Sphere(); break;
					}
					int semiDiameter = sculptingData.getSemiDiameter();
					int blockSemiDiameter = globalMode ? (int) Math.ceil(semiDiameter / 16.0) : 0;
					if (sculptingData.isShapeOffset() && !removeBits)
					{
						int offsetX = side.getFrontOffsetX();
						int offsetY = side.getFrontOffsetY();
						int offsetZ = side.getFrontOffsetZ();
						x2 += offsetX * Utility.PIXEL_F * semiDiameter;
						y2 += offsetY * Utility.PIXEL_F * semiDiameter;
						z2 += offsetZ * Utility.PIXEL_F * semiDiameter;
						x += offsetX * blockSemiDiameter;
						y += offsetY * blockSemiDiameter;
						z += offsetZ * blockSemiDiameter;
					}
					box = new AxisAlignedBB(x - blockSemiDiameter, y - blockSemiDiameter, z - blockSemiDiameter,
							x + blockSemiDiameter, y + blockSemiDiameter, z + blockSemiDiameter);
					float f = 0;
					Vec3d vecOffset = new Vec3d(0, 0, 0);
					if (sculptingData.isBitGridTargeted())
					{
						f = Utility.PIXEL_F * 0.5F;
						vecOffset = BitAreaHelper.getBitGridOffset(side, inside, hitX, hitY, hitZ, removeBits);
					}