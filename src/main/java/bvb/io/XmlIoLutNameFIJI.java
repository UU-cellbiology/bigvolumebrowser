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
package bvb.io;

import org.jdom2.Element;

import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlHelpers;
import mpicbg.spim.data.generic.base.ViewSetupAttributeIo;
import mpicbg.spim.data.generic.base.XmlIoEntity;


@ViewSetupAttributeIo(name = "lutnamefiji", type = LUTNameFIJI.class)
public class XmlIoLutNameFIJI extends XmlIoEntity<LUTNameFIJI> 
{
	public static final String LUTNAMEFIJI_XML_TAG = "LUTNameFIJI";
	public static final String NAMELUTFIJI_XML_TAG = "FIJI_LUT_Name";
	
	public XmlIoLutNameFIJI() 
	{
		super(LUTNAMEFIJI_XML_TAG, LUTNameFIJI.class);
	}
	
	@Override
	public Element toXml(final LUTNameFIJI lutSet) 
	{
		final Element elem = super.toXml(lutSet);
		elem.addContent(XmlHelpers.textElement(NAMELUTFIJI_XML_TAG,
			lutSet.sLUTName));
		return elem;
	}
	@Override
	public LUTNameFIJI fromXml(final Element elem) throws SpimDataException {
		final LUTNameFIJI ds = super.fromXml(elem);

		ds.sLUTName = XmlHelpers.getText(elem, NAMELUTFIJI_XML_TAG);
		return ds;
	}
}
