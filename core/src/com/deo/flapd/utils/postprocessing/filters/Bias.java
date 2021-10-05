package com.deo.flapd.utils.postprocessing.filters;

import com.deo.flapd.utils.postprocessing.ShaderLoader;

/** Bias filter. Adapted for lensflare2 effect.
 * @see <a
 *      href="http://john-chapman-graphics.blogspot.co.uk/2013/02/pseudo-lens-flare.html">http://john-chapman-graphics.blogspot.co.uk/2013/02/pseudo-lens-flare.html</a>
 * @author Toni Sagrista */
public final class Bias extends Filter<Bias> {
	private float bias;

	public enum Param implements Parameter {
		// @formatter:off
		Texture("u_texture0"), Bias("u_bias");
		// @formatter:on

		private final String mnemonic;
		private final int elementSize;

		Param(String mnemonic) {
			this.mnemonic = mnemonic;
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

	public Bias () {
		super(ShaderLoader.fromFile("screenspace", "bias"));
		rebind();
	}

	public void setBias (float bias) {
		this.bias = bias;
		setParam(Param.Bias, this.bias);
	}

	public float getBias () {
		return bias;
	}

	@Override
	protected void onBeforeRender () {
		inputTexture.bind(u_texture0);
	}

	@Override
	public void rebind () {
		setParams(Param.Texture, u_texture0);
		setBias(this.bias);
	}
}
