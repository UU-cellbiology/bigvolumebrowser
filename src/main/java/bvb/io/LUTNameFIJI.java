package bvb.io;

import mpicbg.spim.data.generic.base.NamedEntity;

/**
 * Entity which contains the name of FIJI LUT
 */
public class LUTNameFIJI extends NamedEntity implements
Comparable<LUTNameFIJI>
{
	public String sLUTName = "";
	
	// if isset is false, the display value is discarded
	public boolean isSet = false;
	
	public LUTNameFIJI(final int id, final String name) {
		super(id, name);
	}
	
	public LUTNameFIJI(final int id) {
		this(id, Integer.toString(id));
	}
	
	@Override
	public String toString() 
	{
		String str = "";
		str += "set = " + this.isSet + ", ";
		str += " name = " + sLUTName;
		return str;
	}
	
	/**
	 * Compares the {@link #getId() ids}.
	 */
	@Override
	public int compareTo(final LUTNameFIJI o) {
		return getId() - o.getId();
	}
}
