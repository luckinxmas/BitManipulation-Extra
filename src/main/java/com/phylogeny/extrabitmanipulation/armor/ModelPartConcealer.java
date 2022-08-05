package com.phylogeny.extrabitmanipulation.armor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.primitives.Bytes;
import com.phylogeny.extrabitmanipulation.armor.model.cnpc.CustomNPCsModels;
import com.phylogeny.extrabitmanipulation.armor.model.mpm.MorePlayerModelsModels;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ModelMovingPart;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.nbt.NBTTagCompound;

public class ModelPartConcealer
{
	private Set<ModelMovingPart> concealedParts = new HashSet<>();
	private Set<ModelMovingPart> concealedPartOverlays = new HashSet<>();
	private Set<ModelMovingPart> concealedPartsCombined = new HashSet<>();
	private Map<ModelMovingPart, ModelRenderer> concealedPartRenderers = new HashMap<>();
	
	public ModelPartConcealer() {}
	
	private ModelPartConcealer(byte[] concealedParts, byte[] concealedPartOverlays)
	{
		concealedPartsCombined.addAll(this.concealedParts = indexArrayToPartSet(concealedParts));
		concealedPartsCombined.addAll(this.concealedPartOverlays = indexArrayToPartSet(concealedPartOverlays));
	}
	
	private Set<ModelMovingPart> indexArrayToPartSet(byte[] parts)
	{
		return IntStream.range(0, parts.length).boxed().map(index -> ModelMovingPart.values()[parts[index]]).collect(Collectors.toSet());
	}
	
	public boolean isEmpty()
	{
		return concealedPartsCombined.isEmpty();
	}
	
	public boolean isFull()
	{
		return concealedParts.size() == ModelMovingPart.values().length && concealedPartOverlays.size() == ModelMovingPart.values().length;
	}
	
	private byte[] partsT