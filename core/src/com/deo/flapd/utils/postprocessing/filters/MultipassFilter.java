package com.deo.flapd.utils.postprocessing.filters;

import com.deo.flapd.utils.postprocessing.utils.PingPongBuffer;

/** The base class for any multi-pass filter. Usually a multi-pass filter will make use of one or more single-pass filters,
 * promoting composition over inheritance. */
public abstract class MultipassFilter {
	public abstract void rebind ();

	public abstract void render (PingPongBuffer srcdest);
}
