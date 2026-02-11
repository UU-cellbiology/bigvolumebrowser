/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
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

import java.io.File;
import java.io.IOException;

import net.imglib2.RandomAccessibleInterval;

import bvb.core.BVBSettings;

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

	public static AbstractSpimData< ? > loadRAI(RandomAccessibleInterval<?> rai) 
	{				
		return RAIToSpimDataBvv.getSpimData( rai );
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
		if(nSeriesCount == 1)
		{
			nOpenSeries = 0;
		}
		else
		{
			//make a list of all series
			
			String [] sDatasetNames = new String[nSeriesCount];
			int [] nDatasetIDs = new int[nSeriesCount];
			int [] nDatasetType = new int[nSeriesCount];
			
			for(int nS = 0; nS < nSeriesCount; nS++)
			{
				if(seriesZsize[nS] > 1)
				{
					sDatasetNames[nS] = seriesName[nS]+" 3D";
				}
				else
				{
					sDatasetNames[nS] = seriesName[nS]+" 2D";
				}
				nDatasetIDs[nS] = nS;
				nDatasetType[nS] = seriesBitDepth[nS];				
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
		
		SpimData spimData = null;

		if (seriesBitDepth[nOpenSeries] == FormatTools.UINT16 || seriesBitDepth[nOpenSeries] == FormatTools.UINT8 || seriesBitDepth[nOpenSeries] == FormatTools.FLOAT)
		{
			OpenerSettings settings = OpenerSettings.BioFormats()
					.location(imageFileName)
					.unit("MICROMETER")
					.setSerie(nOpenSeries)
					.positionConvention("TOP LEFT")
					.pyramidize( BVBSettings.bPyramidize );
			spimData = (SpimData)OpenersToSpimData.getSpimData(settings);	
		}
		else
		{
			 IJ.error( "Sorry, only 8-, 16- and 32-bit BioFormats images are supported.");
			 return null;
		}
		
		return spimData;
		
	}

}
