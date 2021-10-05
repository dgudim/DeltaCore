package com.deo.flapd.utils.postprocessing.effects;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.deo.flapd.utils.postprocessing.PostProcessorEffect;
import com.deo.flapd.utils.postprocessing.filters.RadialDistortion;

public final class Curvature extends PostProcessorEffect {
	private final RadialDistortion distort;

	public Curvature () {
		distort = new RadialDistortion();
	}

	@Override
	public void dispose () {
		distort.dispose();
	}

	public void setDistortion (float distortion) {
		distort.setDistortion(distortion);
	}

	public void setZoom (float zoom) {
		distort.setZoom(zoom);
	}

	public float getDistortion () {
		return distort.getDistortion();
	}

	public float getZoom () {
		return distort.getZoom();
	}

	@Override
	public void rebind () {
		distort.rebind();
	}

	@Override
	public void render (FrameBuffer src, FrameBuffer dest) {
		restoreViewport(dest);
		distort.setInput(src).setOutput(dest).render();
	}

}
