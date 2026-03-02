package bvb.animation.utils;

import bvb.animation.KeyFrameScene;
import bvb.core.BigVolumeBrowser;
import bvb.io.codecs.ValueCodec;
import bvb.io.dto.KeyframeDTO;

public class Keyframe< T >
{
    public final T value;
    public final KeyFrameScene parentKF;    

    public Keyframe(final T value, final KeyFrameScene parentKF) 
    {
    	this.parentKF = parentKF;
        this.value = value;
    }
    
    public float getTime()
    {
    	return parentKF.getMovieTimePoint();
    }
    
    @SuppressWarnings( "unchecked" )
	public static <T> Keyframe<T> fromDTO(final KeyframeDTO dto, final KeyFrameScene parentKF_)
    {
    	
    	final ValueCodec<T> codec = BigVolumeBrowser.registry.getById(dto.valueType);
    	
    	if (codec == null)
    		 throw new IllegalStateException(
    	                "Unknown valueType: " + dto.valueType);
    	
    	Object decoded = codec.decode(dto.value);
    	
    	return new Keyframe<>( (T)decoded, parentKF_);
    }

	public KeyframeDTO toGTO()
    {
    	@SuppressWarnings( "unchecked" )
		final ValueCodec<T> codec = BigVolumeBrowser.registry.getByClass( (Class<T>) this.value.getClass() );
    	if (codec == null)
   	        throw new IllegalStateException("No codec for " + this.value.getClass());
    	
    	final KeyframeDTO out = new KeyframeDTO();
    	out.kfsId = parentKF.getCurrentID();
    	out.valueType = codec.getTypeId();
    	out.value = codec.encode( this.value );
    	
    	return out;
    }
}
