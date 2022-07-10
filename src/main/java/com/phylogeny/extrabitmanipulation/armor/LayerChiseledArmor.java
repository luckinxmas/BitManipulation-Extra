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
			ModelVillager modelVillager = ((ModelVillager) model);
			head = modelVillager.villagerHead;
			body = modelVillager.villagerBody;
			rightLeg = modelVillager.rightVillagerLeg;
			leftLeg = modelVillager.leftVillagerLeg;
			villagerArms = modelVillager.villagerArms;
		}
		else if (model instanceof ModelIllager)
		{
			ModelIllager modelIllager = ((ModelIllager) model);
			head = modelIllager.head;
			body = modelIllager.body;
			rightLeg = modelIllager.leg0;
			leftLeg = modelIllager.leg1;
			villagerArms = modelIllager.arms;
			rightArm = modelIllager.rightArm;
			leftArm = modelIllager.leftArm;
			isIllager = true;
		}
		else
		{
			ModelBiped modelBiped = ((ModelBiped) model);
			head = modelBiped.bipedHead;
			body = modelBiped.bipedBody;
			rightLeg = modelBiped.bipedRightLeg;
			leftLeg = modelBiped.bipedLeftLeg;
			rightArm = modelBiped.bipedRightArm;
			leftArm = modelBiped.bipedLeftArm;
			villagerArms = null;
			if (model instanceof ModelPlayer)
				smallArms = ReflectionExtraBitManipulation.areArmsSmall((ModelPlayer) model);
			
			isVex = model instanceof ModelVex;
		}
	}
	
	public void clearDisplayListsMap()
	{
		for (List<Integer> displayLists : movingPartsDisplayListsMap.values())
			deleteDisplayLists(displayLists);
		
		movingPartsDisplayListsMap.clear();
	}
	
	public void removeFromDisplayListsMap(NBTTagCompound nbt)
	{
		deleteDisplayLists(movingPartsDisplayListsMap.remove(nbt));
	}
	
	private void deleteDisplayLists(List<Integer> displayLists)
	{
		if (displayLists != null)
		{
			for (Integer displayList : displayLists)
				GLAllocation.deleteDisplayLists(displayList);
		}
	}
	
	public static boolean isPlayerModelAlt(EntityLivingBase entity, float partialTicks)
	{
		if (entity instanceof EntityPlayer || (!MorePlayerModelsReference.isLoaded && !CustomNPCsReferences.isLoaded))
			return false;
		
		EntityPlayer player = Minecraft.getMinecraft().player;
		return entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks == player.prevPosX + (player.posX - player.prevPosX) * partialTicks
				&& entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks == player.prevPosY + (player.posY - player.prevPosY) * partialTicks
				&& entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks == player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
	}
	
	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
	{
		updateModelAndRenderers(false);
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		ClientHelper.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		EntityLivingBase entityCap = isPlayerModelAlt(entity, partialTicks) ? Minecraft.getMinecraft().player : entity;
		IChiseledArmorSlotsHandler cap = entityCap instanceof EntityPlayer ? ChiseledArmorSlotsHandler.getCapability((EntityPlayer) entityCap) : null;
		List<Integer> displayListsHelmet = getStackDisplayLists(entity, scale, ArmorType.HELMET);
		List<Integer> displayListsSlotHelmet = getSlotStackDisplayLists(entity, scale, cap, ArmorType.HELMET);
		if (displayListsHelmet != null || displayListsSlotHelmet != null)
		{
			GlStateManager.pushMatrix();
			adjustForSneaking(entity);
			if (entity.isChild() && !(entity instanceof EntityVillager))
			{
				GlStateManager.scale(0.75F, 0.75F, 0.75F);
				GlStateManager.translate(0.0F, 1.0F, 0.0F);
			}
			head.postRender(scale);
			GlStateManager.translate(0.0F, -scale * (8 + Configs.armorZFightingBufferScale), 0.0F);
			GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
			
			if (entity instanceof EntityVillager || entity instanceof EntityZombieVillager || entity instanceof AbstractIllager)
				GlStateManager.translate(0.0F, scale * 2, 0.0F);
			
			GlStateManager.pushMatrix();
			if (displayListsHelmet != null && (cap == null || !cap.hasArmorType(0)))
				GlStateManager.callList(displayListsHelmet.get(0));
			
			GlStateManager.popMatrix();
			if (displayListsSlotHelmet != null)
			{
				for (Integer i : displayListsSlotHelmet)
					GlStateManager.callList(i);
			}
			GlStateManager.popMatrix();
		}
		List<Integer> displayListsChestplate = getStackDisplayLists(entity, scale, ArmorType.CHESTPLATE);
		List<Integer> displayListsSlotChestplate = getSlotStackDisplayLists(entity, scale, cap, ArmorType.CHESTPLATE);
		if (displayListsChestplate != null || displayListsSlotChestplate != null)
		{
			GlStateManager.pushMatrix();
			adjustForSneaking(entity);
			adjustForChildModel();
			boolean isPassive = !isIllager || ((AbstractIllager) entity).getArmPose() == AbstractIllager.IllagerArmPose.CROSSED;
			GlStateManager.pushMatrix();
			if (displayListsChestplate != null && (cap == null || !cap.hasArmorType(1)))
			{
				renderArmorPiece(body, displayListsChestplate.get(0), scale, 8);
				renderSleeve(displayListsChestplate.get(1), EnumHandSide.RIGHT, scale, isPassive);
				renderSleeve(displayListsChestplate.get(2), EnumHandSide.LEFT, scale, isPassive);
			}
			GlStateManager.popMatrix();
			if (displayListsSlotChestplate != null)
			{
				for (int i = 0; i < displayListsSlotChestplate.size(); i += 3)
				{
					renderArmorPiece(body, displayListsSlotChestplate.get(i), scale, 8);
					renderSleeve(displayListsSlotChestplate.get(i + 1), EnumHandSide.RIGHT, scale, isPassive);
					renderSleeve(displayListsSlotChestplate.get(i + 2), EnumHandSide.LEFT, scale, isPassive);
				}
			}
			GlStateManager.popMatrix();
		}
		List<Integer> displayListsLeggings = getStackDisplayLists(entity, scale, ArmorType.LEGGINGS);
		List<Integer> displayListsSlotLeggings = getSlotStackDisplayLists(entity, scale, cap, ArmorType.LEGGINGS);
		if (displayListsLeggings != null || displayListsSlotLeggings != null)
		{
			GlStateManager.pushMatrix();
			adjustForSneaking(entity);
			adjustForChildMo