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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import org.janelia.saalfeldlab.n5.N5Reader;
import org.janelia.saalfeldlab.n5.bdv.N5Viewer;
import org.janelia.saalfeldlab.n5.metadata.N5ViewerMultichannelMetadata;
import org.janelia.saalfeldlab.n5.ui.DataSelection;
import org.janelia.saalfeldlab.n5.universe.metadata.MultiscaleMetadata;
import org.janelia.saalfeldlab.n5.universe.metadata.N5Metadata;
import org.janelia.saalfeldlab.n5.universe.metadata.canonical.CanonicalMultichannelMetadata;

import bdv.cache.SharedQueue;
import bdv.tools.brightness.ConverterSetup;
import bdv.util.BdvOptions;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bvb.core.BigVolumeBrowser;

public class N5ViewerBVB
{

	public static void addN5ViewerSelectionToBVB(final BigVolumeBrowser bvb, final DataSelection dataSelection) throws IOException
	{
		final List<N5Metadata> selected = new ArrayList<>();
		for (final N5Metadata meta : dataSelection.metadata) {
			if (meta instanceof N5ViewerMultichannelMetadata) {
				final N5ViewerMultichannelMetadata mc = (N5ViewerMultichannelMetadata)meta;
				for (final MultiscaleMetadata<?> m : mc.getChildrenMetadata())
					selected.add(m);
			} else if (meta instanceof CanonicalMultichannelMetadata) {
				final CanonicalMultichannelMetadata mc = (CanonicalMultichannelMetadata)meta;
				for (final N5Metadata m : mc.getChildrenMetadata())
					selected.add(m);
			} else
				selected.add(meta);
		}

		final N5Reader n5 = dataSelection.n5;
		for(final N5Metadata mdata:selected)
		{
			addN5MetadataToBVB(bvb, n5, mdata);
		}
	}
	
	public static <T extends NumericType<T> & NativeType<T>> void addN5MetadataToBVB(final BigVolumeBrowser bvb, final N5Reader n5, final N5Metadata metadata) throws IOException
	{
		final DataSelection selection = new DataSelection(n5, Collections.singletonList(metadata));
		final SharedQueue sharedQueue = new SharedQueue(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
		final List<ConverterSetup> converterSetups = new ArrayList<>();
		final List<SourceAndConverter<T>> sourcesAndConverters = new ArrayList<>();
		
		final BdvOptions opts = BdvOptions.options();
			N5Viewer.buildN5Sources(
					n5,
					selection,
					sharedQueue,
					converterSetups,
					sourcesAndConverters,
					opts);

		String rootPath = metadata.getPath();
		if(!rootPath.equals( "" ))
		{
			rootPath = "(" + rootPath + ")";
		}
		final URI uri = n5.getURI();
		final String sBaseName =  rootPath + getSourceStyleURI(uri);
		final List<Source<?>> srcs = new ArrayList<>();
		List<String> srcNames = new ArrayList<>();
		int nCh = 1;
		for(final SourceAndConverter<T> sac : sourcesAndConverters)
		{
			srcs.add( sac.getSpimSource() );
			srcNames.add( "channel " + Integer.toString( nCh ) );
			nCh++;
			//Converter< T, ARGBType > dsd = sac.getConverter();
		}
		bvb.addSourcesList( srcs, sBaseName, srcNames, bvb.dataTreeModel.getIconZarr() );		
	}
	
	public static String getSourceStyleURI(URI uri) {
        
		
		if (uri == null) return null;

		String sFinalPart = "datasetname";
		String sSuffix = "full address prefix";
		
	
		if(	uri.toString() != null)
		{
			sSuffix = uri.toString();
		}	
		
        String path = uri.getPath();
        
        if (path == null || path.isEmpty()) 
        {
        	if(uri.getHost() != null)
        	{
        		sSuffix = uri.getHost();
        	}
        }
        else
        {
        	if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        	if (path.endsWith("\\")) {
                path = path.substring(0, path.length() - 2);
            }
        	
        	int lastForwardSlash = path.lastIndexOf('/');
            int lastBackwardSlash = path.lastIndexOf('\\');
            int lastSlashIndex = Math.max(lastForwardSlash, lastBackwardSlash);
            if (lastSlashIndex >= 0) 
            {
            	sFinalPart = path.substring(lastSlashIndex + 1);
            }
        }
        return sFinalPart + "(" + sSuffix + ")"; 
    }
}
