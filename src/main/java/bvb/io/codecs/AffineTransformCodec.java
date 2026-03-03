package bvb.io.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.imglib2.realtransform.AffineTransform3D;

public class AffineTransformCodec implements ValueCodec<AffineTransform3D> {

    @Override
    public String getTypeId() {
        return "affine3d";
    }

    @Override
    public Class<AffineTransform3D> getValueClass() {
        return AffineTransform3D.class;
    }

    @Override
    public Object encode(AffineTransform3D value) {
        double[] m = new double[12];
        value.toArray(m);
        return m;
    }
    @Override
    public AffineTransform3D decode(Object raw) {

        double[] m = new ObjectMapper()
                .convertValue(raw, double[].class);

        AffineTransform3D at = new AffineTransform3D();
        at.set(m);
        return at;
    }
}
