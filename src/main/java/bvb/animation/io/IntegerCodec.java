package bvb.animation.io;

public class IntegerCodec implements ValueCodec<Integer> {

    @Override
    public String getTypeId() {
        return "int";
    }

    @Override
    public Class<Integer> getValueClass() {
        return Integer.class;
    }

    @Override
    public Object encode(Integer value) {
        return value;
    }

    @Override
    public Integer decode(Object raw) {
        return ((Number) raw).intValue();
    }
}