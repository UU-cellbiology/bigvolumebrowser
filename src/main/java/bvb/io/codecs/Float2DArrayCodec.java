package bvb.io.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Float2DArrayCodec implements ValueCodec<float[][]> {

    @Override
    public String getTypeId() {
        return "float2DArray";
    }

    @Override
    public Class<float[][]> getValueClass() {
        return float[][].class;
    }

    @Override
    public Object encode(float[][] value) {
        return value;
    }
    @Override
    public float[][] decode(Object raw) {

    	float[][] m = new ObjectMapper()
                .convertValue(raw, float[][].class);

        return m;
    }
}
