package com.phylogeny.extrabitmanipulation.armor;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.api.ItemType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;

public class ArmorItem
{
	private List<GlOperation> glOperations = new ArrayList<GlOperation>();
	private ItemStack stack;
