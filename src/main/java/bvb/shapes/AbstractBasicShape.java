package bvb.shapes;

public abstract class AbstractBasicShape implements BasicShape
{
	boolean bVisible = true;
	
	@Override
	public boolean isVisible()
	{
		return bVisible;
	}

}
