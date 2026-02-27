package bvb.animation.io;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.imglib2.FinalRealInterval;

public class FinalRealIntervalCodec implements ValueCodec<FinalRealInterval> {

    @Override
    public String getTypeId() {
        return "finalRealInterval";
    }

    @Override
    public Class<FinalRealInterval> getValueClass() {
        return FinalRealInterval.class;
    }

    @Override
    public Object encode(final FinalRealInterval value) {
        double[][] m = new double[2][3];
        value.realMin( m[0] );
        value.realMax( m[1] );
        return m;
    }
    @Override
    public FinalRealInterval decode(Object raw) {

        double[][] m = new ObjectMapper()
                .convertValue(raw, double[][].class);

        FinalRealInterval rInt = new FinalRealInterval( m[0], m[1] );
        return rInt;
    }
}

