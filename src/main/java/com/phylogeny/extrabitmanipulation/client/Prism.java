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
		float 