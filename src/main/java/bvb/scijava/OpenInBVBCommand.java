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
package bvb.scijava;

import java.lang.invoke.MethodHandles;

import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bvb.io.OmeZarrBVB;
import ome.zarr.fiji.Pyramidal;
import ome.zarr.fiji.PyramidalBdv;

/**
 * Sends the OME-Zarr in the window the user last looked at — an ImageJ image,
 * a BigDataViewer or another BigVolumeBrowser — to a new BigVolumeBrowser,
 * without going reading the contents again.
 */
@Plugin( type = Command.class, menuPath = "Plugins > OME-Zarr > Open Current OME-Zarr Image in BigVolumeBrowser" )
public class OpenInBVBCommand implements Command
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private static final String TITLE = "Open in BigVolumeBrowser";

	@Parameter
	private UIService uiService;

	/** Filled in with the active OME-Zarr window's image by the OME-Zarr plugin's preprocessor. */
	@Parameter( required = false )
	private Pyramidal pyramidal;

	@Override
	public void run()
	{
		if ( pyramidal == null )
		{
			final String message = "The active image is not an OME-Zarr dataset.";
			if ( uiService.isVisible() )
				uiService.showDialog( message, TITLE );
			else
				logger.warn( message );
			return;
		}
		try
		{
			OmeZarrBVB.showInBVB( new PyramidalBdv<>( pyramidal.getContext(), pyramidal.getPyramidContents() ) );
		}
		catch ( final Exception e )
		{
			logger.error( "Could not open the active OME-Zarr in BigVolumeBrowser", e );
			if ( uiService.isVisible() )
				uiService.showDialog( e.getMessage(), TITLE );
		}
	}
}
