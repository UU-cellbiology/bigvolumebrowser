package bvb.io.codecs;

import java.util.List;

public class FloatArrayCodec implements ValueCodec<float[]> 
{

    @Override
    public String getTypeId() {
        return "floatArray";
    }

    @Override
    public Class<float[]> getValueClass() {
        return float[].class;
    }

    @Override
    public Object encode(float[] value) {
        return value;  // JSON array of numbers
    }

    @Override
    public float[] decode(Object raw) {

        List<?> list = (List<?>) raw;

        float[] result = new float[list.size()];

        for (int i = 0; i < list.size(); i++) {
            result[i] = ((Number) list.get(i)).floatValue();
        }

        return result;
    }
}