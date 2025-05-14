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
package bvb.gui.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.SourceAndConverter;
import bvb.core.BigVolumeBrowser;
import bvb.gui.PanelTitle;
import bvb.gui.SelectedSources;
import bvvpg.vistools.BvvStackSource;


public class PanelData extends JPanel
{
	final BigVolumeBrowser bvb;
	
	final private JTree treeData;
	
	final JScrollPane treeScroller;
	
	boolean bLocked = false;
	
	public PanelData(final BigVolumeBrowser bvb_)
	{
		super(new GridBagLayout());	
		bvb = bvb_;
		this.setBorder(new PanelTitle(" Loaded data "));
		

        //create the tree by passing in the data model
        treeData = new JTree(bvb.dataTreeModel);
        
        treeData.setRootVisible(false);
        
        DataTreeCellRenderer renderer = new DataTreeCellRenderer();

        renderer.setLeafIcon( bvb.dataTreeModel.getIconOneSource() );
        treeData.setCellRenderer(renderer);
        treeData.setShowsRootHandles(true);
        
        treeData.addTreeSelectionListener( new TreeSelectionListener() 
        		{

					@Override
					public void valueChanged( TreeSelectionEvent arg0 )
					{
						
						selectSetups();
						
					}
        		});
        
        MouseListener ml = new MouseAdapter() {
            @Override
			public void mousePressed(MouseEvent e) 
            {
                int selRow = treeData.getRowForLocation(e.getX(), e.getY());
               
                if(selRow != -1) 
                {
                	if(e.getClickCount() == 2) 
                	{
                       bvb.bvbActions.actionCenterView();
                    }
                }
            }
        };
        treeData.addMouseListener( ml );
        
    	treeScroller = new JScrollPane(treeData);
    	treeScroller.setMinimumSize(new Dimension(170, 200));
    	//treeScroller.setPreferredSize(new Dimension(400, 500));
    	
    	GridBagConstraints gbc = new GridBagConstraints();
	    
	    gbc.insets = new Insets(4,3,4,3);

	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 0.99;
	    gbc.weighty = 0.99;

	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.BOTH;
        this.add(treeScroller,gbc);
	}
	
	/** called by parent **/
	public void addSourceStateListener()
	{
    	bvb.selectedSources.addSourceSelectionListener(  new SelectedSources.Listener()
		{
			
			@Override
			public void selectedSourcesChanged()
			{
				updateSourcesSelection();
			}
		} );
	}
	
	synchronized void selectSetups()
	{
		if(bLocked)
			return;
		
		bLocked = true;
		
		TreePath[] selPaths = treeData.getSelectionPaths();
		if(selPaths == null)
		{
			bLocked = false;
			return;
		}
		ArrayList<SourceAndConverter<?>> selectedSAC = new ArrayList<>();
		for(int i=0;i<selPaths.length; i++)
		{

			DataTreeNode node = (DataTreeNode) selPaths[i].getLastPathComponent(  );
			if(node.bvvSource != null)
			{
				selectedSAC.add( getSAC(node.bvvSource) );
			}
			if(node.spimData != null)
			{
				List< DataTreeNode > listBvvSourcesNodes = bvb.dataTreeModel.dataParentChildren.get(node);
				for(DataTreeNode leafnode :listBvvSourcesNodes)
				{
					if(leafnode.bvvSource != null)
					{
						selectedSAC.add( getSAC(leafnode.bvvSource) );
					}
				}
			}

		}
		bvb.bvvViewer.sourceSelection.table.setSelectedSources( selectedSAC );
		bLocked = false;
	}
	
	synchronized void updateSourcesSelection()
	{
		if(bLocked)
			return;
		bLocked = true;
		final List< ConverterSetup > csList = bvb.selectedSources.getSelectedSources();
		
		if(csList == null || csList.size()==0)
		{
			bLocked = false;
			return;
		}
		
		treeData.getSelectionModel().clearSelection();//.setSelectionPath( null );
		
		for (Entry< DataTreeNode, DataTreeNode > entry : bvb.dataTreeModel.dataChildParent.entrySet()) 
		{
			final DataTreeNode node = entry.getKey();
			if(node.isLeaf && node.bvvSource != null)
			{
				for(ConverterSetup cs: csList)
				{
					if(node.bvvSource.getConverterSetups().get( 0 ).equals( cs ))	
					{
						TreePath tp = new TreePath(bvb.dataTreeModel.getRoot());
						tp = tp.pathByAddingChild( node.getParent() );
						tp = tp.pathByAddingChild( node );
						treeData.addSelectionPath( tp );	
					}
				}

			}
				

		}	   
		bLocked = false;
	}
	
	SourceAndConverter<?> getSAC(final BvvStackSource<?> bvvSource)
	{
		final ConverterSetup csSetup = bvvSource.getConverterSetups().get( 0 );
		return bvb.bvvHandle.getConverterSetups().getSource( csSetup );
	}
	
}
