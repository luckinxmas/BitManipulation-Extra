package com.phylogeny.extrabitmanipulation.shape;

import net.minecraft.util.math.BlockPos;

public abstract class SlopedAsymmetricalShape extends AsymmetricalShape
{
	protected float height, aInset2, cInset2;
	private float insetMin, insetMax, insetMin2, insetMax2;
	
	@Override
	public void init(float centerX, float centerY, float centerZ, float a, float b, float c,
			int direction, boolean sculptHollowShape, float wallThickness, boolean openEnds)
	{
		super.init(centerX, centerY, centerZ, a, b, c, direction, sculptHollowShape, wallThickness, openEnds);
		height = this.b * 2;
		float hsq = height * height;
		aInset2 = this.b - (float) ((Math.sqrt(this.a * this.a + hsq) * wallThickness) / this.a);
		cInset2 = this.b - (float) ((Math.sqrt(this.c * this.c + hsq) * wallThickness) / this.c);
		float bInset2 = Math.max(aInset2, cInset2);
		insetMax = this.centerY + bInset;
		insetMin = this.centerY - 