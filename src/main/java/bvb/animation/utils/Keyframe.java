package bvb.animation.utils;

import bvb.animation.AnimationPanel;
import bvb.animation.KeyFrameScene;
import bvb.animation.dto.KeyframeDTO;
import bvb.animation.io.ValueCodec;

public class Keyframe< T >
{
    public final T value;
    public final KeyFrameScene parentKF;    
    public final Easing easing;

    public Keyframe(final T value, final KeyFrameScene parentKF, final Easing easing) 
    {
    	this.parentKF = parentKF;
        this.value = value;
        this.easing = easing;
    }
    
    public float getTime()
    {
    	return parentKF.getMovieTimePoint();
    }
    
    @SuppressWarnings( "unchecked" )
	public static <T> Keyframe<T> fromDTO(final KeyframeDTO dto, final KeyFrameScene parentKF_)
    {
    	
    	final ValueCodec<T> codec = AnimationPanel.registry.getById(dto.valueType);
    	
    	if (codec == null)
    		 throw new IllegalStateException(
    	                "Unknown valueType: " + dto.valueType);
    	
    	Object decoded = codec.decode(dto.value);

    	Easing easing_out = Easing.LINEAR;
    	if(dto.easing.equals( "EASE_IN" ) )
    	{
    		easing_out = Easing.EASE_IN;
    	}
    	
    	if(dto.easing.equals( "EASE_OUT" ) )
    	{
    		easing_out = Easing.EASE_OUT;
    	}
    	
    	if(dto.easing.equals( "EASE_IN_OUT" ) )
    	{
    		easing_out = Easing.EASE_IN_OUT;
    	}
    	
    	return new Keyframe<>( (T)decoded, parentKF_, easing_out );
    }

	public KeyframeDTO toGTO()
    {
    	@SuppressWarnings( "unchecked" )
		final ValueCodec<T> codec = AnimationPanel.registry.getByClass( (Class<T>) this.value.getClass() );
    	if (codec == null)
   	        throw new IllegalStateException("No codec for " + this.value.getClass());
    	
    	final KeyframeDTO out = new KeyframeDTO();
    	out.kfsId = parentKF.getCurrentID();
    	out.valueType = codec.getTypeId();
    	out.value = codec.encode( this.value );
    	
    	if(easing == Easing.LINEAR)
    	{
    		out.easing = "LINEAR";
    	}
    	
    	if(easing == Easing.EASE_IN)
    	{
    		out.easing = "EASE_IN";
    	}

    	if(easing == Easing.EASE_OUT)
    	{
    		out.easing = "EASE_OUT";
    	}
    	
    	if(easing == Easing.EASE_IN_OUT)
    	{
    		out.easing = "EASE_IN_OUT";
    	}

    	return out;
    }
}
