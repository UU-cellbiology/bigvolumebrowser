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
