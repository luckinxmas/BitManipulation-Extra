package com.phylogeny.extrabitmanipulation.config;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;

public class ConfigBitStack extends ConfigBitToolSettingBase<ItemStack>
{
	private IBlockState valueDefault, stateDefault, stateDefaultDefault;
	private String stringDeafult;
	
	public ConfigBitStack(ItemStack bitStackDefault)
	{
		super("", false, false);
		defaultValue = bitStackDefault;
	}
	
	public ConfigBitStack(String name, IBlockState bitBlockDefault, IBlockState defaultDefaultBitBlock, String stringDefault, IBlockState valueDefault)
	{
		this(name, false, false, bitBlockDefault, defaultDefaultBitBlock, stringDefault,