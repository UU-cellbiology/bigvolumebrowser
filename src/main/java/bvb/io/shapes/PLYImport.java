package bvb.io.shapes;

import java.awt.Color;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import net.imglib2.RealPoint;

import bvb.scene.VisSpots;
import bvb.shapes.Spots;
import ij.IJ;

public class PLYImport extends SwingWorker<Void, String> 
{
	public File filein = null;
	public Spots spotsPLY = null;
	public RadiusMode radiusMode = RadiusMode.MAX_RADIUS;
	int nCount = 0;
	public boolean bSpotsRead = false;
	private static final float SH_C0 = 0.28209479177387814f;
	
	public enum RadiusMode {
        MAX_RADIUS,        // max(sx, sy, sz)
        AVG_RADIUS,        // mean (sx, sy, sz)
        EQUIVALENT_VOLUME  // cbrt(sx * sy * sz) 
    }
	
	@Override
    protected void process(List<String> chunks) 
	{
		String message = chunks.get( chunks.size() - 1 );
		if(message.startsWith( "Progress " ))
		{
			IJ.showProgress(Double.parseDouble( message.substring( 9, message.length() )));
		}

    }
	@Override
	protected Void doInBackground() throws Exception
	{
		FileChannel channel = FileChannel.open(filein.toPath(), StandardOpenOption.READ);
		ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
     // 1. Parse ASCII Header
        List<String> propertyNames = new ArrayList<>();
        int vertexCount = 0;
        int headerLength = 0;
        StringBuilder lineBuilder = new StringBuilder();
        while (buffer.hasRemaining()) {
            char c = (char) buffer.get();
            if (c == '\n') {
                String line = lineBuilder.toString().trim();
                lineBuilder.setLength(0);

                if (line.startsWith("element vertex")) {
                    vertexCount = Integer.parseInt(line.split("\\s+")[2]);
                } else if (line.startsWith("property float")) {
                    propertyNames.add(line.split("\\s+")[2]);
                } else if (line.equals("end_header")) {
                    headerLength = buffer.position();
                    break;
                }
            } else if (c != '\r') {
                lineBuilder.append(c);
            }
        }
        
        Map<String, Integer> propMap = new HashMap<>();
        for (int i = 0; i < propertyNames.size(); i++) {
            propMap.put(propertyNames.get(i), i);
        }
        int floatStride = propertyNames.size();
        //final List<RealPoint> points = new ArrayList<>(vertexCount);
        final List<RealPoint> points = new ArrayList<>();
        float [] colors = new float[(vertexCount)*4];
        float [] spotSizes = new float[vertexCount];
        
        spotsPLY = new Spots(-1.0f, new Color(255,0,0,255), VisSpots.SHAPE_ROUND, VisSpots.RENDER_GAUSS);

        // Property indices
        int idxX = propMap.get("x");
        int idxY = propMap.get("y");
        int idxZ = propMap.get("z");
        int idxOp = propMap.get("opacity");
        int idxS0 = propMap.get("scale_0");
        int idxS1 = propMap.get("scale_1");
        int idxS2 = propMap.get("scale_2");
        int idxDc0 = propMap.get("f_dc_0");
        int idxDc1 = propMap.get("f_dc_1");
        int idxDc2 = propMap.get("f_dc_2");
        
        // 2. Process Binary Vertex Payload
        buffer.position(headerLength);
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        float[] vertexData = new float[floatStride];
        for (int i = 0; i < vertexCount; i++) {
            floatBuffer.get(vertexData);

            points.add( new RealPoint(new double[] {vertexData[idxX], 
            										 vertexData[idxY], 
            										 vertexData[idxZ]}));
            // Scale Activation: exp(s)
            float sx = (float) Math.exp(vertexData[idxS0]);
            float sy = (float) Math.exp(vertexData[idxS1]);
            float sz = (float) Math.exp(vertexData[idxS2]);

            // Radius Approximation
            if (radiusMode == RadiusMode.MAX_RADIUS) {
                float maxScale = Math.max(sx, Math.max(sy, sz));
                spotSizes[i] = maxScale * 2.0f;
            }else if( radiusMode == RadiusMode.AVG_RADIUS)
            {
                spotSizes[i] = (sx + sy + sz )*2.0f / 3.0f;

            }
            
            else {
            	spotSizes[i] = (float) Math.cbrt(sx * sy * sz) * 2.0f;
            }

            // Opacity Activation: Sigmoid(opacity)
            float opRaw = vertexData[idxOp];
            colors[nCount * 4 + 3] = (float) (1.0 / (1.0 + Math.exp(-opRaw)));
            // Color Conversion: 0.5 + C0 * f_dc (Clamped [0, 1])
			colors[nCount * 4] = Math.max(0.0f, Math.min(1.0f, 0.5f + SH_C0 * vertexData[idxDc0]));
			colors[nCount * 4 + 1] =  Math.max(0.0f, Math.min(1.0f, 0.5f + SH_C0 * vertexData[idxDc1]));
			colors[nCount * 4 + 2] = Math.max(0.0f, Math.min(1.0f, 0.5f + SH_C0 * vertexData[idxDc2]));
//			colors[nCount * 4] = (float) Math.pow(Math.max(0.0f, Math.min(1.0f, 0.5f + SH_C0 * vertexData[idxDc0])), 1.0 / 2.2);
//			colors[nCount * 4 + 1] = (float) Math.pow(Math.max(0.0f, Math.min(1.0f, 0.5f + SH_C0 * vertexData[idxDc1])), 1.0 / 2.2);
//			colors[nCount * 4 + 2]  = (float) Math.pow(Math.max(0.0f, Math.min(1.0f, 0.5f + SH_C0 * vertexData[idxDc2])), 1.0 / 2.2);
            nCount++;
	    	publish("Progress " + Double.toString( (double)nCount/((double)vertexCount)));

        }
        
	    spotsPLY.setPoints( points, spotSizes, null );
	    spotsPLY.setColors( colors );
		return null;
	}
	
    @Override
    public void done() 
    {
    	try
		{
			get();
		}
		catch ( InterruptedException | ExecutionException exc )
		{
			exc.printStackTrace();
		}
     	catch (Exception e)
     	{
     		System.err.println("Error spots import: " + e.toString() );
     	}
    	
    	IJ.log("Loaded " + nCount +" points from "+ filein.getName());
    	IJ.showProgress( 1.0 );
    }
}
