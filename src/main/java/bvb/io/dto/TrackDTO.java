package bvb.io.dto;

import java.util.ArrayList;
import java.util.List;

public class TrackDTO
{
	   public String objectId;
	   public String property;
	   public List<KeyframeDTO> keyframes = new ArrayList<>();
	   public TrackDTO() 
	   {
	   }
}
