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
import bvb.io.codecs.StringCodec;
import bvb.io.codecs.ValueCodec;

public class ValueCodecRegistry
{

    private final Map<String, ValueCodec<?>> byId = new HashMap<>();
    private final Map<Class<?>, ValueCodec<?>> byClass = new HashMap<>();

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

    }
}