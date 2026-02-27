package bvb.animation.io;

public class FloatCodec implements ValueCodec<Float> {

    @Override
    public String getTypeId() {
        return "float";
    }

    @Override
    public Class<Float> getValueClass() {
        return Float.class;
    }

    @Override
    public Object encode(Float value) {
        return value;
    }

    @Override
    public Float decode(Object raw) {
        return ((Number) raw).floatValue();
    }
}