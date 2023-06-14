
package com.phylogeny.extrabitmanipulation.config;

import java.io.File;
import java.util.Arrays;

import com.phylogeny.extrabitmanipulation.armor.ChiseledArmorStackHandeler.ArmorStackModelRenderMode;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsEventHandler.ArmorButtonVisibiltyMode;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.LogHelper;
import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ModelRegistration.ArmorModelRenderMode;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.packet.PacketThrowBit.BitBagBitSelectionMode;
import com.phylogeny.extrabitmanipulation.reference.BaublesReferences;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import com.phylogeny.extrabitmanipulation.shape.Shape;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ConfigHandlerExtraBitManipulation
{
	public static Configuration configFileClient, configFileServer, configFileCommon, modelingMapConfigFile, sculptingConfigFile, chiseledArmorConfigFile;
	public static final String ARMOR_SETTINGS = "Chiseled Armor Settings";
	public static final String SCULPTING_WRENCH_SETTINGS = "Sculpting & Wrech Settings";
	public static final String UNCHISELABLE_BLOCK_STATES = "Unchiselable Block States";
	public static final String INSUFFICIENT_BITS = "Insufficient Bits";