package bvb.core;

import java.awt.Dimension;
import java.awt.Insets;

import bvb.gui.SelectedObjects;
import bvb.gui.SourcesRenderPanel;
import bvb.gui.ViewPanel;
import bvb.gui.clip.ClipPanel;
import bvb.gui.data.PanelAddSources;
import bvb.gui.data.PanelData;
import bvb.gui.shapes.PanelAddShapes;
import bvb.gui.shapes.PanelShapes;
import bvb.gui.shapes.ShapesPropertiesPanel;
import bvb.gui.transform.TransformPanel;
import bvvpg.pgcards.BVVPGDefaultCards;

public class BVBCards
{
	
	final BigVolumeBrowser bvb;
	
	final public PanelAddSources panelAddSources;
	
	final public ShapesPropertiesPanel panelShapesProperties;
	
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
		
		setupListeners();
		
		panelShapesProperties = new ShapesPropertiesPanel(bvb);
		
	    viewPanel = new ViewPanel(bvb);
	    
	    sourcesRenderPanel = new SourcesRenderPanel(bvb.bvv.getBvvHandle().getConverterSetups(), bvb.selectedObjects);
	    
	    clipPanel = new ClipPanel(bvb);		
	    
	    transformPanel = new TransformPanel(bvb);
	}
	
	public void setupListeners()
	{
		//setup global listeners
		bvb.selectedObjects = new SelectedObjects(bvb);
		
		panelData.addObjectSelectionListener();
		
	}
	public void installCards()
	{
		final Dimension tableViewPrefSize = new Dimension( 340, 285 );
		//bvvFrame.getSplitPanel().setCollapsed( false );
	    bvb.bvvFrame.getCardPanel().removeCard( BVVPGDefaultCards.DEFAULT_VIEWERMODES_CARD );
	    bvb.bvvFrame.getCardPanel().removeCard( BVVPGDefaultCards.DEFAULT_SOURCEGROUPS_CARD );
	    bvb.bvvFrame.getCardPanel().setCardExpanded( BVVPGDefaultCards.DEFAULT_SOURCES_CARD, false );

	    bvb.bvbCards.panelShapes.setPreferredSize( tableViewPrefSize );
	    bvb.bvbCards.panelData.setPreferredSize( tableViewPrefSize );
	    bvb.bvvFrame.getCardPanel().addCard("Shapes", panelShapes, false, new Insets( 0, 0, 0, 0 ) );
	    bvb.bvvFrame.getCardPanel().addCard("All objects", panelData, true, new Insets( 0, 0, 0, 0 ) );
	    bvb.bvvFrame.getCardPanel().addCard("Sources render", sourcesRenderPanel, false, new Insets( 0, 0, 0, 0 ) );
	    bvb.bvvFrame.getCardPanel().addCard("Shapes render", panelShapesProperties, false, new Insets( 0, 0, 0, 0 ) );	     
	    bvb.bvvFrame.getCardPanel().addCard("View", viewPanel, false, new Insets( 0, 0, 0, 0 ) );
	    bvb.bvvFrame.getCardPanel().addCard("Clipping", clipPanel, false, new Insets( 0, 0, 0, 0 ) );
	    bvb.bvvFrame.getCardPanel().addCard("Transform", transformPanel, false, new Insets( 0, 0, 0, 0 ) );		   
	    bvb.bvvFrame.getCardPanel().addCard("Add volumes", panelAddSources, true, new Insets( 0, 0, 0, 0 ) );		   
	    bvb.bvvFrame.getCardPanel().addCard("Add shapes", panelAddShapes, true, new Insets( 0, 0, 0, 0 ) );		   		    
	   
	    bvb.bvvFrame.getSplitPanel().setCollapsed( false );
	    bvb.bvvFrame.getSplitPanel().set
	    bvb.bvvHandle.getConverterSetups().listeners().add( s -> bvb.clipBoxes.updateClipBoxes() );
	}
	
	public void resetClipTransformPanels()
	{
		//probably it is possible just 
		// to reset clip/transform setups, but for now we redo everything.
		clipPanel = new ClipPanel(bvb);
		transformPanel = new TransformPanel(bvb);
	}
}
