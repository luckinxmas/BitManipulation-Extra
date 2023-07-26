
package com.phylogeny.extrabitmanipulation.helper;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.config.ConfigBitStack;
import com.phylogeny.extrabitmanipulation.config.ConfigBitToolSettingBoolean;
import com.phylogeny.extrabitmanipulation.config.ConfigBitToolSettingInt;
import com.phylogeny.extrabitmanipulation.config.ConfigHandlerExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.config.ConfigReplacementBits;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorMovingPart;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.BodyPartTemplate;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.packet.PacketSetArmorMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetArmorMovingPart;
import com.phylogeny.extrabitmanipulation.packet.PacketSetArmorScale;
import com.phylogeny.extrabitmanipulation.packet.PacketSetBitStack;
import com.phylogeny.extrabitmanipulation.packet.PacketSetDirection;
import com.phylogeny.extrabitmanipulation.packet.PacketSetEndsOpen;
import com.phylogeny.extrabitmanipulation.packet.PacketSetHollowShape;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelAreaMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelGuiOpen;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelSnapMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetSculptMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetSemiDiameter;
import com.phylogeny.extrabitmanipulation.packet.PacketSetShapeOffset;
import com.phylogeny.extrabitmanipulation.packet.PacketSetShapeType;
import com.phylogeny.extrabitmanipulation.packet.PacketSetTargetArmorBits;
import com.phylogeny.extrabitmanipulation.packet.PacketSetTargetBitGridVertexes;
import com.phylogeny.extrabitmanipulation.packet.PacketSetWallThickness;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import com.phylogeny.extrabitmanipulation.shape.Shape;

