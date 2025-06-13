package bvb.gui.data;

import java.util.ArrayList;

import bvb.core.BigVolumeBrowser;
import bvb.shapes.BasicShape;

public class BVBShapeCollectionInfo
{
	
	public final String collectionDescription;
	
	public final ArrayList<BasicShape> shapes;
	
	public BVBShapeCollectionInfo(final ArrayList<BasicShape> shapes_, final String collectionDescription_)
	{
		shapes = shapes_;
		collectionDescription = collectionDescription_;
	}
	
	public static ArrayList<BVBShapeCollectionInfo> assembleCurrentShapes(final BigVolumeBrowser bvb)
	{
		final ArrayList<BVBShapeCollectionInfo> out = new ArrayList<>();
		final DataTreeNode root = (DataTreeNode) bvb.dataTreeModel.getRoot();
		final int nEntries = root.getChildCount();
		for (int i = 0; i < nEntries; i++)
		{
			DataTreeNode child = ( DataTreeNode ) root.getChildAt( i );
			if(child.shapesArr != null)
			{
				out.add( new BVBShapeCollectionInfo(child.shapesArr, child.toString()));
			}
		}
		return out;
	}
}
