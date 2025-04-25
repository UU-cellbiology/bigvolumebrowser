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
import bvvpg.core.VolumeViewerPanel;
import bvvpg.pgcards.sourcetable.SourceSelectionState;
import bvvpg.pgcards.sourcetable.SourceSelectionWindowState;

/** Class that keeps track on sources currently selected in BVV (Cards panel).
 *  Fires an event on selection change or focus change
 *  between the Sources and Groups panels switch. **/

public class SelectedSources implements SourceSelectionWindowState.Listener, SourceSelectionState.Listener
{
	/** -1 - none focused, 0 - source window, 1 - group window**/
	private int nActiveWindow = -1;
	
	private List< ConverterSetup > csList = new ArrayList<>();
	
	private ArrayList<Listener> listeners =	new ArrayList<>();

	
	public static interface Listener 
	{
		public void selectedSourcesChanged();
	}
	
	public SelectedSources(final VolumeViewerPanel viewer)
	{
		viewer.sourceSelection.addSourceSelectionStateListener( this );
		viewer.sourceGroupSelection.addSourceSelectionStateListener( this );
		viewer.sourceSelectionWindowState.addSourceSelectionWindowStateListener( this );				
	}

	@Override
	public void selectionWindowChanged( int nWindow, List< ConverterSetup > csList_ )
	{
		nActiveWindow = nWindow;
		this.csList = csList_;
		for(Listener l : listeners)
				l.selectedSourcesChanged();
	}
	
	@Override
	public void selectionCSChanged( List< ConverterSetup > csList_ )
	{
		this.csList = csList_;
		for(Listener l : listeners)
				l.selectedSourcesChanged();		
	}
	
	public void addSourceSelectionListener(Listener l) 
	{
        listeners.add(l);
    }
	
	public List< ConverterSetup > getSelectedSources()
	{
		return csList;
	}
	
	public int getActiveWindow()
	{
		return nActiveWindow;
	}
}
