package com.deo.flapd.utils.postprocessing;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;

/** This interface defines the base class for the concrete implementation of post-processor effects. An effect is considered
 * enabled by default.
 * 
 * @author bmanuel */
public abstract class PostProcessorEffect implements Disposable {
	protected boolean enabled = true;

	/** Concrete objects shall be responsible to recreate or rebind its own resources whenever its needed, usually when the OpenGL
	 * context is lost. Eg., framebuffer textures should be updated and shader parameters should be reuploaded/rebound. */
	public abstract void rebind ();

	/** Concrete objects shall implements its own rendering, given the source and destination buffers. */
	public abstract void render (final FrameBuffer src, final FrameBuffer dest);

	/** Whether or not this effect is enabled and should be processed */
	public boolean isEnabled () {
		return enabled;
	}

	/** Sets this effect enabled or not */
	public void setEnabled (boolean enabled) {
		this.enabled = enabled;
	}

	/** Convenience method to forward the call to the PostProcessor object while still being a non-publicly accessible method */
	protected void restoreViewport (FrameBuffer dest) {
		PostProcessor.restoreViewport(dest);
	}
}
