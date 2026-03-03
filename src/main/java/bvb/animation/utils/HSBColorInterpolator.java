/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 - 2026 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
