package com.phylogeny.extrabitmanipulation.client.gui.armor;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.ext