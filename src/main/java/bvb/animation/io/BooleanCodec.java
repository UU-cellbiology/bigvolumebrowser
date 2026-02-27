package bvb.animation.io;

public class BooleanCodec implements ValueCodec<Boolean> {

    @Override
    public String getTypeId() {
        return "boolean";
    }

    @Override
    public Class<Boolean> getValueClass() {
        return Boolean.class;
    }

    @Override
    public Object encode(Boolean value) {
        return value;
    }

    @Override
    public Boolean decode(Object raw) {
        return ((Boolean) raw).booleanValue();
    }
}

