package bvb.animation.utils;
import java.awt.Color;

public class HSBColorInterpolator implements Interpolator<Color> {

    @Override
    public Color interpolate(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));

        float[] hsbA = Color.RGBtoHSB(a.getRed(), a.getGreen(), a.getBlue(), null);
        float[] hsbB = Color.RGBtoHSB(b.getRed(), b.getGreen(), b.getBlue(), null);

        float h = lerpAngle(hsbA[0], hsbB[0],  t);
        float s = lerp(hsbA[1], hsbB[1],  t);
        float br = lerp(hsbA[2], hsbB[2],  t);

        int rgb = Color.HSBtoRGB(h, s, br);

        int alpha = Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);

        Color base = new Color(rgb);
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // ensures shortest hue rotation
    private float lerpAngle(float a, float b, float t) {
        float diff = b - a;
        if (Math.abs(diff) > 0.5f) {
            if (diff > 0)
                a += 1f;
            else
                b += 1f;
        }
        float result = lerp(a, b, t);
        return (result + 1f) % 1f;
    }
}