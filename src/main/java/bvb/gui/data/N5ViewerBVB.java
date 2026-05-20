package bvb.gui.data;

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

	public static void addN5ViewerSelectionToBVB(final BigVolumeBrowser bvb, final DataSelection dataSelection)
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
	
	public static <T extends NumericType<T> & NativeType<T>> void addN5MetadataToBVB(final BigVolumeBrowser bvb, final N5Reader n5, final N5Metadata metadata)
	{
		final DataSelection selection = new DataSelection(n5, Collections.singletonList(metadata));
		final SharedQueue sharedQueue = new SharedQueue(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
		final List<ConverterSetup> converterSetups = new ArrayList<>();
		final List<SourceAndConverter<T>> sourcesAndConverters = new ArrayList<>();
		
		final BdvOptions opts = BdvOptions.options();
		try {
			N5Viewer.buildN5Sources(
					n5,
					selection,
					sharedQueue,
					converterSetups,
					sourcesAndConverters,
					opts);

		} catch (final IOException e1) {
			e1.printStackTrace();
			return;
		}
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
