package com.phylogeny.extrabitmanipulation.shape;

import net.minecraft.util.math.BlockPos;

public class Cuboid extends AsymmetricalShape
{
	
	@Override
	public boolean isPointInsideShape(BlockPos pos, int i, int j, int k)
	{
		float x = getBitPosX(pos, i, j);
		float y = getBitPosY(pos, i, j, k);
		float z = getBitPosZ(pos, j, k);
		boolean inShape = isPointInsideisCuboid(x, y, z, a, b, c);
		return sculptHollowShape ? inShape && !isPointInsideisCuboid(x, y, z, aInset, bInset, cInset) : inShape;
