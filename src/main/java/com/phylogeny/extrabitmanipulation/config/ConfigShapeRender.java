package com.phylogeny.extrabitmanipulation.config;

public class ConfigShapeRender extends ConfigNamed
{
	public boolean renderOuterShape, renderInnerShape;
	private boolean renderOuterShapeDefault, renderInnerShapeDefault;
	public float outerShapeAlpha, innerShapeAlpha, red, green, blue;
	private int outerShapeAlphaDefault, innerShapeAlphaDefault, redDefault, greenDefault, blueDefault;
	public float lineWidth;
	private float lineWidthDefault;
	
	public ConfigShapeRender(String title, boolean renderOuterShapeDefault, boolean renderInnerShapeDefault, int outerShapeAlphaDefault,
			int innerShapeAlphaDefault, int redDefault, int greenDefault, int blueDefault, float lineWidthDefault)
	{
		super(title);
		this.renderOuterShapeDefault = renderOuterShapeDefault;
		this.renderInnerShapeDefault = renderInnerShapeDefault;
		