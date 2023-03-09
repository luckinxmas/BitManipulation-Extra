package com.phylogeny.extrabitmanipulation.client.gui.armor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.armor.ArmorItem;
import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.armor.GlOperation;
import com.phylogeny.extrabitmanipulation.armor.GlOperation.GlOperationType;
import com.phylogeny.extrabitmanipulation.armor.ModelPartConcealer;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.GuiHelper;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonHelp;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonSelect;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonSelectTextured;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonTab;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonTextured;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorMovingPart;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ModelMovingPart;
import com.phylogeny.extrabitmanipulation.packet.PacketChangeArmorItemList;
import com.phylogeny.extrabitmanipulation.packet.PacketChangeArmorItemList.ListOperation;
import com.phylogeny.extrabitmanipulation.packet.PacketChangeGlOperationList;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenChiseledArmorGui;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelPartConcealed;
import com.phylogeny.extrabitmanipulation.proxy.ProxyCommon;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;

public class GuiChiseledArmor extends GuiContainer
{
	public static final ResourceLocation TEXTURE_GUI = new ResourceLocation(Reference.MOD_ID, "textures/guis/chiseled_armor.png");
	public static final int HELP_TEXT_BACKGROUNG_COLOR = 1694460416;
	private static final ResourceLocation TEXTURE_ROTATION = new ResourceLocation(Reference.MOD_ID, "textures/guis/rotation_large.png");
	private static final ResourceLocation TEXTURE_TRANSLATION = new ResourceLocation(Reference.MOD_ID, "textures/guis/translation_large.png");
	private static final ResourceLocation TEXTURE_SCALE = new ResourceLocation(Reference.MOD_ID, "textures/guis/scale_large.png");
	private static final ResourceLocation TEXTURE_ILLUMINATION_OFF = new ResourceLocation(Reference.MOD_ID, "textures/guis/full_illumination_off.png");
	private static final ResourceLocation TEXTURE_ILLUMINATION_ON = new ResourceLocation(Reference.MOD_ID, "textures/guis/full_illumination_on.png");
	private static final ResourceLocation TEXTURE_PLAYER_FOLLOW_CURSOR = new ResourceLocation(Reference.MOD_ID, "textures/guis/player_follow_cursor.png");
	private static final ResourceLocation TEXTURE_PLAYER_ROTATE = new ResourceLocation(Reference.MOD_ID, "textures/guis/player_rotate.png");
	private static final ResourceLocation TEXTURE_SCALE_PIXEL = new ResourceLocation(Reference.MOD_ID, "textures/guis/scale_pixel.png");
	private static final ResourceLocation TEXTURE_SCALE_METER = new ResourceLocation(Reference.MOD_ID, "textures/guis/scale_meter.png");
	private static final ResourceLocation TEXTURE_ADD = new ResourceLocation(Reference.MOD_ID, "textures/guis/add.png");
	private static final ResourceLocation TEXTURE_DELETE = new ResourceLocation(Reference.MOD_ID, "textures/guis/delete.png");
	private static final ResourceLocation TEXTURE_MOVE_UP = new ResourceLocation(Reference.MOD_ID, "textures/guis/move_up.png");
	private static final ResourceLocation TEXTURE_MOVE_DOWN = new ResourceLocation(Reference.MOD_ID, "textures/guis/move_down.png");
	private static final String[] GL_OPERATION_TITLES = new String[]{"Rotation", "Translation", "Scale"};
	private static final String[] GL_OPERATION_DATA_TITLES = new String[]{"X component", "Y component", "Z component", "Angle"};
	private static String glOperationHoverHelpText, glOperationHoverKeysHelpText;
	private static final float PLAYER_HEIGHT_HALF = 37.25F;
	private static final float PLAYER_HEIGHT_EYES = 64.5F;
	private GuiListArmorItem[][] armorItemLists = new GuiListArmorItem[4][3];
	private List<GuiListGlOperation>[][] armorItemGlLists = new ArrayList[4][3];
	private GuiListGlOperation[] globalPreGlLists = new GuiListGlOperation[4];
	private GuiListGlOperation[] globalPostGlLists = new GuiListGlOperation[4];
	private DataChiseledArmorPiece[] armorPieces = new DataChiseledArmorPiece[4];
	private GuiButtonTab[][] tabButtons = new GuiButtonTab[4][4];
	private GuiButtonTextured[][][] concealmentCheckBoxes = new GuiButtonTextured[4][3][2];
	private GuiButtonTab[] tabButtonsArmorSet = new GuiButtonTab[5];
	private GuiListGlOperation emptyGlList;
	private GuiButtonSelectTextured buttonFullIlluminationOff, buttonFullIlluminationOn, buttonPlayerFollowCursor, buttonPlayerRotate, buttonScalePixel,
									buttonScaleMeter, buttonItemAdd, buttonItemDelete, buttonGlAdd, buttonGlDelete, buttonGlMoveUp, buttonGlMoveDown,
									buttonAddRotation, buttonAddTranslation, buttonAddScale;
	private GuiButtonSelect buttonGlItems, buttonGlPre, buttonGlPost, buttonScale;
	private GuiButtonHelp buttonHelp;
	private AxisAlignedBB boxPlayer, boxArmorItem, boxGlOperation, boxTitleItems, boxTitleGlOperations;
	private AxisAlignedBB[] boxesData = new AxisAlignedBB[4];
	private boolean playerBoxClicked;
	private int indexArmorSet,selectedTabIndex, selectedSubTabIndex, mouseInitialX, mouseInitialY;
	private float playerScale;
	private Vec3d playerRotation, playerTranslation, playerTranslationInitial;
	private ItemStack copiedArmorItem = ItemStack.EMPTY;
	private NBTTagCompound copiedArmorItemGlOperations = new NBTTagCompound();
	private NBTTagCompound copiedGlOperation;
	private boolean waitingForServerResponse;
	
