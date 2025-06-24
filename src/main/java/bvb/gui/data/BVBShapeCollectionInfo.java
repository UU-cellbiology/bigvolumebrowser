package bvb.gui.data;

import java.util.ArrayList;
import java.util.List;

import bvb.core.BigVolumeBrowser;
import bvb.shapes.BasicShape;

public class BVBShapeCollectionInfo
{
	
	public final String collectionDescription;
	
	public final List<BasicShape> shapes;
	
	public BVBShapeCollectionInfo(final List<BasicShape> shapes_, final String collectionDescription_)
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
