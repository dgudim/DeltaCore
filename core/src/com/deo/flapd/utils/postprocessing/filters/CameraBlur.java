package com.deo.flapd.utils.postprocessing.filters;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.deo.flapd.utils.postprocessing.ShaderLoader;

public final class CameraBlur extends Filter<CameraBlur> {

	private Texture normaldepth = null;
	private final Vector2 viewport = new Vector2();

	public enum Param implements Parameter {
		// @formatter:off
		InputScene("u_texture0"), DepthMap("u_texture1"), CurrentToPrevious("ctp"), Near("near"), Far("far"), BlurPasses(
			"blur_passes"), BlurScale("blur_scale"), DepthScale("depth_scale"), InvProj("inv_proj"), Viewport(
			"viewport");
		// @formatter:on

		private final String mnemonic;
		private final int elementSize;

		Param(String m) {
			this.mnemonic = m;
			this.elementSize = 0;
		}

		@Override
		public String mnemonic () {
			return this.mnemonic;
		}

		@Override
		public int arrayElementSize () {
			return this.elementSize;
		}
	}

	public CameraBlur () {
		super(ShaderLoader.fromFile("screenspace", "camerablur"));
		rebind();
		// dolut = false;
	}

	public void setNormalDepthMap (Texture texture) {
		this.normaldepth = texture;
	}

	public void setCurrentToPrevious (Matrix4 ctp) {
		setParams(Param.CurrentToPrevious, ctp);
		endParams();
	}

	public void setInverseProj (Matrix4 invProj) {
		setParams(Param.InvProj, invProj);
		endParams();
	}

	public void setBlurPasses (int passes) {
		setParams(Param.BlurPasses, passes);
		endParams();
	}

	public void setBlurScale (float blurScale) {
		setParams(Param.BlurScale, blurScale);
		endParams();
	}

	public void setNearFarPlanes (float near, float far) {
		setParams(Param.Near, near);
		setParams(Param.Far, far);
		endParams();
	}

	public void setViewport (float width, float height) {
		viewport.set(width, height);
		setParams(Param.Viewport, viewport);
	}

	public void setDepthScale (float scale) {
		setParams(Param.DepthScale, scale);
		endParams();
	}

	@Override
	public void rebind () {
		setParams(Param.InputScene, u_texture0);
		setParams(Param.DepthMap, u_texture1);
		endParams();
	}

	@Override
	protected void onBeforeRender () {
		rebind();
		inputTexture.bind(u_texture0);
		normaldepth.bind(u_texture1);
	}
}
