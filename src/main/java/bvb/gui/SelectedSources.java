package bvb.gui;

import java.util.ArrayList;
import java.util.List;

import bdv.tools.brightness.ConverterSetup;
import bvvpg.core.VolumeViewerPanel;
import bvvpg.pgcards.sourcetable.SourceSelectionState;
import bvvpg.pgcards.sourcetable.SourceSelectionWindowState;


public class SelectedSources implements SourceSelectionWindowState.Listener, SourceSelectionState.Listener
{
	/** -1 - none focused, 0 - source window, 1 - group window**/
	private int nActiveWindow = -1;
	
	private ArrayList<Listener> listeners =	new ArrayList<>();
	
	public static interface Listener 
	{
		public void selectedSourcesChanged(int nWindow, List< ConverterSetup > csList);

	}
	
	public SelectedSources(final VolumeViewerPanel viewer)
	{
		viewer.sourceSelection.addSourceSelectionStateListener( this );
		viewer.sourceGroupSelection.addSourceSelectionStateListener( this );
		viewer.sourceSelectionWindowState.addSourceSelectionWindowStateListener( this );
				
	}

	@Override
	public void selectionWindowChanged( int nWindow, List< ConverterSetup > csList )
	{
		nActiveWindow = nWindow;
		for(Listener l : listeners)
				l.selectedSourcesChanged(nActiveWindow,csList );

	}
	@Override
	public void selectionCSChanged( List< ConverterSetup > csList )
	{
		for(Listener l : listeners)
				l.selectedSourcesChanged(nActiveWindow,csList );

		
	}
	public void addSourceSelectionListener(Listener l) 
	{
        listeners.add(l);
    }
}
