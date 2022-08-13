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
	
	private byte[] partsToByteArray(Set<ModelMovingPart> parts)
	{
		return Bytes.toArray(parts.stream().map(part -> (byte) part.ordinal()).collect(Collectors.toSet()));
	}
	
	public void saveToNBT(NBTTagCompound nbt)
	{
		savePartsToNBT(nbt, this.concealedParts, NBTKeys.ARMOR_CONCEALED_MODEL_PARTS);
		savePartsToNBT(nbt, this.concealedPartOverlays, NBTKeys.ARMOR_CONCEALED_MODEL_PART_OVERLAYS);
	}
	
	private void savePartsToNBT(NBTTagCompound nbt, Set<ModelMovingPart> parts, String key)
	{
		byte[] partsArray = partsToByteArray(parts);
		if (partsArray.length > 0)
			nbt.setByteArray(key, partsArray);
		else
			nbt.removeTag(key);
	}
	
	@Nullable
	public static ModelPartConcealer loadFromNBT(NBTTagCompound nbt)
	{
		if (!nbt.hasKey(NBTKeys.ARMOR_CONCEALED_MODEL_PARTS) && !nbt.hasKey(NBTKeys.ARMOR_CONCEALED_MODEL_PART_OVERLAYS))
			return null;
		
		byte[] concealedParts = nbt.getByteArray(NBTKeys.ARMOR_CONCEALED_MODEL_PARTS);
		byte[] concealedPartOverlays = nbt.getByteArray(NBTKeys.ARMOR_CONCEALED_MODEL_PART_OVERLAYS);
		return concealedParts.length > 0 || concealedPartOverlays.length > 0 ? new ModelPartConcealer(concealedParts, concealedPartOverlays).copy() : null;
	}
	
	public void merge(@Nullable ModelPartConcealer modelPartConcealer)
	{
		if (modelPartConcealer != null)
		{
			concealedParts.addAll(modelPartConcealer.concealedParts);
			concealedPartOverlays.addAll(modelPartConcealer.concealedPartOverlays);
			concealedPartsCombined.addAll(modelPartConcealer.concealedPartsCombined);
		}
	}
	
	public ModelPartConcealer copy()
	{
		return new ModelPartConcealer(partsToByteArray(concealedParts), partsToByteArray(concealedPartOverlays));
	}
	
	private Set<ModelMovingPart> getParts(boolean isOverlay)
	{
		return isOverlay ? concealedPartOverlays : concealedParts;
	}
	
	public boolean contains(ModelMovingPart part, boolean isOverlay)
	{
		return getParts(isOverlay).contains(part);
	}
	
	public void addOrRemove(int partIndex, boolean isOverlay, boolean remove)
	{
		ModelMovingPart part = ModelMo