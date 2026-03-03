package bvb.io.dto;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.realtransform.AffineTransform3D;

import bvb.animation.SceneView;
import bvb.animation.utils.PropertyBinding;
import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;
import bvb.io.codecs.ValueCodec;
import bvb.registry.PropertyRegistry;
import ij.IJ;

public class SceneStateDTO
{
	public String BVBVersion;
	public BVBObjectsDTO bvbObjects;
	public List<ObjectStateDTO> objects = new ArrayList<>();
	public int timePoint;
	public double [] transformMatrix = new double[12];
	
	public SceneStateDTO() {}
	
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static SceneStateDTO captureState (final BigVolumeBrowser bvb)
	{
		 final SceneStateDTO scene = new SceneStateDTO();
		 scene.BVBVersion = BVBSettings.sVersion;
		 scene.bvbObjects = bvb.objectHashStorage.toDTO();
		 SceneView sceneView = SceneView.getCurrentSceneView( bvb.bvvViewer );
		 scene.timePoint = sceneView.getTimeFrame();
		 sceneView.getViewerTransform().toArray( scene.transformMatrix );
		 for (String objectId : scene.bvbObjects.presentObjectsNames) 
		 {
			 final ObjectStateDTO objDTO = new ObjectStateDTO();
		     objDTO.objectId = objectId;
		     final List< String > prNames = PropertyRegistry.getPropertyNames( bvb.objectHashStorage.getObjectFromHash( objectId ) );
		     for (String propertyName : prNames) 
		     {
		    	 final PropertyBinding< Object > binding = bvb.propertyRegistry.get( objectId, propertyName);
		    	 final  Object value = binding.property.get();
		    	 final ValueCodec codec =
		    			 BigVolumeBrowser.registry.getByClass(value.getClass());
		    	 final PropertyStateDTO propDTO = new PropertyStateDTO();
		         propDTO.propertyName = propertyName;
		         propDTO.valueType = codec.getTypeId();
		         propDTO.value = codec.encode(value);
		         objDTO.properties.add(propDTO);
		     }
		     scene.objects.add(objDTO);
		 }
		 return scene;
	}
	
	@SuppressWarnings( "rawtypes" )
	public static void restoreState(final BigVolumeBrowser bvb, final SceneStateDTO scene, final String filename)
	{
		
		bvb.bvvViewer.state().setCurrentTimepoint( scene.timePoint );
		final AffineTransform3D viewerTransform = new AffineTransform3D();
		viewerTransform.set( scene.transformMatrix );
		final SceneView sceneView = new SceneView(viewerTransform, scene.timePoint);
		SceneView.setSceneView( bvb.bvvViewer, sceneView );

		for (ObjectStateDTO objDTO : scene.objects) 
		{
			final Object obj = bvb.objectHashStorage.getObjectFromHash( objDTO.objectId );

			if (obj == null)
			{
				if(filename != null)
				{
					IJ.log( "WARNING:  BVB recover scene state, while loading" +  filename);					
				}
				IJ.log( "BVB: Cannot find object " +  objDTO.objectId);
				continue; 
			}
			for (final PropertyStateDTO propDTO : objDTO.properties) 
			{
				final PropertyBinding< Object > binding = bvb.propertyRegistry.get( objDTO.objectId, propDTO.propertyName);

				if (binding == null)
					continue;

				final ValueCodec codec =
						BigVolumeBrowser.registry.getById(propDTO.valueType);

				final Object decoded =
						codec.decode(propDTO.value);

				binding.property.set(decoded);
			}
		}
	}
}
