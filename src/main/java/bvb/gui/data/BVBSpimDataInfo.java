/*-
 * #%L
 * browsing large volumetric data
 * %%
 * Copyright (C) 2025 Cell Biology, Neurobiology and Biophysics Department of Utrecht University.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
