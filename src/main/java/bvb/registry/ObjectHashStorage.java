package bvb.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.imglib2.RandomAccessibleInterval;

import bdv.tools.brightness.ConverterSetup;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bvb.gui.data.BVBSpimDataInfo;
import bvb.io.dto.BVBObjectsDTO;
import bvb.shapes.BasicShape;
import bvb.utils.Misc;
import bvvpg.vistools.BvvStackSource;

public class ObjectHashStorage
{

	public final ConcurrentHashMap < Object, String > objectHash;
	public final ConcurrentHashMap < String, Object > hashObject;
	public final ConcurrentHashMap < String, Integer > stringIndex;
	
	public ObjectHashStorage()
	{	
		objectHash = new ConcurrentHashMap<>();	
		hashObject = new ConcurrentHashMap<>();	
		stringIndex = new ConcurrentHashMap<>();	
	}
	
	public void clear()
	{
		objectHash.clear();
		hashObject.clear();
		stringIndex.clear();
	}
	
	public void addBVVSources(final List<BvvStackSource<?>> bvvList, BVBSpimDataInfo info)
	{		
		if(bvvList == null)
			return;
		
		if(bvvList.size() == 0)
			return;
		
		final ArrayList< SourceAndConverter< ? > > sacList = Misc.bvvSourcesToSaCList(bvvList);
		
		for(final SourceAndConverter< ? > sac : sacList)
		{
			String hash = info.sourceDescription + sac.getSpimSource().getName();
			final Source< ? > src = sac.getSpimSource();
			final RandomAccessibleInterval< ? > rai0 = src.getSource( 0, 0 );
			if(rai0 != null)
			{
				final long[] dim = rai0.dimensionsAsLongArray();
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
			final int ind = getHashIndex(hash);
			
			hash = Integer.toString( ind ) + "_" + hash;
			objectHash.put(bvvList.get(0).getBvvHandle().getConverterSetups().getConverterSetup( sac ), hash);
			hashObject.put( hash, bvvList.get(0).getBvvHandle().getConverterSetups().getConverterSetup( sac ) );
			//System.out.println(hash);

		}
		
	}
	
	public void addShapes(final List<BasicShape> shapes_in, String shapeGroupName)
	{
		for(final BasicShape sh : shapes_in)
		{
			String hash = "";
			hash = hash  + sh.getClass().getName();
			hash = hash + sh.toString() + shapeGroupName;
			final int ind = getHashIndex(hash);			
			hash = Integer.toString( ind ) + "_" + hash;
			objectHash.put(sh, hash);
			hashObject.put( hash, sh );
			//System.out.println(hash);
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
	
	public Object getObjectFromHash(final String hash)
	{
		return hashObject.get( hash );
	}

	public List<String> getAllObjectIDs()
	{
		return new ArrayList<>( hashObject.keySet());
	}
	
	public BVBObjectsDTO toDTO()
	{
		BVBObjectsDTO out = new BVBObjectsDTO();
		out.presentObjectsNames = getAllObjectIDs();
		return out;
	}
	
}
