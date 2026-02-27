package bvb.animation.io;

public class DoubleCodec implements ValueCodec<Double> {

    @Override
    public String getTypeId() {
        return "double";
    }

    @Override
    public Class<Double> getValueClass() {
        return Double.class;
    }

    @Override
    public Object encode(Double value) {
        return value;
    }

    @Override
    public Double decode(Object raw) {
        return ((Number) raw).doubleValue();
    }
}
