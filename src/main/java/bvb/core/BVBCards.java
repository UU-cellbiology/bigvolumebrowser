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
package bvb.core;

import java.awt.Dimension;
import java.awt.Insets;

import bvb.animation.AnimationPanel;
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
	
	final public AnimationPanel animationPanel;
	
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
	    
	    animationPanel =  new AnimationPanel(bvb);
	    
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
	    final Insets zeroInsets = new Insets( 0, 0, 0, 0 );
	    bvb.bvvFrame.getCardPanel().addCard("Shapes", panelShapes, false, zeroInsets );
	    bvb.bvvFrame.getCardPanel().addCard("All objects", panelData, true, zeroInsets );
	    bvb.bvvFrame.getCardPanel().addCard("Sources render", sourcesRenderPanel, false, zeroInsets );
	    bvb.bvvFrame.getCardPanel().addCard("Shapes render", panelShapesProperties, false, zeroInsets );	     
	    bvb.bvvFrame.getCardPanel().addCard("View", viewPanel, false, zeroInsets );
	    bvb.bvvFrame.getCardPanel().addCard("Clipping", clipPanel, false, zeroInsets );
	    bvb.bvvFrame.getCardPanel().addCard("Transform", transformPanel, false, zeroInsets );	
	    bvb.bvvFrame.getCardPanel().addCard("Animation", animationPanel, false, zeroInsets );
	    bvb.bvvFrame.getCardPanel().addCard("Add volumes", panelAddSources, true, zeroInsets );		   
	    bvb.bvvFrame.getCardPanel().addCard("Add shapes", panelAddShapes, true, zeroInsets );		   		    
	   
	    bvb.bvvFrame.getSplitPanel().setCollapsed( false );
	    bvb.bvvHandle.getConverterSetups().listeners().add( s -> bvb.clipBoxes.updateClipBoxes() );
	}
	
	public void resetClipTransformPanels()
	{
		// probably it is possible just 
		// to reset clip/transform setups, but for now we redo everything.
		clipPanel = new ClipPanel(bvb);
		transformPanel = new TransformPanel(bvb);
	}
}
