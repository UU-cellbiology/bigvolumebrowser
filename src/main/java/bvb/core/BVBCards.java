package bvb.core;

import bvb.gui.SelectedObjects;
import bvb.gui.SourcesRenderPanel;
import bvb.gui.ViewPanel;
import bvb.gui.clip.ClipPanel;
import bvb.gui.data.PanelAddSources;
import bvb.gui.data.PanelData;
import bvb.gui.shapes.PanelAddShapes;
import bvb.gui.shapes.PanelShapes;
import bvb.gui.transform.TransformPanel;

public class BVBCards
{
	
	final BigVolumeBrowser bvb;
	
	final public PanelAddSources panelAddSources;
	
	final public PanelData panelData;	
	
	final public PanelAddShapes panelAddShapes;
	
	final public PanelShapes panelShapes;	
	
	final public ViewPanel viewPanel;
	
	final public SourcesRenderPanel sourcesRenderPanel;
	
	public ClipPanel clipPanel;
	
	public TransformPanel transformPanel;
	
	public BVBCards(final BigVolumeBrowser bvb_) 
	{
		bvb = bvb_;   
	    
		panelAddSources = new PanelAddSources(bvb);

		panelData = new PanelData(bvb);
		
		panelAddShapes = new PanelAddShapes(bvb);
		
		panelShapes = new PanelShapes(bvb);
		
		//setup global listeners
		bvb.selectedObjects = new SelectedObjects(bvb);
		
		panelData.addSourceStateListener();
		
		
	    viewPanel = new ViewPanel(bvb);
	    
	    sourcesRenderPanel = new SourcesRenderPanel(bvb.bvv.getBvvHandle().getConverterSetups(), bvb.selectedObjects);
	    
	    clipPanel = new ClipPanel(bvb);		
	    
	    transformPanel = new TransformPanel(bvb);
	}
	
	public void resetClipPanel()
	{
		//this.remove( clipPanel );
		clipPanel = new ClipPanel(bvb);
		clipPanel.setSourceListeners();
	}
}
