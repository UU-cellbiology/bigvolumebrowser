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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import bvvpg.vistools.BvvStackSource;
import mpicbg.spim.data.generic.AbstractSpimData;

public class DataTreeModel implements TreeModel
{
	public final ConcurrentHashMap < DataTreeNode, List< DataTreeNode> > dataParentChildren;

	public final ConcurrentHashMap < DataTreeNode, DataTreeNode > dataChildParent;
	
	final DataTreeNode rootNode;
	
	final ArrayList<TreeModelListener> listeners;
	
	final ImageIcon iconBDV;
	
	final ImageIcon iconBioFormats;
	
	final ImageIcon iconFIJI;
	
	final ImageIcon iconMoBIE;
	
	final ImageIcon iconDefaultData;
	
	final ImageIcon iconOneSource;
	
	public enum SPIMDataType {
		  BDV,
		  BIOFORMATS,
		  FIJI,
		  MOBIE,
		  SOURCE
		}

	public DataTreeModel()
	{
		dataParentChildren =  new ConcurrentHashMap<>();
		dataChildParent =  new ConcurrentHashMap<>();
		rootNode = new DataTreeNode(this);
		listeners = new ArrayList<>();

		iconBDV = new ImageIcon(this.getClass().getResource("/icons/bdv-small.png"));
		iconBioFormats = new ImageIcon(this.getClass().getResource("/icons/bioformats-small.png"));
		iconFIJI = new ImageIcon(this.getClass().getResource("/icons/fiji-logo-small.png"));
		iconMoBIE = new ImageIcon(this.getClass().getResource("/icons/mobie-logo-small.png"));		
		iconDefaultData = new ImageIcon(this.getClass().getResource("/icons/data-small-default.png"));
		iconOneSource = new ImageIcon(this.getClass().getResource("/icons/source-small.png"));

	}
	
	public void addData(final AbstractSpimData< ? > spimData, final List<BvvStackSource<?>> bvvList, BVBSpimDataInfo info)
	{
		addData(spimData, bvvList, info.sourceDescription, info.icon);
	}
	
	public void addData(final AbstractSpimData< ? > spimData, final List<BvvStackSource<?>> bvvList, String dataName, final ImageIcon spimDataIcon)
	{
		List<DataTreeNode> spim =  dataParentChildren.get( rootNode );
		if(spim == null)
			spim = new ArrayList<>();
		final DataTreeNode spimNode = new DataTreeNode(this, spimData);
		spimNode.setDescription( dataName );
		spimNode.setIcon( spimDataIcon );
		spim.add( spimNode );
		dataParentChildren.put( rootNode, spim );
		dataChildParent.put( spimNode, rootNode );
		ArrayList<DataTreeNode> sourcesTN = new ArrayList<>(); 
		for(BvvStackSource<?> src :bvvList)
		{
			final DataTreeNode srcNode = new DataTreeNode(this, src);
			srcNode.setDescription( src.getSources().get( 0 ).getSpimSource().getName() );
			sourcesTN.add( srcNode );
			dataChildParent.put( srcNode, spimNode );
			
		}
		dataParentChildren.put( spimNode, sourcesTN );
		
		fireTreeStructureChanged();
	}
	
	@Override
	public void addTreeModelListener( TreeModelListener arg0 )
	{
		listeners.add( arg0 );		
	}
	
	protected void fireTreeStructureChanged() 
	{

		TreeModelEvent e = new TreeModelEvent(this, 
				new Object[] {rootNode});
		for (TreeModelListener tml : listeners) 
		{
			tml.treeStructureChanged(e);
		}
	}
	
	public void clearAllSources()
	{
		dataParentChildren.clear();
		dataChildParent.clear();
		
	}
	@SuppressWarnings( "cast" )
	@Override
	public Object getChild( Object parent, int index )
	{
		if(parent==null || !(parent instanceof DataTreeNode) )
			return null;
		final List< DataTreeNode > list = dataParentChildren.get((DataTreeNode)parent );
		if(list == null)
			return null;
		if(index<list.size())
			return list.get(index);
		return null;
	}

	
	@SuppressWarnings( "cast" )
	@Override
	public int getChildCount( Object parent )
	{
		if(!(parent instanceof DataTreeNode))
			return 0;
				
		final List< DataTreeNode > list = dataParentChildren.get((DataTreeNode)parent );
		
		if(list == null)
			return 0;
		
		return dataParentChildren.get((DataTreeNode)parent ).size();
	}

	
	@Override
	public int getIndexOfChild( Object parent, Object child )
	{
		if(parent==null || child == null )
			return 0;
		if(!(parent instanceof DataTreeNode))
			return 0;
		if(!(child instanceof DataTreeNode))
			return 0;
		@SuppressWarnings( "cast" )
		final List< DataTreeNode > list = dataParentChildren.get((DataTreeNode)parent );
		if(list == null)
			return 0;
		for(int i=0; i<list.size();i++)
		{
			if(list.get( i ).equals( child ))
				return i;
		}
		return 0;
	}

	@Override
	public Object getRoot()
	{
		return rootNode;
	}

	@Override
	public boolean isLeaf( Object node )
	{
		if(node instanceof DataTreeNode)
			return ((DataTreeNode)node).isLeaf();
		return false;
	}

	@Override
	public void removeTreeModelListener( TreeModelListener l )
	{
		listeners.remove( l );		
	}

	@Override
	public void valueForPathChanged( TreePath path, Object newValue )
	{
		
	}
	
	public ImageIcon getIconBDV()
	{
		return iconBDV;
	}
	
	public ImageIcon getIconBioformats()
	{
		return iconBioFormats;
	}
	
	public ImageIcon getIconFIJI()
	{
		return iconFIJI;
	}

	public ImageIcon getIconMoBIE()
	{
		return iconMoBIE;
	}
	
	public ImageIcon getIconDataDefault()
	{
		return iconDefaultData;
	}
	
	public ImageIcon getIconOneSource()
	{
		return iconOneSource;
	}
}
