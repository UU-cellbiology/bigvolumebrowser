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
package bvb.io.dto;

import java.util.Arrays;

public class IndexColorModelDTO
{
    // Metadata
    public int bits;
    public int size;
    public boolean hasAlpha;
    public int transparentPixel = -1;

    // Color tables (0–255 values)
    public int[] r;
    public int[] g;
    public int[] b;
    public int[] a; // nullable if hasAlpha == false

    public IndexColorModelDTO() {
    }

    // Optional convenience constructor
    public IndexColorModelDTO(int bits, int size, boolean hasAlpha) {
        this.bits = bits;
        this.size = size;
        this.hasAlpha = hasAlpha;

        this.r = new int[size];
        this.g = new int[size];
        this.b = new int[size];

        if (hasAlpha) {
            this.a = new int[size];
        }
    }

    @Override
    public String toString() {
        return "IndexColorModelDTO{" +
                "bits=" + bits +
                ", size=" + size +
                ", hasAlpha=" + hasAlpha +
                ", transparentPixel=" + transparentPixel +
                ", r=" + Arrays.toString(r) +
                ", g=" + Arrays.toString(g) +
                ", b=" + Arrays.toString(b) +
                ", a=" + Arrays.toString(a) +
                '}';
    }
}
