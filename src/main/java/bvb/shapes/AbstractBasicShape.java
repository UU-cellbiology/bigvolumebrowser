package bvb.shapes;

public abstract class AbstractBasicShape implements BasicShape
{
	boolean bVisible = true;
	
	int nTimePoint = -1; 
	
	@Override
	public boolean isVisible()
	{
		return bVisible;
	}
	
	@Override
	public void setTimePoint(final int nTP)
	{
		this.nTimePoint = nTP;
	}
	
	@Override
	public int getTimePoint()
	{
		return nTimePoint;
	}
}
