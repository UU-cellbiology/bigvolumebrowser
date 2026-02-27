package bvb.animation.io;

public class StringCodec implements ValueCodec<String> {

    @Override
    public String getTypeId() {
        return "string";
    }

    @Override
    public Class<String> getValueClass() {
        return String.class;
    }

    @Override
    public Object encode(String value) {
        return value;
    }

    @Override
    public String decode(Object raw) {
        return (String)raw;
    }

}
