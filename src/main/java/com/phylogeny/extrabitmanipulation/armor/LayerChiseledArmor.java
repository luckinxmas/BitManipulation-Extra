package com.phylogeny.extrabitmanipulation.armor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelIllager;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelVex;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;

public class LayerChiseledArmor implements LayerRenderer<EntityLivingBase>
{
	private final Map<NBTTagCompound, List<Integer>> movingPartsDisplayListsMap = new HashMap<NBTTagCompound, List<Integer>>();
	private ModelRenderer head, body, villagerArms, rightLeg, leftLeg, rightArm, leftArm;
	private ModelBase model;
	private boolean smallArms, isIllager, isVex;
	private RenderLivingBase<? extends EntityLivingBase> livingEntityRenderer;
	
	public LayerChiseledArmor(RenderLivingBase<? extends EntityLivingBase> livingEntityRenderer)
	{
		this.livingEntityRenderer = livingEntityRenderer;
		updateModelAndRenderers(false);
	}
	
	public void updateModelAndRenderers(boolean force)
	{
		ModelBase modelNew = livingEntityRenderer.getMainModel();
		if (!force && modelNew == model)
			return;
		
		model = modelNew;
		if (model instanceof ModelVillager)
		{
			ModelVillager modelVillager = ((ModelVil