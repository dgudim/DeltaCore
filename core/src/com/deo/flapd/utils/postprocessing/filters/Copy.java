package com.deo.flapd.utils.postprocessing.filters;

import com.deo.flapd.utils.postprocessing.ShaderLoader;

public class Copy extends Filter<Copy> {
	public enum Param implements Parameter {
		// @formatter:off
		Texture0(), ;
		// @formatter:on

		private final String mnemonic;
		private final int elementSize;

		Param() {
			this.mnemonic = "u_texture0";
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

	public Copy () {
		super(ShaderLoader.fromFile("screenspace", "copy"));
	}

	@Override
	public void rebind () {
		setParam(Param.Texture0, u_texture0);
	}

	@Override
	protected void onBeforeRender () {
		inputTexture.bind(u_texture0);
	}
}
