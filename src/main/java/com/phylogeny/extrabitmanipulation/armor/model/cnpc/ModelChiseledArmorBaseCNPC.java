package com.phylogeny.extrabitmanipulation.armor.model.cnpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.phylogeny.extrabitmanipulation.reference.Configs;

import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import noppes.npcs.client.model.ModelBipedAlt;
import noppes.npcs.client.model.ModelScaleRenderer;
import noppes.npcs.constants.EnumParts;


public class ModelChiseledArmorBaseCNPC extends ModelBipedAlt
{
	protected float scale;
	
	public ModelChiseledArmorBaseCNPC(float scale)
	{
		super(scale);
		textureWidth = 86;
		textureHeight = 64;
		scale = Configs.armorZFightingBufferScale;
	}
	
	protected void setRotationAngles(ModelRenderer modelRenderer, float angleX, float angleY, float angleZ)
	{
		modelRenderer.rotateAngleX = angleX;
		modelRenderer.rotateAngleY = angleY;
		modelRenderer.rotateAngleZ = angleZ;
	}
	
	protected ModelScaleRenderer createScaledModel(int texOffX, 