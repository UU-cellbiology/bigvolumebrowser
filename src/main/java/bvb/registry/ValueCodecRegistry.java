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
package bvb.registry;

import java.util.HashMap;
import java.util.Map;

import bvb.io.codecs.AffineTransformCodec;
import bvb.io.codecs.BooleanCodec;
import bvb.io.codecs.ColorCodec;
import bvb.io.codecs.DoubleArrayCodec;
import bvb.io.codecs.DoubleCodec;
import bvb.io.codecs.FinalRealIntervalCodec;
import bvb.io.codecs.Float2DArrayCodec;
import bvb.io.codecs.FloatArrayCodec;
import bvb.io.codecs.FloatCodec;
import bvb.io.codecs.IntegerCodec;
import bvb.io.codecs.ICMCodec;
import bvb.io.codecs.StringCodec;
import bvb.io.codecs.ValueCodec;

public class ValueCodecRegistry
{

    private final Map<String, ValueCodec<?>> byId = new HashMap<>();
    private final Map<Class<?>, ValueCodec<?>> byClass = new HashMap<>();

    
    public ValueCodecRegistry()
    {
    	initializeAll();
    }
    
    public <T> void register(ValueCodec<T> codec) {
        byId.put(codec.getTypeId(), codec);
        byClass.put(codec.getValueClass(), codec);
    }

    @SuppressWarnings("unchecked")
    public <T> ValueCodec<T> getByClass(Class<T> clazz) {
        return (ValueCodec<T>) byClass.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> ValueCodec<T> getById(String id) {
        return (ValueCodec<T>) byId.get(id);
    }
    
    public void initializeAll()
    {
    	register( new BooleanCodec() );
		register( new IntegerCodec() );
		register( new FloatCodec() );
		register( new FloatArrayCodec() );
		register( new Float2DArrayCodec() );
		register( new DoubleCodec() );
		register( new DoubleArrayCodec() );
		register( new StringCodec() );
		register( new ColorCodec() );
		register( new AffineTransformCodec() );
		register( new FinalRealIntervalCodec() );
		register( new ICMCodec() );

    }
}