	public GuiChiseledArmor(EntityPlayer player)
	{
		super(ProxyCommon.createArmorContainer(player));
		int index = BitToolSettingsHelper.getArmorSetTabIndex();
		indexArmorSet = setHasArmor(index) ? index : ChiseledArmorSlotsHandler.findNextArmorSetIndex(ChiseledArmorSlotsHandler.COUNT_SETS);
		xSize = 352;
		ySize = 230;
		selectedSubTabIndex = 1;
		index = BitToolSettingsHelper.getArmorTabIndex();
		selectedTabIndex = ItemStackHelper.isChiseledArmorStack(getArmorStack(index)) ? index : -1;
		resetRotationAndScale();
		playerTranslation = Vec3d.ZERO;
		playerTranslationInitial = Vec3d.ZERO;
	}
	
	private void resetRotationAndScale()
	{
		playerRotation = new Vec3d(30, -45, 0);
		playerScale = 1F;
	}
	
	public void refreshListsAndSelectEntry(int selectedEntry, boolean isArmorItem, boolean scrollToEnd, int glListRemovalIndex)
	{
		GuiListChiseledArmor list = isArmorItem ? getSelectedGuiListArmorItem() : getSelectedGuiListGlOperation();
		if (glListRemovalIndex >= 0 && glListRemovalIndex < getSelectedGuiListArmorItemGlOperations().size())
			getSelectedGuiListArmorItemGlOperations().remove(glListRemovalIndex);
		
		if (selectedEntry >= 0)
			list.selectListEntry(selectedEntry);
		
		if (isArmorItem && scrollToEnd)
		{
			List<GuiListGlOperation> list2 = getSelectedGuiListArmorItemGlOperations();
			int index = getSelectedGuiListArmorItem().getSelectListEntryIndex();
			while (index >= list2.size())
			{
				list2.add(createGuiListGlOperation(getSelectedGuiListArmorItem().armorPiece));
			}
		}
		refreshLists(true);
		if (scrollToEnd)
			list.scrollBy(Integer.MAX_VALUE);
		
		waitingForServerResponse = false;
	}
	
