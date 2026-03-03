/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
