package com.phylogeny.extrabitmanipulation.client.render;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.LightUtil;

import org.lwjgl.opengl.GL11;

import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;

public class RenderState
{
	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	
	public static void renderStateIntoGUI(final IBlockState state, int x, int y)
	{
		BlockModelShapes blockModelShapes = ClientHelper.getBlockModelShapes();
		IBakedModel model = blockModelShapes.getModelForState(state);
		boolean emptyModel;
		try
		{
			boolean missingModel = isMissingModel(blockModelShapes, model);
			emptyModel = missingModel || model.getQuads(state, null, 0L).isEmpty();
			if (!missingModel && emptyModel)
			{
				for (EnumFacing enumfacing : EnumFacing.values())
				{
					if (!model.getQuads(state, enumfacing, 0L).isEmpty())
					{
						emptyModel = false;
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			emptyModel = true;
		}
		Block block = state.getBlock();
		ItemStack stack = new ItemStack(block, 1, block.getMetaFromState(state));
		if (isNullItem(block, stack))
			stack = ItemStack.EMPTY;
		
		boolean isVanillaChest = block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
		if (!stack.isEmpty() && emptyModel)
		{
			model = getItemModelWithOverrides(stack);
			if (!isVanillaChest && isMissingModel(blockModelShapes, model))
			{
				stack = new ItemStack(block);
				if (isNullItem(block, stack))
					stack = ItemStack.EMPTY;
				
				if (!stack.isEmpty())
					model = getItemModelWithOverrides(stack);
			}
		}
		boolean renderAsTileEntity = !stack.isEmpty() && (model.isBuiltInRenderer() || isVanillaChest);
		try
		{
			renderStateModelIntoGUI(state, model, stack, renderAsTileEntity, x, y, 0, 0, -1);
		}
		catch (Throwable throwable)
		{
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering block state in " + Reference.MOD_ID + " bit mapping GUI");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Block state being rendered");
			crashreportcategory.addDetail("Block State", new ICrashReportDetail<String>()
			{
				@Override
				public String call() throws Exception
				{
					return String.valueOf(state);
				}
			});
			if (!stack.isEmpty())
			{
				final ItemStack stack2 = stack.copy();
				crashreportcategory.addDetail("State's Item Type", new ICrashReportDetail<String>()
				{
					@Override
					public String call() throws Exception
					{
						return String.valueOf(stack2.getItem());
					}
				});
				crashreportcategory.addDetail("State's Item Aux", new ICrashReportDetail<String>()
				{
					@Override
					public String call() throws Exception
					{
						return String.valueOf(stack2.getMetadata());
					}
				});
				crashreportcategory.addDetail("State's Item NBT", new ICrashReportDetail<String>()
				{
					@Override
					public String call() throws Exception
					{
						return String.valueOf(stack2.getTagCompound());
					}
				});
				crashreportcategory.addDetail("State's Item Foil", new ICrashReportDetail<String>()
				{
					@Override
					public String call() throws Exception
					{
						return String.valueOf(stack2.hasEffect());
					}
				});
			}
			throw new ReportedException(crashreport);
		}
	}
	
	public static IBakedModel getItemModelWithOverrides(ItemStack stack)
	{
		return ClientHelper.getRenderItem().getItemModelWithOverrides(stack, null, ClientHelper.getPlayer());
	}
	
	private static boolean isNullItem(final Block block, ItemStack stack)
	{
		return stack.getItem() == null || block == Blocks.STANDING_BANNER || block == Blocks.BARRIER;
	}
	
	private static boolean isMissingModel(BlockModelShapes blockModelShapes, IBakedModel model)
	{
		return model.equals(blockModelShapes.getModelManager().getMissingModel());
	}
	
	public static void renderStateModelIntoGUI(IBlockState state, IBakedModel model, ItemStack stack,
			boolean renderAsTileEntity, int x, int y, float angleX, float angleY, float scale)
	{
		renderStateModelIntoGUI(state, model, stack, 1.0F, false, renderAsTileEntity, x, y, angleX, angleY, scale);
	}
	
	public static void renderStateModelIntoGUI(IBlockState state, IBakedModel model, ItemStack stack, float alphaMultiplier,
			boolean transformFroGui, boolean renderAsTileEntity, int x, int y, float angleX, float ang