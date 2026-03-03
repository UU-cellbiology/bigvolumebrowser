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

import java.util.ArrayList;
import java.util.List;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.SourceToConverterSetupBimap;
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
	
	private final SourceToConverterSetupBimap bimap;
	
	private List< ConverterSetup > csList = new ArrayList<>();
	
	private List< BasicShape > shList = new ArrayList<>();
	
	private final List< Object > objList = new ArrayList<>();

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
		bimap = bvb.bvvViewer.getConverterSetups();
	}

	@Override
	public void selectionWindowChanged( int nWindow, final List< ConverterSetup > csList_ )
	{
		updateCSList(csList_);
		for(Listener l : listeners)
				l.selectedObjectsChanged();
	}
	
	void updateCSList(final List< ConverterSetup > csList_ )
	{
		for(final ConverterSetup cs : csList)
		{
			objList.remove( cs );
		}
		
		this.csList = csList_;
		
		for(final ConverterSetup cs : csList)
		{
			objList.add( cs );
		}	
	}
	
	@Override
	public void selectionCSChanged( final List< ConverterSetup > csList_ )
	{
		updateCSList(csList_);
		for(Listener l : listeners)
				l.selectedObjectsChanged();		
	}
	
	@Override
	public void selectionShapesChanged(final List< BasicShape > shList_ )
	{
		updateShapeList( shList_ );
		for(Listener l : listeners)
				l.selectedObjectsChanged();				
	}
	
	void updateShapeList(final List< BasicShape > shList_ )
	{
		for(final BasicShape sh : shList)
		{
			objList.remove( sh );
		}
		
		this.shList = shList_;
		
		for(final BasicShape sh : shList)
		{
			objList.add( sh );
		}	
	}
	
	public void addObjectSelectionListener(Listener l) 
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
	
	public List< ConverterSetup > getSelectedConverterSetups()
	{
		return csList;
	}
	
	public List<SourceAndConverter<?>> getSelectedSources()
	{
		final ArrayList<SourceAndConverter<?>> sacList = new ArrayList<>();
		for(final ConverterSetup cs : csList)
		{
			sacList.add( bimap.getSource( cs ) );
		}
		return sacList;
	}
	
	public List< Object > getSelectedObjects()
	{
		return objList;
	}
	
	public List< BasicShape > getSelectedShapes()
	{
		return shList;
	}


}
