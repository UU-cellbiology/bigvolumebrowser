package bvb.scene;

import java.awt.image.IndexColorModel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Misc
{
	public static ByteBuffer ICMToByteBuffer(final IndexColorModel icm )
	{
		ByteBuffer data = null;
		if(icm != null)
		{
			int size_ = icm.getMapSize();
			if (size_ < 65536)
			{
				int nTextureSpan = 256*(int)Math.ceil(size_/256.0);
				final int numBytes = 4 * nTextureSpan;
				data = ByteBuffer.allocateDirect( numBytes ); // allocate a bit more than needed...
				data.order( ByteOrder.nativeOrder() );	
				final IntBuffer sdata = data.asIntBuffer();
				byte [][] colorsARGB = new byte[4][nTextureSpan];
				icm.getAlphas( colorsARGB[0] );
				icm.getReds( colorsARGB[1] );
				icm.getGreens( colorsARGB[2] );
				icm.getBlues( colorsARGB[3] );
				int all = 0;
				for (int i=0; i<size_;i++)
				{
					final int a = colorsARGB[0][i] & 0xff;
					final int r = colorsARGB[1][i] & 0xff;
					final int g = colorsARGB[2][i] & 0xff;
					final int b = colorsARGB[3][i] & 0xff;
					all = ( a << 24 ) | ( b << 16 ) | ( g << 8 ) | r;
					sdata.put( i, all );	
				}
				//fill the rest with the last color
				for (int i = size_; i < nTextureSpan; i++)
				{
					sdata.put( i, all );
				}
			}
		}
		return data;
	}
}
