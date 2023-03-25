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
		int y = 127;
		String text = " the item list of the selected moving part of the selected armor piece.";
		buttonItemAdd = createButtonListInteraction(46, y, TEXTURE_ADD, "",
				"Adds an empty slot to" + text + "\n\nIf the scale of the " +
				"armor piece is not 1:1, a GL scale operation will be added to its GL operations list automatically.");
		buttonItemDelete = createButtonListInteraction(60, y, TEXTURE_DELETE, "Remove item",
				"Removes the selected slot from" + text);
		buttonGlAdd = createButtonListInteraction(107, y, TEXTURE_ADD, "",
				"Opens a selection screen, where the chosen GL operation is added to the list above, under the selected operation.");
		buttonGlDelete = createButtonListInteraction(121, y, TEXTURE_DELETE, "",
				"Removes the selected GL operation from the list above.");
		text = "Moves the selected GL operation @ in the list above. This is important, as changing the order of operations can have a significant effect." +
				"\n\n" + getPointExample() + " translating in the x or z axis can result in a translation is any absolute " +
				"direction, if performed after rotating in the y axis.";
		buttonGlMoveUp = createButtonListInteraction(144, y, TEXTURE_MOVE_UP, "Move GL operation up",
				text.replace("@", "up"));
		buttonGlMoveDown = createButtonListInteraction(158, y, TEXTURE_MOVE_DOWN, "Move GL operation down",
				text.replace("@", "down"));
		buttonGlMoveUp.setRightOffsetX(0.5F, false);
		buttonGlMoveDown.setRightOffsetX(0.5F, true);
		y = 50;
		buttonAddRotation = createButtonAddGlOperation(109, y, TEXTURE_ROTATION, 0);
		buttonAddTranslation = createButtonAddGlOperation(170, y, TEXTURE_TRANSLATION, 1);
		buttonAddScale = createButtonAddGlOperation(231, y, TEXTURE_SCALE, 2);
		buttonHelp = new GuiButtonHelp(100, buttonList, guiLeft + xSize - 41, guiTop + 5, "Show more explanatory hover text", "Exit help mode");
		buttonScale = new GuiButtonSelect(100, guiLeft + 188, guiTop + 127, 17, 12, "", "Armor piece scale", -5111553, -5111553);
		buttonScale.setTextOffsetX(1);
		buttonScale.setHoverHelpText("This is the scale of the selected armor piece. Just as all newly imported blocks are scaled by this amount " +
				"when collecting them in-world, all newly added slots to any of the selected armor pieces' moving parts are likewise scaled by this amount.");
		buttonList.add(buttonGlItems);
		buttonList.add(buttonGlPre);
		buttonList.add(buttonGlPost);
		buttonList.add(buttonFullIlluminationOff);
		buttonList.add(buttonFullIlluminationOn);
		buttonList.add(buttonPlayerFollowCursor);
		buttonList.add(buttonPlayerRotate);
		buttonList.add(buttonScalePixel);
		buttonList.add(buttonScaleMeter);
		buttonList.add(buttonItemAdd);
		buttonList.add(buttonItemDelete);
		buttonList.add(buttonGlAdd);
		buttonList.add(buttonGlDelete);
		buttonList.add(buttonGlMoveUp);
		buttonList.add(buttonGlMoveDown);
		buttonList.add(buttonAddRotation);
		buttonList.add(buttonAddTranslation);
		buttonList.add(buttonAddScale);
		buttonList.add(buttonHelp);
		buttonList.add(buttonScale);
		emptyGlList = createGuiListGlOperation(null, GlOperationListType.ARMOR_ITEM);
		for (int i = 0; i < armorPieces.length; i++)
		{
			ItemStack stack = getArmorStack(i);
			if (ItemStackHelper.isChiseledArmorStack(stack))
				armorPieces[i] = new DataChiseledArmorPiece(ItemStackHelper.getNBTOrNew(stack), ArmorType.values()[i]);
			
			ArmorType armorType = ArmorType.values()[i];
			ItemChiseledArmor armorItem =  stack.getItem() instanceof ItemChiseledArmor ? (ItemChiseledArmor) stack.getItem() : getArmorItem(armorType);
			GuiButtonTab tab = new GuiButtonTab(i * 4, guiLeft, guiTop + 23 + i * 25, 24, 25,
					armorType.getName(), true, new ItemStack(armorItem), 19, 230, 0, 512, TEXTURE_GUI);
			tab.setHoverHelpText("Armor Piece: " + armorType.getName() + "\n\nEach of these 4 tabs represents a worn chiseled armor piece. " +
					"If a tab is disabled, the corresponding armor slot is either empty or contains a different kind of armor.");
			DataChiseledArmorPiece armorPiece = armorPieces[i];
			tab.enabled = armorPiece != null;
			if (selectedTabIndex < 0 && tab.enabled)
				selectedTabIndex = i;
			
			if (i == selectedTabIndex)
				tab.selected = true;
			
			tabButtons[i][0] = tab;
			buttonList.add(tab);
			if (armorPiece == null)
				continue;
			
			ArmorMovingPart[] movingParts = armorItem.MOVING_PARTS;
			for (int j = 0; j < movingParts.length; j++)
			{
				GuiButtonTab tabSub = new GuiButtonTab(i * 4 + j + 1, guiLeft, guiTop + 147 + j * 25, 24, 25,
						movingParts[j].getName(), true, 19, 230, 0, 512, TEXTURE_GUI, movingParts[j].getIconModels(armorItem.getArmorMaterial()));
				tabSub.setHoverHelpText("Moving Part: " + movingParts[j].getName() + "\n\nEach of these tabs represents a moving part of the armor piece " +
						"specified by the selected armor piece tab.");
				if (j + 1 == selectedSubTabIndex)
					tabSub.selected = true;
				
				tabButtons[i][j + 1] = tabSub;
				buttonList.add(tabSub);
				for (int k = 0; k < 2; k++)
				{
					ModelMovingPart part = movingParts[j].getModelMovingPart();
					String name, textOverlay;
					int offsetX;
					if (k == 0)
					{
						name = part.getName();
						textOverlay = "";
						offsetX = 0;
					}
					else
					{
						name = part.getOverlayName();
						textOverlay = " Overlay";
						offsetX = 14;
					}
					GuiButtonTextured checkBox = GuiButtonTextured.createCheckBox(1200 + j + concealmentCheckBoxes[0].length * k, guiLeft + 29 + offsetX, guiTop + 153 + j * 25, 12, 12,
							"Hide player model " + name);
					checkBox.setHoverHelpText("Model Moving Part" + textOverlay + ": " + name +
							"\n\nPrevents the player model's corresponding moving part" + textOverlay.toLowerCase() + " from rendering when the armor piece is worn in a vanity slot.");
					buttonList.add(concealmentCheckBoxes[i][j][k] = checkBox);
				}
			}
			globalPreGlLists[i] = createGuiListGlOperation(armorPiece, GlOperationListType.GLOBAL_PRE);
			globalPostGlLists[i] = createGuiListGlOperation(armorPiece, GlOperationListType.GLOBAL_POST);
			for (int j = 0; j < armorItemLists[0].length; j++)
			{
				if (tabButtons[i][j + 1] == null)
					continue;
				
				armorItemLists[i][j] = new GuiListArmorItem(this, height, guiTop + 24, guiTop + 124, 20, 38, 49, armorPiece, j);
				List<GuiListGlOperation> armorItemGlList = new ArrayList<GuiListGlOperation>();
				List<ArmorItem> armorItems = armorPiece.getArmorItemsForPart(j);
				for (int k = 0; k < armorItems.size(); k ++)
					armorItemGlList.add(createGuiListGlOperation(armorPiece, GlOperationListType.ARMOR_ITEM, j, k));
				
				armorItemGlLists[i][j] = armorItemGlList;
			}
		}
		for (int i = 0; i < ChiseledArmorSlotsHandler.COUNT_SETS + 1; i++)
		{
			String hoverText = i == 0 ? "Main Armor Set" : "Vanity Armor Set " + i;
			GuiButtonTab tab = new GuiButtonTab(1100 + i, guiLeft + xSize - 24, guiTop + 23 + i * 25, 24, 25,
					hoverText, false, ItemStack.EMPTY, 183, 230, 3, 512, TEXTURE_GUI);
			tab.enabled = setHasArmor(i);
			tab.selected = i == indexArmorSet;
			if (i == 0)
			{
				tab.displayString = "M";
				tab.setHoverHelpText(hoverText, " ", "The players standard set of armor.");
			}
			else
			{
				tab.displayString = "V" + i;
				tab.setHoverHelpText(hoverText, " ", "Armor worn in a vanity slot, which will render " +
				TextFormatting.BLUE + (i < 2 ? "in place of" : "in addition to") + TextFormatting.WHITE + " the main armor.");
			}
			tabButtonsArmorSet[i] = tab;
			buttonList.add(tab);
		}
		updateButtons();
		refreshLists(false);
	}
	
	private boolean setHasArmor(int indexSet)
	{
		for (ArmorType armorType : ArmorType.values())
		{
			if (ItemStackHelper.isChiseledArmorStack(ItemStackHelper.getChiseledArmorStack(ClientHelper.getPlayer(), armorType, indexSet)))
				return true;
		}
		return false;
	}
	
	private ItemChiseledArmor getArmorItem(ArmorType armorType)
	{
		switch (armorType)
		{
			case HELMET:		return (ItemChiseledArmor) ItemsExtraBitManipulation.chiseledHelmetDiamond;
			case CHESTPLATE:	return (ItemChiseledArmor) ItemsExtraBitManipulation.chiseledChestplateDiamond;
			case LEGGINGS:		return (ItemChiseledArmor) ItemsExtraBitManipulation.chiseledLeggingsDiamond;
			case BOOTS:			return (ItemChiseledArmor) ItemsExtraBitManipulation.chiseledBootsDiamond;
			default:			return null;
		}
	}
	
	public static String getPointMain(String point)
	{
		return TextFormatting.AQUA + point + TextFormatting.RESET;
	}
	
	public static String getPointSub(String point)
	{
		return "    " + TextFormatting.GREEN + point + TextFormatting.RESET;
	}
	
	public static String getPointExample()
	{
		return TextFormatting.YELLOW + "Ex:" + TextFormatting.RESET;
	}
	
	public static String underlineText(String text)
	{
		return TextFormatting.UNDERLINE + text + TextFormatting.RESET;
	}
	
	private ArmorType getArmorSlot(int index)
	{
		return ArmorType.values()[index];
	}
	
	private ItemStack getArmorStack(int index)
	{
		return ItemStackHelper.getChiseledArmorStack(ClientHelper.getPlayer(), getArmorSlot(index), indexArmorSet);
	}
	
	private GuiListGlOperation createGuiListGlOperation(DataChiseledArmorPiece armorPiece)
	{
		return createGuiListGlOperation(armorPiece, buttonGlItems.selected ? GlOperationListType.ARMOR_ITEM
				: (buttonGlPre.selected ? GlOperationListType.GLOBAL_PRE : GlOperationListType.GLOBAL_POST),
				selectedSubTabIndex - 1, getSelectedGuiListArmorItem().getSelectListEntryIndex());
	}
	
	private GuiListGlOperation createGuiListGlOperation(@Nullable DataChiseledArmorPiece armorPiece, GlOperationListType type)
	{
		return createGuiListGlOperation(armorPiece, type, 0, 0);
	}
	
	private GuiListGlOperation createGuiListGlOperation(@Nullable DataChiseledArmorPiece armorPiece,
			GlOperationListType type, int partIndex, int armorItemIndex)
	{
		return type == GlOperationListType.ARMOR_ITEM ? new GuiListGlOperationItem(this, height,
				guiTop + 24, guiTop + 124, 16, 91, 220, armorPiece, partIndex, armorItemIndex)
			: new GuiListGlOperationGlobal(this, height, guiTop + 24, guiTop + 124, 16, 91, 220, armorPiece, type == GlOperationListType.GLOBAL_PRE);
	}
	
	private GuiButtonSelectTextured createButtonAddGlOperation(int offsetX, int offsetY, ResourceLocation texture, int titleIndex)
	{
		GuiButtonSelectTextured button = createButtonToggled(offsetX, offsetY, 52, 48,
				GL_OPERATION_TITLES[titleIndex], glOperationHoverHelpText, -38400, -38400, texture);
		button.visible = false;
		return button;
	}
	
	private GuiButtonSelectTextured createButtonListInteraction(int offsetX, int offsetY, ResourceLocation texture, String hoverText, String hoverHelpText)
	{
		return createButtonToggled(offsetX, offsetY, 12, 12, hoverText, hoverHelpText, -16739073, -16739073, texture);
	}
	
	private GuiButtonSelectTextured createButtonToggled(int offsetX, int offsetY, ResourceLocation texture, String hoverText, String hoverHelpText)
	{
		return createButtonToggled(offsetX, offsetY, 12, 12, hoverText, hoverText + "\n\n" + hoverHelpText, -16726016, -8882056, texture);
	}
	
	private GuiButtonSelectTextured createButtonToggled(int offsetX, int offsetY, int width, int height,
			String hoverText, String hoverHelpText, int colorSelected, int colorDeselected, ResourceLocation texture)
	{
		GuiButtonSelectTextured button = new GuiButtonSelectTextured(100, guiLeft + offsetX,
				guiTop + offsetY, width, height, "", hoverText, colorSelected, colorDeselected, texture);
		button.setHoverHelpText(hoverHelpText);
		return button;
	}
	
	private GuiButtonSelect createButtonGlobalGl(int id, int offsetX, int offsetY, String text)
	{
		return createButtonGl(id, offsetX, offsetY, text, "Performed @ each item's GL operations".replace("@", text.toLowerCase()),
				"Before and after a given item's list of GL operations are performed, a global list of operations are performed. For each moving part " +
				"of an armor piece, rendering occurs as follows:\n" + getPointMain("1") + " Perform global pre-operations.\n" + getPointMain("2") + 
				" For each item:\n" + getPointSub("a") + " Perform the item's operations.\n" + getPointSub("b") + " Perform global post-operations.\n" +
				getPointSub("c") + " Render the item.\n\nIf this button is selected, the list of global " +
				text.replace("Before", "pre").replace("After", "post") + "-operations will be displayed in the area below.");
	}
	
	private GuiButtonSelect createButtonGl(int id, int offsetX, int offsetY, String text, String hoverText, String hoverHelpText)
	{
		GuiButtonSelect button = new GuiButtonSelect(id, guiLeft + offsetX, guiTop + offsetY, fontRenderer.getStringWidth(text) + 3,
				12, text, hoverText, -16726016, 0);
		button.setHoverHelpText(hoverHelpText);
		return button;
	}
	
	private GuiListArmorItem getSelectedGuiListArmorItem()
	{
		return armorItemLists[selectedTabIndex][selectedSubTabIndex - 1];
	}
	
	private GuiListGlOperation getSelectedGuiListGlOperation()
	{
		if (!buttonGlItems.selected)
			return buttonGlPre.selected ? globalPreGlLists[selectedTabIndex] : globalPostGlLists[selectedTabIndex];
		
		List<GuiListGlOperation> list = getSelectedGuiListArmorItemGlOperations();
		int index = getSelectedGuiListArmorItem().getSelectListEntryIndex();
		return index < list.size() ? list.get(index) : emptyGlList;
	}
	
	private List<GuiListGlOperation> getSelectedGuiListArmorItemGlOperations()
	{
		return armorItemGlLists[selectedTabIndex][selectedSubTabIndex - 1];
	}
	
	public boolean scalePixel()
	{
		return buttonScalePixel.selected;
	}
	
	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		if (!playerBoxClicked && !buttonAddRotation.visible)
		{
			getSelectedGuiListArmorItem().handleMouseInput();
			getSelectedGuiListGlOperation().handleMouseInput();
		}
		Pair<Vec3d, Float> pair = GuiHelper.scaleObjectWithMouseWheel(this, boxPlayer, playerTranslation, playerScale, 5.0F, 40.25F);
		playerTranslation = pair.getLeft();
		playerScale = pair.getRight();
	}
	
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
	{
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		if (!playerBoxClicked)
			return;
		
		float deltaX = mouseInitialX - mouseX;
		float deltaY = mouseInitialY - mouseY;
		if (clickedMouseButton == 0)
		{
			mouseInitialX = mouseX;
			mouseInitialY = mouseY;
		}
		Triple<Vec3d, Vec3d, Float> triple = GuiHelper.dragObject(clickedMouseButton, deltaX, deltaY,
				playerTranslationInitial, playerRotation, playerScale, 5.0F, 2.0F, 3.0F, buttonPlayerRotate.selected);
		playerTranslation = triple.getLeft();
		playerRotation = triple.getMiddle();
		playerScale = triple.getRight();
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (!buttonAddRotation.visible)
		{
			getSelectedGuiListArmorItem().mouseClicked(mouseX, mouseY, mouseButton);
			getSelectedGuiListGlOperation().mouseClicked(mouseX, mouseY, mouseButton);
		}
		else
		{
			if (!buttonGlAdd.isMouseOver() && (!GuiHelper.isCursorInsideBox(boxGlOperation, mouseX, mouseY)
					|| buttonAddRotation.isMouseOver() || buttonAddTranslation.isMouseOver() || buttonAddScale.isMouseOver()))
			{
				hideAddGlButtons();
			}
		}
		playerBoxClicked = GuiHelper.isCursorInsideBox(boxPlayer, mouseX, mouseY);
		mouseInitialX = mouseX;
		mouseInitialY = mouseY;
		playerTranslationInitial = new Vec3d(playerTranslation.x, playerTranslation.y, 0);
	}
	
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state)
	{
		super.mouseReleased(mouseX, mouseY, state);
		if (!buttonAddRotation.visible)
		{
			getSelectedGuiListArmorItem().mouseReleased(mouseX, mouseY, state);
			getSelectedGuiListGlOperation().mouseReleased(mouseX, mouseY, state);
		}
		mouseInitialX = 0;
		mouseInitialY = 0;
		playerBoxClicked = false;
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		getSelectedGuiListGlOperation().updateScreen();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (playerBoxClicked || buttonAddRotation.visible)
		{
			if (buttonAddRotation.visible && buttonHelp.selected
					&& (buttonAddRotation.isMouseOver() || buttonAddTranslation.isMouseOver() || buttonAddScale.isMouseOver()))
				drawHoveringText(buttonAddRotation.getHoverText(), mouseX, mouseY);
			
			return;
		}
		renderHoveredToolTip(mouseX, mouseY);
		GuiHelper.drawHoveringTextForButtons(this, buttonList, mouseX, mouseY);
		GuiListGlOperation glOperationsList = getSelectedGuiListGlOperation();
		if (GuiHelper.isCursorInsideBox(boxGlOperation, mouseX, mouseY))
		{
			for (int i = 0; i < glOperationsList.getSize(); i++)
			{
				GuiListEntryGlOperation entry = (GuiListEntryGlOperation) glOperationsList.getListEntry(i);
				if (entry.isElementHovered(buttonHelp.selected))
				{
					drawHoveringText(entry.getElementHoverText(buttonHelp.selected ? glOperationHoverHelpText : null), mouseX, mouseY);
					break;
				}
		