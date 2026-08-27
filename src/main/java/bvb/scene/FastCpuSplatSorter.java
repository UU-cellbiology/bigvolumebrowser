package bvb.scene;
import java.util.Arrays;

public class FastCpuSplatSorter {

    // Scratch buffers to avoid Garbage Collection allocations every frame
    private long[] keyIndexBuffer;
    private float[] tmpVertices;
    private float[] tmpSizes;
    private float[] tmpColors;

    public void allocate(int n) {
        if (keyIndexBuffer == null || keyIndexBuffer.length < n) {
            keyIndexBuffer = new long[n];
            tmpVertices = new float[n * 3];
            tmpSizes = new float[n];
            tmpColors = new float[n * 4];
        }
    }

    public void sortBackToFront(
        int N,
        float[] vertices, 
        float[] spotSizes, 
        float[] colors, 
        float viewX, float viewY, float viewZ, float zOff, boolean hasColors, boolean hasSizes
    ) {
       // allocate(N);

        // 1. Compute view-space depths and pack into 64-bit primitive keys
        for (int i = 0; i < N; i++) {
            float vx = vertices[i * 3];
            float vy = vertices[i * 3 + 1];
            float vz = vertices[i * 3 + 2];

            // Project along camera view vector
            float depth = vx * viewX + vy * viewY + vz * viewZ + zOff;
            //float depth = vz;

            // Convert IEEE-754 float to sortable uint32 bits
            int fBits = Float.floatToIntBits(depth);
            // Flip sign bits so negative floats sort correctly as unsigned integers
            int sortableDepth = (fBits < 0) ? (fBits ^ 0x7FFFFFFF) : (fBits ^ 0x80000000);

            // Pack: [ 32-bit Sortable Depth | 32-bit Index ]
            keyIndexBuffer[i] = (((long) sortableDepth) << 32) | (i & 0xFFFFFFFFL);
        }

        // 2. Multithreaded Dual-Pivot Quicksort / TimSort over primitive longs
        Arrays.parallelSort(keyIndexBuffer, 0, N);

        // 3. Reorder primitive arrays into temporary buffers
        for (int i = 0; i < N; i++) {
            // For Front-to-Back: read keyIndexBuffer[i]
            // For Back-to-Front: read keyIndexBuffer[N - 1 - i]
            long packed = keyIndexBuffer[N - 1 - i];
            int origIdx = (int) (packed & 0xFFFFFFFFL);

            // Copy 3D position
            tmpVertices[i * 3]     = vertices[origIdx * 3];
            tmpVertices[i * 3 + 1] = vertices[origIdx * 3 + 1];
            tmpVertices[i * 3 + 2] = vertices[origIdx * 3 + 2];

            if(hasSizes)
            {
	            // Copy Spot Size
	            tmpSizes[i] = spotSizes[origIdx];
            }
            
            if(hasColors)
            {
	            // Copy RGBA Color
	            tmpColors[i * 4]     = colors[origIdx * 4];
	            tmpColors[i * 4 + 1] = colors[origIdx * 4 + 1];
	            tmpColors[i * 4 + 2] = colors[origIdx * 4 + 2];
	            tmpColors[i * 4 + 3] = colors[origIdx * 4 + 3];
            }
        }

        // 4. Copy back to source buffers (or upload tmp* buffers directly to GPU)
        System.arraycopy(tmpVertices, 0, vertices, 0, N * 3);
        if(hasSizes)
        	System.arraycopy(tmpSizes, 0, spotSizes, 0, N);
        if(hasColors)
        	System.arraycopy(tmpColors, 0, colors, 0, N * 4);
    }
}