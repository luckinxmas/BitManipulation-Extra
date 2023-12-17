package com.phylogeny.extrabitmanipulation.shape;

import net.minecraft.util.math.BlockPos;

public class Ellipsoid extends AsymmetricalShape
{
	
	@Override
	public boolean isPointInsideShape(BlockPos pos, int i, int j, int k)
	{
		float dx = getBitPosDiffX(pos, i, j, centerX);
		float dy = getBitPosDiffY(pos, i, j, k, centerY);
		float dz = getBitPosDiffZ(pos, j, k, centerZ);
		boolean inShape = isPointInsideisEllipsoid(dx, dy, dz, a, b, c);
		return sculptHollowShape ? inShape && !isPointInsideisEllipsoid(dx, dy, dz, aInset, bInset, cInset) : inShape;
	}
	
	private boolean isPointInsideisEllipsoid(float dx, float dy, float dz, 