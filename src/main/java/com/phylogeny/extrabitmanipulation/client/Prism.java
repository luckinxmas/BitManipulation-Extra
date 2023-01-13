package com.phylogeny.extrabitmanipulation.client;

import net.minecraft.client.renderer.GlStateManager;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Quadric;

public class Prism extends Quadric
{
	private boolean isPryamid, isTriangular;
	
	public Prism(boolean isPryamid, boolean isTriangular)
	{
		this.isPryamid = isPryamid;
		this.isTriangular = isTriangular;
	}
	
	public void draw(float radius, boolean isOpen)
	{
		float slope = isPryamid ? radius : 0;
		float slope2 = isTriangular ? radius : 0;
		boolean isCube = !isPryamid && !isTriangular;
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, radius, isCube ? -radius : 0);
		drawSquare(radius, isPryamid, slope, slope2);
		GlStateManager.translate(0, -radius * 2, 0);
		GlStateManager.scale(1, -1, 1);
		drawSquare(radius, isPryamid, slope, slope2);
		GlStateManager.popMatrix();
		
		GlStateManager.rotate(90, 0, 0, 1);
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, radius, isCube ? -radius : 0);
		if (!isTriangular)
		{
			drawSquare(radius, isPryamid, slope, 0);
		}
		GlStateManager.translate(0, -radius * 2, 0);
		