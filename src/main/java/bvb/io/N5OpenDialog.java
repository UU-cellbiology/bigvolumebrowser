package bvb.io;


import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.janelia.saalfeldlab.n5.bdv.N5ViewerCreator;
import org.janelia.saalfeldlab.n5.bdv.N5ViewerTreeCellRenderer;
import org.janelia.saalfeldlab.n5.ij.N5Importer;
import org.janelia.saalfeldlab.n5.ui.DatasetSelectorDialog;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import ij.Prefs;
import ij.io.OpenDialog;

public class N5OpenDialog extends N5ViewerCreator
{
	private DatasetSelectorDialog dialogBVB;
	
	public void openBVB(final BigVolumeBrowser bvb) 
	{

		final ExecutorService exec = Executors.newFixedThreadPool(ij.Prefs.getThreads());
		OpenDialog.setDefaultDirectory( BVBSettings.lastDir  );
		dialogBVB = new DatasetSelectorDialog(
				new N5Importer.N5ViewerReaderFun(),
				new N5Importer.N5BasePathFun(),
				"",
				n5vGroupParsers,
				n5vParsers);

		dialogBVB.setLoaderExecutor(exec);
		dialogBVB.setTreeRenderer(new N5ViewerTreeCellRenderer(false));
		dialogBVB.setContainerPathUpdateCallback(x -> {});
		try
		{
		dialogBVB.run(selection -> 
		{
			try 
			{
				final URI uri = selection.n5.getURI();
				if(uri.getScheme().equalsIgnoreCase( "file" ))
				{
					//if file system, let's update last folder
					final Path folderPath = Paths.get(uri).getParent();
				    BVBSettings.lastDir = folderPath.toAbsolutePath().toString();
				    Prefs.set( "BVB.lastDir", BVBSettings.lastDir );
				}
				N5ViewerBVB.addN5ViewerSelectionToBVB( bvb, selection );
			}
		 catch (final IOException e) {
			 System.out.println("Got you!");
			System.out.println(e.toString());
		}
		});
		}
		catch(final Exception e)
		{
			 System.out.println("Got you again!");
			System.out.println(e.toString());
			
		}
		//System.out.println( "done");
	}
}
