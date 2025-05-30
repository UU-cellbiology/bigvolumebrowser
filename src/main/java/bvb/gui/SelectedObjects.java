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
package bvb.gui;

import java.util.ArrayList;
import java.util.List;

import bdv.tools.brightness.ConverterSetup;
import bvb.core.BigVolumeBrowser;
import bvb.shapes.BasicShape;
import bvvpg.pgcards.sourcetable.SourceSelectionState;
import bvvpg.pgcards.sourcetable.SourceSelectionWindowState;

/** Class that keeps track on sources currently selected in BVV (Cards panel)
 *  and shapes in Shapes Cards panel. **/

public class SelectedObjects implements SourceSelectionWindowState.Listener, 
										SourceSelectionState.Listener,
										ShapeSelectionState.Listener
{
	
	private List< ConverterSetup > csList = new ArrayList<>();
	private List< BasicShape > shList = new ArrayList<>();
	
	private ArrayList<Listener> listeners =	new ArrayList<>();

	
	public static interface Listener 
	{
		public void selectedObjectsChanged();
	}
	
	public SelectedObjects(final BigVolumeBrowser bvb)
	{
		bvb.bvvViewer.sourceSelection.addSourceSelectionStateListener( this );
		bvb.bvvViewer.sourceGroupSelection.addSourceSelectionStateListener( this );
		bvb.bvvViewer.sourceSelectionWindowState.addSourceSelectionWindowStateListener( this );	
		bvb.shapeSelection.addShapesSelectionStateListener( this );
	}

	@Override
	public void selectionWindowChanged( int nWindow, List< ConverterSetup > csList_ )
	{
		this.csList = csList_;
		for(Listener l : listeners)
				l.selectedObjectsChanged();
	}
	
	@Override
	public void selectionCSChanged( List< ConverterSetup > csList_ )
	{
		this.csList = csList_;
		for(Listener l : listeners)
				l.selectedObjectsChanged();		
	}
	
	@Override
	public void selectionShapesChanged( List< BasicShape > shList_ )
	{
		this.shList = shList_;
		for(Listener l : listeners)
				l.selectedObjectsChanged();				
	}
	
	public void addSourceSelectionListener(Listener l) 
	{
        listeners.add(l);
    }
	
	public boolean isAnythingSelected()
	{
		if(csList.isEmpty() && shList.isEmpty())
			return false;
		return true;
	}
	
	public boolean areSourcesSelected()
	{
		return !csList.isEmpty();
	}
	
	public boolean areShapesSelected()
	{
		return !shList.isEmpty();
	}
	
	public List< ConverterSetup > getSelectedSources()
	{
		return csList;
	}
	
	public List< BasicShape > getSelectedShapes()
	{
		return shList;
	}


}
