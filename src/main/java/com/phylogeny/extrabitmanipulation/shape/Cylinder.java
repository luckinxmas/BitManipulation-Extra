package com.phylogeny.extrabitmanipulation.shape;

import net.minecraft.util.math.BlockPos;

public class Cylinder extends SymmetricalShape
{
	private float diameterSq, diameterInsetSq;
	
	@Override
	public void init(float centerX, float centerY, float centerZ, float radius, int direction,
			boolean sculptHollowShape, float wallThickness, boolean openEnds)
	{
		super.init(centerX, centerY, centerZ, radius, direction, sculptHollowShape, wallThickness, openEnds);
		diameter