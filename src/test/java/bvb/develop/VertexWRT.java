package bvb.develop;

public class VertexWRT
{
	public final float [] xyz = new float[3];
	public final float [] uv = new float [2];
	public final float [] nxyz = new float[3];

	public void setXYZ(final float x,final  float y,final  float z)
	{
		this.xyz[0] = x;
		this.xyz[1] = y;
		this.xyz[2] = z;
	}
	
	public void setXYZ(String x,String y,String z)
	{
		this.xyz[0] = Float.parseFloat( x );
		this.xyz[1] = Float.parseFloat( y );
		this.xyz[2] = Float.parseFloat( z );
	}
	
	public void setUV(final float u, final float v)
	{
		this.uv[0] = u;
		this.uv[1] = v;
	}

	
	public void setUV(String u, String v)
	{
		this.uv[0] = Float.parseFloat( u );
		this.uv[1] = Float.parseFloat( v );
	}

	
	public void setNXYZ(final float x, final float y, final float z)
	{
		this.nxyz[0] = x;
		this.nxyz[1] = y;
		this.nxyz[2] = z;
	}
	public void setNXYZ(String x,String y,String z)
	{
		this.nxyz[0] =  Float.parseFloat( x );
		this.nxyz[1] = Float.parseFloat( y );
		this.nxyz[2] =  Float.parseFloat( z );
	}
	
}
