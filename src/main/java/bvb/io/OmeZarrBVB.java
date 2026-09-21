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
import ome.zarr.imglib2.metadata.Omero;

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
		showInBVB(bvb, pyramidal);
		return bvb;
	}
	
	public static void showInBVB(final BigVolumeBrowser bvb, final PyramidalBdv< ? > pyramidal )
	{
		addSources( bvb, pyramidal );
		registerWindow( bvb, pyramidal );
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
		applyChannelProperties( bvb, pyramidal.getPyramidContents().omero, added.getB() );
	}

	/**
	 * Applies the OME-Zarr's OMERO display settings to the BVV sources.
	 * <p>
	 * Color, display range, per-channel visibility and the start timepoint are set
	 * by {@link BdvUtils}, the same code the OME-Zarr plugin uses for
	 * BigDataViewer, so a dataset looks the same in both viewers. That works
	 * because {@code BvvHandle} exposes the same BigDataViewer core types a
	 * {@code BdvHandle} does — {@link bdv.viewer.ConverterSetups} and
	 * {@link bdv.viewer.ViewerState} — even though the two handles are otherwise
	 * unrelated.
	 */
	private static void applyChannelProperties( final BigVolumeBrowser bvb, final Omero omero,
			final List< BvvStackSource< ? > > bvvSources )
	{
		final List< SourceAndConverter< ? > > viewerSources = new ArrayList<>();
		for ( final BvvStackSource< ? > bvvSource : bvvSources )
			viewerSources.addAll( bvvSource.getSources() );
		final List< Omero.Channel > omeroChannels = BdvUtils.omeroChannels( omero, viewerSources.size() );
		BdvUtils.setTimepoint( omero, bvb.bvvViewer.state() );
		BdvUtils.setChannelProperties( omeroChannels, viewerSources, bvb.bvvHandle.getConverterSetups(), bvb.bvvViewer.state() );
		setBrightnessSliders( omeroChannels, bvvSources );
	}

	/**
	 * Moves BVB's brightness sliders onto the already applied display range and
	 * takes their travel from the OMERO window's min and max.
	 * <p>
	 * BVV keeps its own brightness state in {@code MinMaxGroup}s that only
	 * {@link BvvStackSource} writes. Without this the sliders stay at BVB's
	 * defaults. {@code MinMaxGroup.setRange} pulls the range inside the bounds, so
	 * both checks matter: a missing min and max read as 0 and 0, and too-narrow
	 * bounds would undo the range set just before.
	 */
	private static void setBrightnessSliders( final List< Omero.Channel > omeroChannels,
			final List< BvvStackSource< ? > > bvvSources )
	{
		if ( omeroChannels.isEmpty() )
			return;
		for ( int channelNumber = 0; channelNumber < bvvSources.size(); channelNumber++ )
		{
			final Omero.Channel omeroChannel = omeroChannels.get( channelNumber );
			if ( omeroChannel == null || omeroChannel.window == null )
				continue;
			final Omero.Channel.Window window = omeroChannel.window;
			final BvvStackSource< ? > bvvSource = bvvSources.get( channelNumber );
			bvvSource.setDisplayRange( window.start, window.end );
			if ( window.max > window.min )
				bvvSource.setDisplayRangeBounds( Math.min( window.min, window.start ), Math.max( window.max, window.end ) );
		}
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
