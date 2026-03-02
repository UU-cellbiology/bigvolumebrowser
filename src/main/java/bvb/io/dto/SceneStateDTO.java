package bvb.io.dto;

import java.util.ArrayList;
import java.util.List;

import bvb.core.BVBSettings;
import bvb.core.BigVolumeBrowser;

public class SceneStateDTO
{
	public String BVBVersion;
	public BVBObjectsDTO bvbObjects;
	public List<ObjectStateDTO> objects = new ArrayList<>();
	
	public SceneStateDTO() {}
	
	public static SceneStateDTO captureState (final BigVolumeBrowser bvb)
	{
		 final SceneStateDTO scene = new SceneStateDTO();
		 scene.BVBVersion = BVBSettings.sVersion;
		 scene.bvbObjects = bvb.objectHashStorage.toDTO();
		 for (String objectId : scene.bvbObjects.presentObjectsNames) 
		 {
			 ObjectStateDTO objDTO = new ObjectStateDTO();
		     objDTO.objectId = objectId;
		 }
		 return scene;
	}
}
