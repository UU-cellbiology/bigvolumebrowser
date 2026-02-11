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
