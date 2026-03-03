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
