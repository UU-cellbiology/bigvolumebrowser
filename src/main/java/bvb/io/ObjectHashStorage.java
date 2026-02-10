package bvb.io;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.RandomAccessibleInterval;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bvb.gui.data.BVBSpimDataInfo;
import bvb.shapes.BasicShape;
import bvvpg.vistools.BvvStackSource;

public class ObjectHashStorage
{

	public final ConcurrentHashMap < Object, String > objectHash;
	public final ConcurrentHashMap < String, Integer > stringIndex;
	
	public ObjectHashStorage()
	{	
		objectHash = new ConcurrentHashMap<>();	
		stringIndex = new ConcurrentHashMap<>();	
	}
	
	public void clear()
	{
		objectHash.clear();
		stringIndex.clear();
	}
	
	public void addBVVSources(final List<BvvStackSource<?>> bvvList, BVBSpimDataInfo info)
	{				
		for(final BvvStackSource<?> bvvStack : bvvList)
		{
			final SourceAndConverter< ? > sac = bvvStack.getSources().get( 0 );
			String hash = info.sourceDescription + sac.getSpimSource().getName();
			Source< ? > src = sac.getSpimSource();
			RandomAccessibleInterval< ? > rai0 = src.getSource( 0, 0 );
			if(rai0 != null)
			{
				long[] dim = rai0.dimensionsAsLongArray();
				hash = hash + "[";
				for (int d = 0; d < 3; d++)
				{
					hash = hash + Long.toString( dim[d] );
					if( d < 2 )
					{
						hash = hash + "x";
					}					
				}
				hash = hash + "]";
			}							
			int ind = getHashIndex(hash);
			
			hash = Integer.toString( ind ) + "_" + hash;
			objectHash.put(bvvStack.getBvvHandle().getConverterSetups().getConverterSetup( sac ), hash);
			System.out.println(hash);

		}
		
	}
	
	public void addShapes(final List<BasicShape> shapes_in, String shapeGroupName)
	{
		for(final BasicShape sh : shapes_in)
		{
			String hash = "";
			hash = hash  + sh.getClass().getName();
			hash = hash + sh.toString() + shapeGroupName;
			int ind = getHashIndex(hash);			
			hash = Integer.toString( ind ) + "_" + hash;
			objectHash.put(sh, hash);
			System.out.println(hash);
		}
	}
	
	/** in case the same files are loaded, let's just number them**/
	int getHashIndex(String hash)
	{
		Integer ind = stringIndex.get( hash );		
		if(ind == null)
		{
			ind = 0;			
		}
		else
		{
			ind++;			
		}
		stringIndex.put( hash, ind );
		return ind;
	}
	
	public String getBVBHashString(final Object obj)
	{
		if(obj instanceof ConverterSetup || obj instanceof BasicShape)
		{
			String out = objectHash.get( obj );
			if(out == null)
			{
				System.err.println("Warning! Cannot find the BVB hash name for " + obj.toString());
			}
			return out;
		}
		System.err.println("Error! Requesting hash of unknown BVB object.");
		return null;

	}
}
