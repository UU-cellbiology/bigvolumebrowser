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
