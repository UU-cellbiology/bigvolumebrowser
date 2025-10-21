package bvb.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.imglib2.FinalRealInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bvb.core.BVVSettings;
import bvb.core.BigVolumeBrowser;
import bvb.geometry.Cuboid3D;
import bvb.geometry.Intersections3D;
import bvb.geometry.Line3D;
import bvb.shapes.BasicShape;
import bvvpg.core.util.MatrixMath;

public class CanvasSelection
{

	public static Object findClosestObjectOnCanvasOnClick(final BigVolumeBrowser bvb, boolean bShouldNotBeSelected)
	{	
		double minZ = Double.POSITIVE_INFINITY; 
		
		Object closestObject = null;
		
		final Line3D clickRay =  findClickRay(bvb, true);
		if(clickRay == null)
			return null;
		
		final AffineTransform3D viewTransform = new AffineTransform3D();
		bvb.bvvViewer.state().getViewerTransform(viewTransform);
		final List<SourceAndConverter<?>> selectedSAC = bvb.selectedObjects.getSelectedSources();
		final List<BasicShape> selectedShapes = bvb.selectedObjects.getSelectedShapes();
		//sources
		final Set< SourceAndConverter< ? > > visibleSet = bvb.bvvViewer.state().getVisibleSources();
		final int nTimePoint = bvb.bvvViewer.state().getCurrentTimepoint();
		for(final SourceAndConverter< ? > sac :visibleSet)
		{
			if(bShouldNotBeSelected && selectedSAC.contains( sac ))
			{
				continue;
			}
			final Source< ? > src = sac.getSpimSource();
			if(src.isPresent( nTimePoint ))
			{
				final double [] min = src.getSource( nTimePoint, 0 ).minAsDoubleArray();
				final double [] max = src.getSource( nTimePoint, 0 ).maxAsDoubleArray();
				//extend to include all range
				for(int d=0; d<3; d++)
				{
					min[d] -= 0.5;
					max[d] += 0.5;
				}
				final FinalRealInterval interval = new FinalRealInterval(min, max);
				final AffineTransform3D srcTransform = new AffineTransform3D();
				src.getSourceTransform( nTimePoint, 0, srcTransform );
				Cuboid3D objectCube = new Cuboid3D(interval);
				objectCube.applyTransform( srcTransform );
				objectCube.applyTransform( viewTransform );
				final ArrayList< RealPoint > intersectionPoints = Intersections3D.cuboidLinesIntersect(objectCube, clickRay);
				if(intersectionPoints.size() > 0)
				{
					for(final RealPoint rp:intersectionPoints)
					{
						if(rp.getDoublePosition( 2 ) < minZ)
						{
							minZ = rp.getDoublePosition( 2 );
							closestObject = sac;
						}
					}
				}
			}
		}
		
		
		for(final BasicShape sh : bvb.shapes)
		{
			if(bShouldNotBeSelected && selectedShapes.contains( sh ))
			{
				continue;
			}

			if(sh.isVisible())
			{				
				Cuboid3D objectCube = new Cuboid3D(sh.boundingBoxNotTransformed());
				final AffineTransform3D shTransform = new AffineTransform3D();
				sh.getTransform( shTransform );				
				objectCube.applyTransform( shTransform );
				objectCube.applyTransform( viewTransform );
				final ArrayList< RealPoint > intersectionPoints = Intersections3D.cuboidLinesIntersect(objectCube, clickRay);
				if(intersectionPoints.size() > 0)
				{
					for(final RealPoint rp:intersectionPoints)
					{
						if(rp.getDoublePosition( 2 ) < minZ)
						{
							minZ = rp.getDoublePosition( 2 );
							closestObject = sh;
						}
					}
				}

			}
		}
		
		return closestObject;

	}
	/** returns a click line through the frustum,
	 * in the world system of  coordinates.
	 * If withViewerTransform is true, applies ViewerTransform **/
	public static Line3D findClickRay(final BigVolumeBrowser bvb, boolean withViewerTransform)
	{
		java.awt.Point point_mouse  = bvb.bvvViewer.getMousePosition();

		if(point_mouse == null)
		{
			return null;
		}
		//get perspective matrix:
		final AffineTransform3D viewTransform = new AffineTransform3D();
		bvb.bvvViewer.state().getViewerTransform(viewTransform);
		int sW = bvb.bvvViewer.getWidth();
		int sH = bvb.bvvViewer.getHeight();
		Matrix4f matPerspWorld = new Matrix4f();
		MatrixMath.screenPerspective(bvb.bvvViewer.getProjectionType(), BVVSettings.dCam, BVVSettings.dClipNear, BVVSettings.dClipFar, sW, sH, 0, matPerspWorld ).mul( MatrixMath.affine( viewTransform, new Matrix4f() ) );
		

		Vector3f temp = new Vector3f(); 
		
		//Main click Line 
		RealPoint [] mainLinePoints = new RealPoint[2];
		for (int z =0 ; z<2; z++)
		{
			//take coordinates in original data volume space
			matPerspWorld.unproject(point_mouse.x,sH-(float)point_mouse.y,z, //z=1 ->far from camera z=0 -> close to camera
					new int[] { 0, 0, sW, sH },temp);

			mainLinePoints[z] = new RealPoint(temp.x,temp.y,temp.z);			
		}
		if(withViewerTransform)
		{							
			for(int i=0; i<2; i++)
			{
				viewTransform.apply( mainLinePoints[i], mainLinePoints[i] );				
			}
		}
		return new Line3D(mainLinePoints[0],mainLinePoints[1]);
	}
}
