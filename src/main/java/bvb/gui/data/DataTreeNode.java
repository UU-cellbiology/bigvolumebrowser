package bvb.gui.data;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

import bvvpg.vistools.BvvStackSource;
import mpicbg.spim.data.generic.AbstractSpimData;

public class DataTreeNode implements TreeNode
{
	DataTreeNode root = null;
	public AbstractSpimData< ? > spimData = null;
	public BvvStackSource<?> bvvSource = null;
	final DataTreeModel dataModel;

	boolean isLeaf = false;
	
	String sDescription = "root";
	
	ImageIcon nodeIcon = null;
	
	//root node
	public DataTreeNode(final DataTreeModel dataModel_)
	{
		dataModel = dataModel_;
	}
	
	//bvv source
	public DataTreeNode(final DataTreeModel dataModel_, BvvStackSource<?> bvvSource_)
	{
		dataModel = dataModel_;
		bvvSource = bvvSource_;
		isLeaf = true;		
	}

	public void setDescription(String sText)
	{
		sDescription = sText;
	}
	
	public void setIcon(final ImageIcon in)
	{
		nodeIcon = in;
	}
	
	public ImageIcon getIcon()
	{
		return nodeIcon;
	}

	@Override
	public String toString()
	{
		return sDescription;
	}

	
	//spimdata source	
	public DataTreeNode(final DataTreeModel dataModel_, AbstractSpimData<?> spimData_)
	{
		dataModel = dataModel_;
		spimData = spimData_;
		isLeaf = false;		
	}
	
	@Override
	public Enumeration< DataTreeNode > children()
	{
		if(isLeaf)
		{
			return null;
		}
		return Collections.enumeration( dataModel.dataParentChildren.get( this ));
	}

	@Override
	public boolean getAllowsChildren()
	{		
		return !isLeaf;
	}

	@Override
	public TreeNode getChildAt( int arg0 )
	{
		if(isLeaf)
		{
			return null;
		}
		if(arg0<dataModel.dataParentChildren.get( this ).size())
		{
			return dataModel.dataParentChildren.get( this ).get( arg0 );
		}
		return null;
	}

	@Override
	public int getChildCount()
	{
		if(isLeaf || spimData == null)
		{
			return 0;
		}
		return dataModel.dataParentChildren.get( this ).size();
			
	}

	@Override
	public int getIndex( TreeNode arg0 )
	{
		List< DataTreeNode > listKids = dataModel.dataParentChildren.get( this );
		for(int i=0;i<listKids.size();i++)
		{
			
			if(listKids.get( i ).equals( arg0 ))
			{
				return i;
			}
		}
		return -1;
	}

	@Override
	public TreeNode getParent()
	{
		return dataModel.dataChildParent.get( this );
	}

	@Override
	public boolean isLeaf()
	{
		return isLeaf;
	}

}
