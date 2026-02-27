package bvb.io.codecs;

import java.util.List;

public class DoubleArrayCodec implements ValueCodec<double[]> 
{

    @Override
    public String getTypeId() {
        return "doubleArray";
    }

    @Override
    public Class<double[]> getValueClass() {
        return double[].class;
    }

    @Override
    public Object encode(double[] value) {
        return value;  // JSON array of numbers
    }

    @Override
    public double[] decode(Object raw) {

        List<?> list = (List<?>) raw;

        double[] result = new double[list.size()];

        for (int i = 0; i < list.size(); i++) {
            result[i] = ((Number) list.get(i)).doubleValue();
        }

        return result;
    }

}
