
package com.phylogeny.extrabitmanipulation.reference;

import java.util.HashMap;
import java.util.Map;

import com.phylogeny.extrabitmanipulation.armor.ChiseledArmorStackHandeler.ArmorStackModelRenderMode;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsEventHandler.ArmorButtonVisibiltyMode;
import com.phylogeny.extrabitmanipulation.config.ConfigBitStack;
import com.phylogeny.extrabitmanipulation.config.ConfigBitToolSettingBoolean;
import com.phylogeny.extrabitmanipulation.config.ConfigBitToolSettingInt;
import com.phylogeny.extrabitmanipulation.config.ConfigNamed;
import com.phylogeny.extrabitmanipulation.config.ConfigReplacementBits;
import com.phylogeny.extrabitmanipulation.config.ConfigShapeRender;
import com.phylogeny.extrabitmanipulation.config.ConfigShapeRenderPair;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.init.ModelRegistration.ArmorModelRenderMode;
import com.phylogeny.extrabitmanipulation.packet.PacketThrowBit.BitBagBitSelectionMode;

import mod.chiselsandbits.api.IBitBrush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;