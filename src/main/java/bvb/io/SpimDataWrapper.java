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

import javax.swing.ImageIcon;

import net.imglib2.util.ValuePair;

import bvb.core.BigVolumeBrowser;
import bvb.gui.data.BVBSpimDataInfo;
import bvb.gui.data.DataTreeModel;
import bvb.utils.Misc;
import ij.ImagePlus;
import mpicbg.spim.data.generic.AbstractSpimData;

public class SpimDataWrapper
{
	final DataTreeModel dataTreeModel; 
	
	public SpimDataWrapper (final BigVolumeBrowser bvb)
	{
		dataTreeModel = bvb.dataTreeModel;
	}
	
	public ValuePair<AbstractSpimData<?>, BVBSpimDataInfo> createSpimDataImagePlus(final ImagePlus imp)
	{
		final AbstractSpimData<?> spimData = ImagePlusToSpimDataBvv.getSpimData( imp );
		final BVBSpimDataInfo info = new BVBSpimDataInfo(imp.getTitle(), dataTreeModel.getIconFIJI());
		return new ValuePair<>(spimData, info);
	}
	
	/** nType 0 - BDV, nType 1 - BioFormats/TIF **/
	public ValuePair<AbstractSpimData<?>, BVBSpimDataInfo> createSpimDataBDVorBF(String sFilename, final int nType)
	{
		AbstractSpimData<?> spimData;
		final ImageIcon spimDataIcon;
		
		if( nType == 0 )
		{
			spimData = SpimDataLoader.loadHDF5( sFilename );
			spimDataIcon = dataTreeModel.getIconBDV();
		}
		else
		{
			spimData = SpimDataLoader.loadBioFormats( sFilename );
			spimDataIcon = dataTreeModel.getIconBioformats();
		}

		final BVBSpimDataInfo info = new BVBSpimDataInfo(Misc.getSourceStyleName(sFilename),spimDataIcon);
		return new ValuePair<>(spimData, info);
	}
	
}
