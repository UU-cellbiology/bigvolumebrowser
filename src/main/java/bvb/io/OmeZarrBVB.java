/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bvb.io;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.util.ValuePair;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import mpicbg.spim.data.generic.AbstractSpimData;
import bvb.core.BigVolumeBrowser;
import bvvpg.vistools.BvvStackSource;
import ome.zarr.fiji.PyramidalBdv;
import ome.zarr.fiji.plugins.PyramidalService;
import ome.zarr.fiji.util.BdvUtils;

/**
 * Shows an OME-Zarr resolution pyramid, read by the OME-Zarr Fiji plugin, in a
 * BigVolumeBrowser window.
 */
public class OmeZarrBVB
{
	private OmeZarrBVB()
	{
		// static utility class
	}

	/**
	 * Opens {@code pyramidal} in a new BigVolumeBrowser window and returns the handle to that {@link BigVolumeBrowser}.
	 */
	public static BigVolumeBrowser showInBVB( final PyramidalBdv< ? > pyramidal )
	{
		final BigVolumeBrowser bvb = new BigVolumeBrowser();
		bvb.startBVB( pyramidal.getName() );
		addSources( bvb, pyramidal );
		registerWindow( bvb, pyramidal );
		return bvb;
	}

	/**
	 * Adds every channel of the {@code pyramidal} as one BVV source, grouped under the
	 * dataset name, and applies the OME-Zarr display settings. Source names are
	 * left to the sources themselves, which already carry the OME-Zarr channel
	 * labels.
	 */
	static void addSources( final BigVolumeBrowser bvb, final PyramidalBdv< ? > pyramidal )
	{
		final List< Source< ? > > spimSources = new ArrayList<>();
		for ( final SourceAndConverter< ? > sourceAndConverters : pyramidal.asSources() )
			spimSources.add( sourceAndConverters.getSpimSource() );
		final ValuePair< AbstractSpimData< ? >, List< BvvStackSource< ? > > > added =
				bvb.addSourcesList( spimSources, pyramidal.getName(), null, bvb.dataTreeModel.getIconZarr() );
		// null means BVB turned the sources down, e.g. because of their pixel type;
		// it has logged why, but the caller still has to hear that nothing was shown.
		if ( added == null )
			throw new IllegalStateException( "BigVolumeBrowser cannot display this dataset, see the log for the reason." );
	}

	/**
	 * Makes the BVV frame known to {@link PyramidalService}, which tracks the window
	 * last holding an OME-Zarr for the "open the current OME-Zarr in …" commands.
	 * <p>
	 * {@link BigVolumeBrowser#restartBVV()} builds a new frame, so the registration
	 * is renewed on {@code bvbRestarted}. It and the old frame's
	 * {@code windowClosed} may arrive in any order, and the reference count stays
	 * balanced either way.
	 */
	static void registerWindow( final BigVolumeBrowser bvb, final PyramidalBdv< ? > pyramidal )
	{
		final PyramidalService pyramidalService = pyramidal.getContext().getService( PyramidalService.class );
		BdvUtils.registerBdvWindow( pyramidal, bvb.bvvFrame, pyramidalService );
		bvb.addBVBListener( () -> BdvUtils.registerBdvWindow( pyramidal, bvb.bvvFrame, pyramidalService ) ); // NB: a restart replaces the frame, and this call has to register the new one. bvb.bvvFrame needs to be read again.
	}
}