	private void refreshLists(boolean onlySelected)
	{
		for (int i = 0; i < armorItemLists.length; i++)
		{
			ItemStack stack = getArmorStack(i);
			if (ItemStackHelper.isChiseledArmorStack(stack))
				armorPieces[i].loadFromNBT(ItemStackHelper.getNBTOrNew(stack));
			
			for (int j = 0; j < armorItemLists[0].length; j++)
			{
				GuiListArmorItem armorItemList = armorItemLists[i][j];
				if (armorItemList == null)
					continue;
				
				armorItemList.refreshList();
				if (onlySelected && i != selectedTabIndex)
					continue;
				
				for (int k = 0; k < armorItemGlLists[i][j].size(); k++)
				{
					GuiListGlOperation armorItemGlList = armorItemGlLists[i][j].get(k);
					if (armorItemGlList instanceof GuiListGlOperationItem)
						((GuiListGlOperationItem) armorItemGlList).refreshList(k);
					else
						armorItemGlList.refreshList();
				}
			}
			if (armorPieces[i] != null && (!onlySelected || i == selectedTabIndex))
			{
				globalPreGlLists[i].refreshList();
				globalPostGlLists[i].refreshList();
			}
		}
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		int left = guiLeft + 223;
		int top = guiTop + 130;
		boxPlayer = new AxisAlignedBB(left, top, -1, left + 85, top + 92, 1);
		left = guiLeft + 91;
		top = guiTop + 36;
		boxGlOperation = new AxisAlignedBB(left, top, -1, left + 212, top + 88, 1);
		left = guiLeft + 37;
		top = guiTop + 24;
		boxArmorItem = new AxisAlignedBB(left, top, -1, left + 41, top + 100, 1);
		left = guiLeft + 39;
		top = guiTop + 9;
		boxTitleItems = new AxisAlignedBB(left, top, -1, left + 33, top + 14, 1);
		left = guiLeft + 92;
		boxTitleGlOperations = new AxisAlignedBB(left, top, -1, left + 77, top + 14, 1);
		left = guiLeft + 126;
		top = guiTop + 24;
		for (int i = 0; i < boxesData.length; i++)
		{
			int left2 = left + i * 45;
			int width = 10;
			if (i == 3)
			{
				left2 -= 8;
				width++;
			}
			boxesData[i] = new AxisAlignedBB(left2, top, -1, left2 + width, top + 11, 1);
		}
		glOperationHoverKeysHelpText = "\n\nKey presses manipulate GL operations as follows:\n" + getPointMain("Control + C") + " copy\n" +
				getPointMain("Control + V") + " paste\n" + getPointMain("Delete") + " delete\n" + getPointMain("Up Arrow") + " move up\n" +
				getPointMain("Down Arrow") + " move down";
		glOperationHoverHelpText = "Operations, like this, cause the items of armor piece's moving parts to either rotate (by the given angle), " +
				"translate, or scale in the given axes." + glOperationHoverKeysHelpText;
		buttonGlItems = createButtonGl(100, 209, 11, "Items", "Performed for the selected item",
				"Each rendered item for each moving part of an armor piece can have a list of GL operations applied to it.\n\nIf this button is selected, " +
				"and if the selected moving part of the selected armor piece (indicated by the tabs on the left of the GUI) have any items to render, " +
				"then the area below will display the list of operations for the selected item in the area to the left.");
		buttonGlItems.setTextOffsetX(1);
		buttonGlItems.selected = true;
		buttonGlPre = createButtonGlobalGl(100, 239, 11, "Before");
		buttonGlPost = createButtonGlobalGl(100, 278, 11, "After");
		int x = 311;
		String prefix = "The player model will ";
		buttonFullIlluminationOff = createButtonToggled(x, 140, TEXTURE_ILLUMINATION_OFF, "Standard illumination",
				prefix + "render with its faces darkened based on rotation, as it does in-world.");
		buttonFullIlluminationOn = createButtonToggled(x, 154, TEXTURE_ILLUMINATION_ON, "Full illumination",
				prefix + "render without darkening any of its faces.");
		String suffix1 = " can be interacted with as follows:\n" + getPointMain("1") + " Translated by right-clicking and dragging.\n" +
				getPointMain("2") + " Scaled by:\n" + getPointSub("a") + " Scrolling the mouse wheel.\n" + getPointSub("b") +
				" Right-clicking and dragging while holding shift.";
		String suffix2 = "\n\nAlter the player model's orientation by pressing the following keys:\n" + getPointMain("C") +
				" centers it\n" + getPointMain("R") + " resets its translation/rotation/scale";
		buttonPlayerFollowCursor = createButtonToggled(x, 186, TEXTURE_PLAYER_FOLLOW_CURSOR, "Look at cursor",
				prefix + "bend and twist to look at the cursor. In this mode, the model cannot be rotated, but" + suffix1 + suffix2);
		buttonPlayerRotate = createButtonToggled(x, 200, TEXTURE_PLAYER_ROTATE, "Left-click & drag to rotate", prefix + "only look forward. " +
				"In this mode, the model" + suffix1 + "\n" + getPointMain("3") + " Rotated by left-clicking and dragging." + suffix2);
		prefix = "The x/y/z data of translation operations will display in";
		buttonScalePixel = createButtonToggled(x, 67, TEXTURE_SCALE_PIXEL, "Translation data in pixels",
				prefix + " 1/16 meters, i.e. pixels.\n\n" + getPointExample() + " 2 = 2/16 of a meter");
		buttonScaleMeter = createButtonToggled(x, 81, TEXTURE_SCALE_METER, "Translation data in meters",
				prefix + " meters.\n\n" + getPointExample() + " 2 = 2 meters");