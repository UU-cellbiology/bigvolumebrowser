package bvb.io.codecs;

public interface ValueCodec<T> 
{

    String getTypeId();   // "float", "color", "affine3d"

    Class<T> getValueClass();

    Object encode(T value);   // → JSON-safe object

    T decode(Object raw);     // ← JSON raw object
}
