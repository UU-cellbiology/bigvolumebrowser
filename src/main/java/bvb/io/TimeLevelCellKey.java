package bvb.io;

import java.util.Objects;

public class TimeLevelCellKey
{
	private final int timepoint;
	private final int level;
	private final long cellind;
	
	public TimeLevelCellKey (final int timepoint, final int level, final long cellind)
	{
		this.timepoint = timepoint;
		this.level = level;
		this.cellind = cellind;
	}
	
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof TimeLevelCellKey)) {
            return false;
        } else {
        	TimeLevelCellKey other = (TimeLevelCellKey) obj;
            if(other.level == this.level && other.timepoint == this.timepoint 
            		&& other.cellind == this.cellind)
            {
            	return true;
            }
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(timepoint, level, cellind);
    }
    
    public String getTimePointLevel()
    {
    	return Integer.toString(timepoint) + Integer.toString( level);
    }
    
    public Long getCellKey()
    {
    	return cellind;
    }
}
