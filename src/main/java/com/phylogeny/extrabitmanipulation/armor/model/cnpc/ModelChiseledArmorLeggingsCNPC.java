package com.phylogeny.extrabitmanipulation.armor.model.cnpc;

import com.phylogeny.extrabitmanipulation.reference.Configs;

import noppes.npcs.constants.EnumParts;

public class ModelChiseledArmorLeggingsCNPC extends ModelChiseledArmorBaseCNPC
{
	
	public ModelChiseledArmorLeggingsCNPC()
	{
		super(1);
		scale += Configs.armorZFightingBufferScaleRightLegOrFoot;
		
		//Pelvis
		bipedBody = createScaledModel(0, 45, EnumParts.BODY);
		bipedBody.addBox(-4.0F, 7.0F, -2.0F, 8, 5, 4, scale);
		
		//Right Leg
		bipedRightLeg = createScaledModel(25, 40, EnumParts.LEG_RIGH