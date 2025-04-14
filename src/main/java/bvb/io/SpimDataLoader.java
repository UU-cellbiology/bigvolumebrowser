package bvb.io;


import java.io.File;
import java.io.IOException;

import ch.epfl.biop.bdv.img.OpenersToSpimData;
import ch.epfl.biop.bdv.img.opener.OpenerSettings;
import ij.IJ;
import ij.gui.GenericDialog;
import loci.common.DebugTools;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.generic.AbstractSpimData;

public class SpimDataLoader
{
	public static AbstractSpimData< ? > loadHDF5(String xmlFileName) 
	{
		SpimData spimData;
		try
		{
			spimData = new XmlIoSpimData().load( xmlFileName );
		}
		catch ( SpimDataException exc )
		{
			exc.printStackTrace();
			spimData = null;
		}
		
		return spimData;
	}
	
	public static AbstractSpimData< ? > loadBioFormats(String imageFileName) 
	{
		DebugTools.setRootLevel("INFO");
		
		//analyze file a bit
		int nSeriesCount = 0;
	    
		String[] seriesName = null;
		
	    int[] seriesZsize = null;
	    int[] seriesBitDepth = null;
	    
	    // check if multiple files inside, like LIF
	    try (ImageProcessorReader r = new ImageProcessorReader(
	    		new ChannelSeparator(LociPrefs.makeImageReader()));)
	    {
	    	ServiceFactory factory = new ServiceFactory();
	    	OMEXMLService service = factory.getInstance(OMEXMLService.class);

	    	r.setMetadataStore(service.createOMEXMLMetadata());      
	    	r.setId(imageFileName);

	    	nSeriesCount = r.getSeriesCount();
	    	seriesName = new String[nSeriesCount];
	    	seriesZsize = new int[nSeriesCount];
	    	seriesBitDepth = new int[nSeriesCount];

	    	MetadataRetrieve retrieve = (MetadataRetrieve) r.getMetadataStore();
	    	for (int nS=0;nS<nSeriesCount;nS++)
	    	{
	    		r.setSeries(nS);
	    		seriesZsize[nS] = r.getSizeZ();
	    		seriesName[nS] = retrieve.getImageName(nS);
	    		seriesBitDepth[nS] = r.getPixelType();
	    	}

	    }
	    catch (FormatException exc) {
	    	System.err.println("Sorry, an error occurred: " + exc.getMessage());
	    	return null;

	    }
	    catch (IOException exc) {
	    	System.err.println( "Sorry, an error occurred: " + exc.getMessage());
	    	return null;
	    }
	    catch (DependencyException de) { 
	    	System.err.println( "Sorry, an error occurred: " + de.getMessage());
	    	return null;
	    }
	    catch (ServiceException se) { 
	    	System.err.println( "Sorry, an error occurred: " + se.getMessage());
	    	return null;
	    }
	    
		int nOpenSeries = 0;
		if(nSeriesCount==1)
		{
			if(seriesZsize[0]>1)
			{
				nOpenSeries = 0;
			}
			else
			{
				 IJ.error("Only 3D datasets are supported.");
				 return null;
			}
		}
		else
		{
			//make a list of 3D series
			int outCount = 0;
			for(int nS=0;nS<nSeriesCount; nS++)
			{
				if(seriesZsize[nS] > 1)
				{
					outCount++;
				}
			}
			if(outCount == 0)
			{
				IJ.error( "Cannot find 3D datasets in provided file\n" + imageFileName);
				return null;
			}
			
			String [] sDatasetNames = new String[outCount];
			int [] nDatasetIDs = new int[outCount];
			int [] nDatasetType = new int[outCount];
			int nCurrDS = 0;
			for(int nS=0;nS<nSeriesCount;nS++)
			{
				if(seriesZsize[nS] > 1)
				{
					sDatasetNames[nCurrDS] = seriesName[nS];
					nDatasetIDs[nCurrDS] = nS;
					nDatasetType[nCurrDS] = seriesBitDepth[nS];
					nCurrDS++;
				}
			}
			GenericDialog openDatasetN = new GenericDialog("Choose dataset..");
			openDatasetN.addChoice("Name: ",sDatasetNames, sDatasetNames[0]);
			openDatasetN.showDialog();
			if (openDatasetN.wasCanceled())
			{
	            System.out.println("Dataset opening was cancelled.");
	            return null;
			}
			
			nOpenSeries = nDatasetIDs[openDatasetN.getNextChoiceIndex()];
			
		}
		

		if (seriesBitDepth[nOpenSeries] == FormatTools.UINT16)
		{
			OpenerSettings settings = OpenerSettings.BioFormats()
					.location(new File(imageFileName))
					.unit("MICROMETER")
					.setSerie(nOpenSeries)
					.positionConvention("TOP LEFT");
			return OpenersToSpimData.getSpimData(settings);
			
		}
		else
		if(seriesBitDepth[nOpenSeries] == FormatTools.UINT8 )		
		{
			OpenerSettings settings = OpenerSettings.BioFormats()
					.location(new File(imageFileName))
					.unit("MICROMETER")
					.setSerie(nOpenSeries)
					.to16bits(true)
					.positionConvention("TOP LEFT");
			return OpenersToSpimData.getSpimData(settings);
		}
		else
		{
			 IJ.error( "Sorry, only 8- and 16-bit BioFormats images are supported.");
			 return null;
		}

//		final SequenceDescription seq = bt.spimData.getSequenceDescription();

		
//		//see if data comes from LLS7
//		String sTestLLS = seq.getViewDescription(0, 0).getViewSetup().getName();
//		if(sTestLLS.length()>3)
//		{
//			if(sTestLLS.contains("LLS") && btdata.sFileNameFullImg.endsWith(".czi"))
//			{
//				if (JOptionPane.showConfirmDialog(null, "Looks like the input comes from Zeiss LLS7.\nDo you want to deskew it?\n"
//						+ "(if it is already deskewed, click No)", "Loading option",
//				        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
//				    // yes option
//					bt.bApplyLLSTransform = true;
//				} 
//			}
//
//		}
		
	}
}
