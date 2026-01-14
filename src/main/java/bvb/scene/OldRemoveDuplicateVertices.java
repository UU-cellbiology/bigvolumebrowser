package bvb.scene;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.mesh.Mesh;
import net.imglib2.mesh.Triangle;
import net.imglib2.mesh.Vertex;
import net.imglib2.mesh.impl.nio.BufferMesh;

/**
 * @author Deborah Schmidt
 */
public class OldRemoveDuplicateVertices
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
			triangles[ trianglesCount ][ 0 ] = getVertex( vertices, p1, precision );
			triangles[ trianglesCount ][ 1 ] = getVertex( vertices, p2, precision );
			triangles[ trianglesCount ][ 2 ] = getVertex( vertices, p3, precision );
			trianglesCount++;
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
	
	public static void oldCalculateNormals( final net.imglib2.mesh.Mesh src, final net.imglib2.mesh.Mesh dest )
	{

		// Compute the triangle normals.
		final HashMap< Long, float[] > triNormals = new HashMap<>();
		for ( final Triangle tri : src.triangles() )
		{
			final int v0 = ( int ) tri.vertex0();
			final int v1 = ( int ) tri.vertex1();
			final int v2 = ( int ) tri.vertex2();

			final float v0x = src.vertices().xf( v0 );
			final float v0y = src.vertices().yf( v0 );
			final float v0z = src.vertices().zf( v0 );
			final float v1x = src.vertices().xf( v1 );
			final float v1y = src.vertices().yf( v1 );
			final float v1z = src.vertices().zf( v1 );
			final float v2x = src.vertices().xf( v2 );
			final float v2y = src.vertices().yf( v2 );
			final float v2z = src.vertices().zf( v2 );

			final float v10x = v1x - v0x;
			final float v10y = v1y - v0y;
			final float v10z = v1z - v0z;

			final float v20x = v2x - v0x;
			final float v20y = v2y - v0y;
			final float v20z = v2z - v0z;

			final float nx = v10y * v20z - v10z * v20y;
			final float ny = v10z * v20x - v10x * v20z;
			final float nz = v10x * v20y - v10y * v20x;
			final float nmag = ( float ) Math.sqrt( Math.pow( nx, 2 ) + Math.pow( ny, 2 ) + Math.pow( nz, 2 ) );

			triNormals.put( tri.index(), new float[] { nx / nmag, ny / nmag, nz / nmag } );
		}

		// Next, compute the normals per vertex based on face normals
		final HashMap< Long, float[] > vNormals = new HashMap<>();
		// Note: these are cumulative until normalized by vNbrCount

		float[] cumNormal, triNormal;
		for ( final Triangle tri : src.triangles() )
		{
			triNormal = triNormals.get( tri.index() );
			for ( final long idx : new long[] { tri.vertex0(), tri.vertex1(), tri.vertex2() } )
			{
				cumNormal = vNormals.getOrDefault( idx, new float[] { 0, 0, 0 } );
				cumNormal[ 0 ] += triNormal[ 0 ];
				cumNormal[ 1 ] += triNormal[ 1 ];
				cumNormal[ 2 ] += triNormal[ 2 ];
				vNormals.put( idx, cumNormal );
			}
		}

		// Now populate dest
		final Map< Long, Long > vIndexMap = new HashMap<>();
		float[] vNormal;
		double vNormalMag;
		// Copy the vertices, keeping track when indices change.
		for ( final Vertex v : src.vertices() )
		{
			final long srcIndex = v.index();
			vNormal = vNormals.get( v.index() );
			vNormalMag = Math.sqrt( Math.pow( vNormal[ 0 ], 2 ) + Math.pow( vNormal[ 1 ], 2 ) + Math.pow( vNormal[ 2 ], 2 ) );
			final long destIndex = dest.vertices().add( //
					v.x(), v.y(), v.z(), //
					vNormal[ 0 ] / vNormalMag, vNormal[ 1 ] / vNormalMag, vNormal[ 2 ] / vNormalMag, //
					v.u(), v.v() );
			if ( srcIndex != destIndex )
			{
				/*
				 * NB: If the destination vertex index matches the source, we
				 * skip recording the entry, to save space in the map. Later, we
				 * leave indexes unchanged which are absent from the map.
				 * 
				 * This scenario is actually quite common, because vertices are
				 * often numbered in natural order, with the first vertex having
				 * index 0, the second having index 1, etc., although it is not
				 * guaranteed.
				 */
				vIndexMap.put( srcIndex, destIndex );
			}
		}
		// Copy the triangles, taking care to use destination indices.
		for ( final Triangle tri : src.triangles() )
		{
			final long v0src = tri.vertex0();
			final long v1src = tri.vertex1();
			final long v2src = tri.vertex2();
			final long v0 = vIndexMap.getOrDefault( v0src, v0src );
			final long v1 = vIndexMap.getOrDefault( v1src, v1src );
			final long v2 = vIndexMap.getOrDefault( v2src, v2src );
			triNormal = triNormals.get( tri.index() );
			dest.triangles().add( v0, v1, v2, triNormal[ 0 ], triNormal[ 1 ], triNormal[ 2 ] );
		}
	}
}
