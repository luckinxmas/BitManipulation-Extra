package com.phylogeny.extrabitmanipulation.client.render;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;

public class RenderEntityBit extends Render<EntityBit>
{
	
	public RenderEntityBit(RenderManager renderManager)
	{
		super(renderManager);
	}
	
	@Override
	public void doRender(EntityBit entity, 