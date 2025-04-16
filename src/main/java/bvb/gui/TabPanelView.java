package bvb.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import bvb.core.BigVolumeBrowser;
import bvb.gui.clip.ClipPanel;

public class TabPanelView extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final public ViewPanel viewPanel;

	final public SourcesRenderPanel sourcesRenderPanel;

	public ClipPanel clipPanel;
	
	public TabPanelView(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		
		bvb = bvb_;
			
	   
	    
	    viewPanel = new ViewPanel(bvb);
	    
	    sourcesRenderPanel = new SourcesRenderPanel(bvb.bvv.getBvvHandle().getConverterSetups(), bvb.selectedSources);
	    
	    clipPanel = new ClipPanel(bvb);		
	   
	    GridBagConstraints gbc = new GridBagConstraints();
	    
	    //add panels to Navigation
	    gbc.insets = new Insets(4,3,4,3);
	    //View
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
	    JPanel both = new JPanel(new GridBagLayout());
	    
	    GridBagConstraints c = new GridBagConstraints();
	    c.insets = new Insets(3,0,3,0);
	    both.add( viewPanel,c);
	    c.gridx++;
	    both.add( sourcesRenderPanel,c );
	    
	    both.setBorder(new PanelTitle(" View "));

	    this.add( both, gbc );

	    gbc.gridy++;
	    this.add(clipPanel,gbc);
	    
        // Blank/filler component
	    gbc.gridy++;
	    gbc.weightx = 0.01;
	    gbc.weighty = 0.01;
	    this.add(new JLabel(), gbc);	
	}
	


	public void resetClipPanel()
	{
		this.remove( clipPanel );
		clipPanel = new ClipPanel(bvb);
		clipPanel.setSourceListeners();
		
	    GridBagConstraints gbc = new GridBagConstraints();
	    
	    //add panels to Navigation
	    gbc.insets = new Insets(4,3,4,3);
	    //View
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add( clipPanel,gbc );
	}
}
	
