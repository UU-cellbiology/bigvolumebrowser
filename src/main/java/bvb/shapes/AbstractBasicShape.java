package bvb.shapes;


public abstract class AbstractBasicShape implements BasicShape
{
	boolean bVisible = true;
	
	boolean bTransparent = false;
	
	int nTimePoint = -1; 

	String sName = "";
	
	@Override
	public boolean isVisible()
	{
		return bVisible;
	}
	
	@Override
	public boolean isTransparent()
	{
		return bTransparent;
	}
	
	@Override
	public void setVisible( boolean bVisible_ )
	{
		bVisible = bVisible_;
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
	
	@Override
	public void setName(String sName_)
	{
		sName = sName_;
	}
	

}
