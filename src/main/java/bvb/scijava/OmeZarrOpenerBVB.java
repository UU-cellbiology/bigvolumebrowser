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

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bvb.io.OmeZarrBVB;
import ome.zarr.fiji.PyramidalBdv;
import ome.zarr.fiji.open.OmeZarrOpener;
import ome.zarr.fiji.read.OmeZarr;

/**
 * Registers BigVolumeBrowser as an opening option of the OME-Zarr Fiji plugin,
 * so that dragging an OME-Zarr onto Fiji (or pasting its URL) can send it here.
 */
@Plugin( type = OmeZarrOpener.class, name = OmeZarrOpenerBVB.NAME,
		label = "BigVolumeBrowser",
		description = "Open as a multi-resolution volume in BigVolumeBrowser",
		iconPath = "/bvb/icons/bvb-logo.png", priority = Priority.NORMAL )
public class OmeZarrOpenerBVB implements OmeZarrOpener
{
	/** The stable identifier this opener is persisted under. */
	public static final String NAME = "bvb-volume";

	@Override
	public void open( final OmeZarr omeZarr )
	{
		try
		{
			OmeZarrBVB.showInBVB( new PyramidalBdv<>( omeZarr.context(), omeZarr.readContents() ) );
		}
		catch ( final Exception e )
		{
			omeZarr.errorHandler().accept( "Could not open in BigVolumeBrowser: " + e.getMessage() );
		}
	}
}
