package com.phylogeny.extrabitmanipulation.shape;

import net.minecraft.util.math.BlockPos;

public abstract class SlopedAsymmetricalShape extends AsymmetricalShape
{
	protected float height, aInset2, cInset2;
	private float insetMin, insetMax, insetMin2, insetMax2;
	
	@Overri