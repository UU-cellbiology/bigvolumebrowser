package bvb.gui.data;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import bvvpg.vistools.BvvStackSource;


public class BVBSpimDataInfo
{
	public final ImageIcon icon;
	
	public final String sourceDescription;
	
	public final ArrayList<BVVSourceSettings> sourceSettings = new ArrayList<>();
	
	
	public BVBSpimDataInfo(final String sourceDescription, final ImageIcon icon)
	{
		this.icon = icon;
		this.sourceDescription = sourceDescription;
		
	}
	
	public void storeSourceSettings(List<BvvStackSource<?>> srcList)
	{
		sourceSettings.clear();
		for(BvvStackSource<?> src:srcList)
		{
			sourceSettings.add( new BVVSourceSettings(src) );
		}
	}
	
	public void applySourceSettings(List<BvvStackSource<?>> srcList)
	{
		if(sourceSettings.size() != srcList.size())
			return;

		for (int i = 0; i< sourceSettings.size(); i++)
		{
			sourceSettings.get( i ).applyStoredSettings( srcList.get( i ) );
		}
	}
	
}
