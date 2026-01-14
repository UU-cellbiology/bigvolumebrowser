package bvb.scene;


import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.impl.nio.BufferMesh;

/**
 * @author Deborah Schmidt
 */
public class RemoveMeshDuplicateVertices
{

	public static BufferMesh calculate( final Mesh mesh, final int precision )
	{
		final Map< String, IndexedVertex > vertices = new LinkedHashMap<>();
		final int[][] triangles = new int[ mesh.triangles().size() ][ 3 ];

		int trianglesCount = 0;
		for ( final net.imglib2.mesh.Triangle triangle : mesh.triangles() )
		{
			final RealPoint p1 = new RealPoint( triangle.v0x(), triangle.v0y(), triangle.v0z() );
			final RealPoint p2 = new RealPoint( triangle.v1x(), triangle.v1y(), triangle.v1z() );
			final RealPoint p3 = new RealPoint( triangle.v2x(), triangle.v2y(), triangle.v2z() );
			final int [] ind = new int [3]; 
			ind[ 0 ] = getVertex( vertices, p1, precision );
			ind[ 1 ] = getVertex( vertices, p2, precision );
			ind[ 2 ] = getVertex( vertices, p3, precision );
			
			if(ind[0] != ind[1] &&
			   ind[0] != ind[2] &&
			   ind[1] != ind[2])
			{			
				for (int d = 0; d < 3; d++)
				{
					triangles[ trianglesCount ][ d ] = ind [ d ];
				}
				trianglesCount++;

			}
		}
		final BufferMesh res = new BufferMesh( vertices.size(), triangles.length );
		vertices.values().forEach( vertex -> {
			res.vertices().add( vertex.point.getFloatPosition( 0 ), vertex.point.getFloatPosition( 1 ), vertex.point.getFloatPosition( 2 ) );
		} );

		for ( final int[] triangle : triangles )
		{
			res.triangles().add( triangle[ 0 ], triangle[ 1 ], triangle[ 2 ] );
		}
		return res;
	}

	private static int getVertex( final Map< String, IndexedVertex > vertices, final RealPoint point, final int precision )
	{
		final String hash = getHash( point, precision );
		final IndexedVertex vertex = vertices.get( hash );
		if ( vertex == null )
			return createVertex( vertices, hash, point, precision );
		return vertex.index;
	}

	private static int createVertex( final Map< String, IndexedVertex > vertices, final String hash, final RealPoint point, final int precision )
	{
		final int index = vertices.size();
		final IndexedVertex vertex = new IndexedVertex( point, index, precision );
		vertices.put( hash, vertex );
		return index;
	}

	private static String getHash( final RealPoint point, final int precision )
	{
		final int factor = ( int ) Math.pow( 10, precision );
		return Math.round( point.getFloatPosition( 0 ) * factor ) + "-" + Math.round( point.getFloatPosition( 1 ) * factor ) + "-" + Math.round( point.getFloatPosition( 2 ) * factor );
	}

	private static class IndexedVertex
	{

		int index;

		RealLocalizable point;

		IndexedVertex( final RealLocalizable pos, final int index, final int precision )
		{
			final double[] newpos = new double[ pos.numDimensions() ];
			final double factor = Math.pow( 10, precision );
			for ( int i = 0; i < newpos.length; i++ )
			{
				newpos[ i ] = Math.round( pos.getDoublePosition( i ) * factor ) / factor;
			}
			this.point = new RealPoint( newpos );
			this.index = index;
		}
	}
}
