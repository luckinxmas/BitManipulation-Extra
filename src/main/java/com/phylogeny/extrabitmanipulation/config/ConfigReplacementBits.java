package com.phylogeny.extrabitmanipulation.config;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class ConfigReplacementBits
{
	private ConfigBitStack defaultReplacementBit;
	private boolean useDefaultReplacementBit, useAnyBitsAsReplacements, useAirAsReplacement;
	
	public ConfigReplacementBits() {}
	
	public ConfigReplacementBits(ConfigBitStack defaultReplacementBit, boolean useDefaultReplacementBit,
			boolean useAnyBitsAsReplacements, boolean useAirAsReplacement)
	{
		this.defaultReplacementBit = def