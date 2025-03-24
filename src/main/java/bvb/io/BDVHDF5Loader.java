package bvb.io;

import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.generic.AbstractSpimData;

public class BDVHDF5Loader
{
	public static AbstractSpimData< ? > loadHDF5(String xmlFileName) throws SpimDataException
	{
		final SpimData spimData = new XmlIoSpimData().load( xmlFileName );
		
		return spimData;
	}
}
