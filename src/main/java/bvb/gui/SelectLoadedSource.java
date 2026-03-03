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
package bvb.gui;

import java.util.List;

import javax.swing.Timer;

import bdv.viewer.SourceAndConverter;
import bvb.core.BigVolumeBrowser;
import bvb.gui.data.DataTreeNode;
import bvb.utils.Misc;
import bvvpg.vistools.BvvStackSource;

/** workaround selection of sources in BVB **/
public class SelectLoadedSource
{
	final BigVolumeBrowser bvb;
	final DataTreeNode dataNode;
	private final Timer timer;
	final List<SourceAndConverter<?>> sourcesToSelect;
	private long startTime;
	private final int nSources;
	
	public SelectLoadedSource(final BigVolumeBrowser bvb, final DataTreeNode dataNode, List< BvvStackSource< ? > > bvvStackList)
	{
		this.bvb = bvb;
		this.dataNode = dataNode;
		timer = new Timer(1000 / 10, e -> tick());
		sourcesToSelect = Misc.bvvSourcesToSaCList (bvvStackList);
		nSources = sourcesToSelect.size();
	}
	public void start() 
	{
		if(nSources > 0)
		{
			startTime = System.nanoTime();
			timer.start();
		}
	}
	
	private void tick() 
	{
		float elapsedTime =
    			(System.nanoTime() - startTime) / 1_000_000_000f;   
		if(elapsedTime > 10)
		{
			timer.stop();
			return;
		}
		//select node
		bvb.bvbCards.panelData.selectDataNode( dataNode );
		
		//let's check if sources where selected
		List< SourceAndConverter< ? > > selectedSac = bvb.selectedObjects.getSelectedSources();
		int nChecked = nSources;
		for( final SourceAndConverter< ? > sac : selectedSac)
		{
			if(sourcesToSelect.contains( sac ))
			{
				nChecked--;
			}
		}
		if(nChecked == 0 )
			timer.stop();
	}
}
