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
