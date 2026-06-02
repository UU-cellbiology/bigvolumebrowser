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