import io.netty.buffer.ByteBuf;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BitToolSettingsHelper
{
	
	public static String[] getDirectionNames()
	{
		String[] directionTexts = new String[6];
		for (EnumFacing facing : EnumFacing.VALUES)
			directionTexts[facing.getIndex()] = facing.getName().substring(0, 1).toUpperCase() + facing.getName().substring(1);
		
		return directionTexts;
	}
	
	private static int getInt(ConfigBitToolSettingInt config, NBTTagCompound nbt, String nbtKey)
	{
		return config.isPerTool() ? ItemStackHelper.getInt(nbt, config.getDefaultValue(), nbtKey) : config.getValue();
	}
	
	private static boolean getBoolean(ConfigBitToolSettingBoolean config, NBTTagCompound nbt, String nbtKey)
	{
		return config.isPerTool() ? ItemStackHelper.getBoolean(nbt, config.getDefaultValue(), nbtKey) : config.getValue();
	}
	
	private static ItemStack getStack(ConfigBitStack config, NBTTagCompound nbt, String nbtKey)
	{
		return config.isPerTool() ? ItemStackHelper.getStack(nbt, nbtKey) : config.getValue();
	}
	
	private static void setIntProperty(World world, Configuration configFile, ConfigBitToolSettingInt config, String catagory, int value)
	{
		if (!world.isRemote)
			return;
		
		Property prop = configFile.get(catagory, config.getTitle(), config.getDefaultValue());
		if (prop != null)
		{
			config.setValue(value);
			prop.setValue(value);
			configFile.save();
		}
	}
	
	private static void setBooleanProperty(World world, Configuration configFile, ConfigBitToolSettingBoolean config, String catagory, boolean value)
	{
		if (!world.isRemote)
			return;
		
		Property prop = configFile.get(catagory, config.getTitle(), config.getDefaultValue());
		if (prop != null)
		{
			config.setValue(value);
			prop.setValue(value);
			configFile.save();
		}
	}
	
	private static void setStackProperty(World world, Configuration configFile, ConfigBitStack config, String catagory, IBitBrush value)
	{
		if (!world.isRemote)
			return;
		
		Property prop = configFile.get(catagory, config.getTitle(), config.getStringDeafult());
		if (prop != null)
		{
			config.setValue(value == null ? null : value.getItemStack(1));
			prop.setValue(BitIOHelper.getStringFromState(value == null ? null : value.getState()));
			configFile.save();
		}
	}
	
	public static void setBitMapProperty(boolean isStateMap, String[] stringEntries)
	{
		Configuration configFile = ConfigHandlerExtraBitManipulation.modelingMapConfigFile;
		Property prop = configFile.get(ConfigHandlerExtraBitManipulation.MODELING_TOOL_MANUAL_MAPPINGS,
				isStateMap ? ConfigHandlerExtraBitManipulation.STATE_TO_BIT_MAP : ConfigHandlerExtraBitManipulation.BLOCK_TO_BIT_MAP, new String[]{});
		if (prop != null)
		{
			prop.setValues(stringEntries);
			configFile.save();
		}
	}
	
	public static int getModelAreaMode(NBTTagCompound nbt)
	{
		return getInt(Configs.modelAreaMode, nbt, NBTKeys.MODEL_AREA_MODE);
	}
	
	public static void setModelAreaMode(EntityPlayer player, ItemStack stack, int mode, @Nullable ConfigBitToolSettingInt modelAreaMode)
	{
		World world = player.world;
		if (modelAreaMode == null || modelAreaMode.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetModelAreaMode(mode));
			}
			else
			{
				ItemStackHelper.setInt(player, stack, mode, NBTKeys.MODEL_AREA_MODE);
			}
		}
		else if (world.isRemote)
		{
			setIntProperty(world, ConfigHandlerExtraBitManipulation.modelingMapConfigFile,
					modelAreaMode, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_MODEL, mode);
		}
	}
	
	public static int getModelSnapMode(NBTTagCompound nbt)
	{
		return getInt(Configs.modelSnapMode, nbt, NBTKeys.MODEL_SNAP_MODE);
	}
	
	public static void setModelSnapMode(EntityPlayer player, ItemStack stack, int mode, @Nullable ConfigBitToolSettingInt modelSnapMode)
	{
		World world = player.world;
		if (modelSnapMode == null || modelSnapMode.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetModelSnapMode(mode));
			}
			else
			{
				ItemStackHelper.setInt(player, stack, mode, NBTKeys.MODEL_SNAP_MODE);
			}
		}
		else if (world.isRemote)
		{
			setIntProperty(world, ConfigHandlerExtraBitManipulation.modelingMapConfigFile,
					modelSnapMode, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_MODEL, mode);
		}
	}
	
	public static boolean getModelGuiOpen(NBTTagCompound nbt)
	{
		return getBoolean(Configs.modelGuiOpen, nbt, NBTKeys.MODEL_GUI_OPEN);
	}
	
	public static void setModelGuiOpen(EntityPlayer player, ItemStack stack, boolean isOpen, @Nullable ConfigBitToolSettingBoolean modelGuiOpen)
	{
		World world = player.world;
		if (modelGuiOpen == null || modelGuiOpen.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetModelGuiOpen(isOpen));
			}
			else
			{
				ItemStackHelper.setBoolean(player, stack, isOpen, NBTKeys.MODEL_GUI_OPEN);
			}
		}
		else if (world.isRemote)
		{
			setBooleanProperty(world, ConfigHandlerExtraBitManipulation.modelingMapConfigFile,
					modelGuiOpen, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_MODEL, isOpen);
		}
	}
	
	public static int getSculptMode(NBTTagCompound nbt)
	{
		return getInt(Configs.sculptMode, nbt, NBTKeys.SCULPT_MODE);
	}
	
	public static void setSculptMode(EntityPlayer player, ItemStack stack, int mode, @Nullable ConfigBitToolSettingInt sculptMode)
	{
		World world = player.world;
		if (sculptMode == null || sculptMode.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetSculptMode(mode));
			}
			else
			{
				ItemStackHelper.setInt(player, stack, mode, NBTKeys.SCULPT_MODE);
			}
		}
		else if (world.isRemote)
		{
			setIntProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
					sculptMode, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, mode);
		}
	}
	
	public static int getDirection(NBTTagCompound nbt)
	{
		return getInt(Configs.sculptDirection, nbt, NBTKeys.DIRECTION);
	}
	
	public static void setDirection(EntityPlayer player, ItemStack stack, int direction, @Nullable ConfigBitToolSettingInt sculptDirection)
	{
		World world = player.world;
		if (sculptDirection == null || sculptDirection.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetDirection(direction));
			}
			else
			{
				ItemStackHelper.setInt(player, stack, direction, NBTKeys.DIRECTION);
			}
		}
		else if (world.isRemote)
		{
			setIntProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
					sculptDirection, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, direction);
		}
	}
	
	public static int getShapeType(NBTTagCompound nbt, boolean isCurved)
	{
		int shapeType = getInt(isCurved ? Configs.sculptShapeTypeCurved : Configs.sculptShapeTypeFlat, nbt, NBTKeys.SHAPE_TYPE);
		return isCurved && shapeType > 2 ? Configs.sculptShapeTypeCurved.getDefaultValue()
				: (!isCurved && shapeType < 3 ? Configs.sculptShapeTypeFlat.getDefaultValue() : shapeType);
	}
	
	public static void setShapeType(EntityPlayer player, ItemStack stack, boolean isCurved, int shapeType, @Nullable ConfigBitToolSettingInt sculptShapeType)
	{
		World world = player.world;
		if (sculptShapeType == null || sculptShapeType.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetShapeType(isCurved, shapeType));
			}
			else
			{
				ItemStackHelper.setInt(player, stack, shapeType, NBTKeys.SHAPE_TYPE);
			}
		}
		else if (world.isRemote)
		{
			setIntProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
					sculptShapeType, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, shapeType);
		}
	}
	
	public static boolean isBitGridTargeted(NBTTagCompound nbt)
	{
		return getBoolean(Configs.sculptTargetBitGridVertexes, nbt, NBTKeys.TARGET_BIT_GRID_VERTEXES);
	}
	
	public static void setBitGridTargeted(EntityPlayer player, ItemStack stack,
			boolean isTargeted, @Nullable ConfigBitToolSettingBoolean sculptTargetBitGridVertexes)
	{
		World world = player.world;
		if (sculptTargetBitGridVertexes == null || sculptTargetBitGridVertexes.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetTargetBitGridVertexes(isTargeted));
			}
			else
			{
				ItemStackHelper.setBoolean(player, stack, isTargeted, NBTKeys.TARGET_BIT_GRID_VERTEXES);
			}
		}
		else if (world.isRemote)
		{
			setBooleanProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
					sculptTargetBitGridVertexes, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, isTargeted);
		}
	}
	
	public static int getSemiDiameter(NBTTagCompound nbt)
	{
		return getInt(Configs.sculptSemiDiameter, nbt, NBTKeys.SCULPT_SEMI_DIAMETER);
	}
	
	public static void setSemiDiameter(EntityPlayer player, ItemStack stack, int semiDiameter, @Nullable ConfigBitToolSettingInt sculptSemiDiameter)
	{
		World world = player.world;
		if (sculptSemiDiameter == null || sculptSemiDiameter.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetSemiDiameter(semiDiameter));
			}
			else
			{
				ItemStackHelper.setInt(player, stack, semiDiameter, NBTKeys.SCULPT_SEMI_DIAMETER);
			}
		}
		else if (world.isRemote)
		{
			setIntProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
					sculptSemiDiameter, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, semiDiameter);
		}
	}
	
	public static boolean isHollowShape(NBTTagCompound nbt, boolean isWire)
	{
		return getBoolean(isWire ? Configs.sculptHollowShapeWire : Configs.sculptHollowShapeSpade, nbt, NBTKeys.SCULPT_HOLLOW_SHAPE);
	}
	
	public static void setHollowShape(EntityPlayer player, ItemStack stack, boolean isWire,
			boolean hollowShape, @Nullable ConfigBitToolSettingBoolean sculptHollowShape)
	{
		World world = player.world;
		if (sculptHollowShape == null || sculptHollowShape.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetHollowShape(hollowShape, isWire));
			}
			else
			{
				ItemStackHelper.setBoolean(player, stack, hollowShape, NBTKeys.SCULPT_HOLLOW_SHAPE);
			}
		}
		else if (world.isRemote)
		{
			setBooleanProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
					sculptHollowShape, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, hollowShape);
		}
	}
	
	public static boolean areEndsOpen(NBTTagCompound nbt)
	{
		return getBoolean(Configs.sculptOpenEnds, nbt, NBTKeys.OPEN_ENDS);
	}
	
	public static void setEndsOpen(EntityPlayer player, ItemStack stack, boolean openEnds, @Nullable ConfigBitToolSettingBoolean sculptOpenEnds)
	{
		World world = player.world;
		if (sculptOpenEnds == null || sculptOpenEnds.isPerTool())
		{
			if (world.isRemote)
			{
				ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetEndsOpen(openEnds));
			}
			else
			{
				ItemStackHelper.setBoolean(player, stack, openEnds, NBTKeys.OPEN_ENDS);
			}
		}
		else if (world.isRemote)
		{
			setBooleanProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,