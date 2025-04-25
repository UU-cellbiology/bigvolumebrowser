package bvb.gui.data;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
        
    	treeScroller = new JScrollPane(treeData);
    	treeScroller.setMinimumSize(new Dimension(170, 250));
    	treeScroller.setPreferredSize(new Dimension(400, 500));
    	
    	GridBagConstraints gbc = new GridBagConstraints();
	    
	    gbc.insets = new Insets(4,3,4,3);

	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.weightx = 1.0;
	    gbc.gridwidth = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
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
