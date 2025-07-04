package bvb.shapes;

import bvb.scene.VisMeshColor;
import bvb.scene.VisSpots;

public class MiscShapes
{
	static public boolean isShapeTransparent (final BasicShape shape)
	{
		if(shape instanceof BasicMeshColor)
		{
			if(((BasicMeshColor)shape).getRenderType() == VisMeshColor.MESH)
			{
				//if(((BasicMeshColor)shape).getSurfaceRender() == VisMeshColor.SURFACE_SHADE)

				if(((BasicMeshColor)shape).getSurfaceRender() == VisMeshColor.SURFACE_SILHOUETTE)
					return true;
			}
		}
		if(shape instanceof Spots)
		{
			if(((Spots)shape).getRenderType()>=VisSpots.RENDER_GAUSS_UNIFORM)
				return true;
		}
		return false;
	}
}
