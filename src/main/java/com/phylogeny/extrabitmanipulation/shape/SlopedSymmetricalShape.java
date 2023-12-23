package com.phylogeny.extrabitmanipulation.shape;

public abstract class SlopedSymmetricalShape extends SymmetricalShape
{
	protected float height, semiDiameterInset2;
	private float insetMin, insetMax, insetMin2, insetMax2;
	
	@Override
	public void init(float centerX, float centerY, float centerZ, float radius, int direction,
			boolean sculptHollowShape, float wallThickness, boolean openEnds)
	{
		super.init(centerX, centerY, centerZ, rad