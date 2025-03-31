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
