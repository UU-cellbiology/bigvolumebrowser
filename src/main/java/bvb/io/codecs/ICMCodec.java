package bvb.io.codecs;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.IndexColorModel;

import bvb.io.dto.IndexColorModelDTO;

public class ICMCodec implements ValueCodec<IndexColorModel> {

	@Override
	public String getTypeId()
	{
		 return "indexColorModel";
	}

	@Override
	public Class< IndexColorModel > getValueClass()
	{
		return IndexColorModel.class;
	}

	@Override
	public Object encode( IndexColorModel icm )
	{

        int size = icm.getMapSize();

        IndexColorModelDTO dto = new IndexColorModelDTO();

        dto.bits = icm.getPixelSize();
        dto.size = size;
        dto.hasAlpha = icm.hasAlpha();
        dto.transparentPixel = icm.getTransparentPixel();

        dto.r = new int[size];
        dto.g = new int[size];
        dto.b = new int[size];

        if (dto.hasAlpha)
            dto.a = new int[size];

        for (int i = 0; i < size; i++) {
            dto.r[i] = icm.getRed(i);
            dto.g[i] = icm.getGreen(i);
            dto.b[i] = icm.getBlue(i);

            if (dto.hasAlpha) {
                dto.a[i] = icm.getAlpha(i);
            }
        }

        return dto;
	}

	@SuppressWarnings( "null" )
	@Override
	public IndexColorModel decode( Object raw )
	{
        ObjectMapper mapper = new ObjectMapper();
      
        IndexColorModelDTO dto = mapper.convertValue(raw, IndexColorModelDTO.class);

        int size = dto.size;

        byte[] r = new byte[size];
        byte[] g = new byte[size];
        byte[] b = new byte[size];
        byte[] a = dto.hasAlpha ? new byte[size] : null;

        for (int i = 0; i < size; i++) {
            r[i] = (byte) dto.r[i];
            g[i] = (byte) dto.g[i];
            b[i] = (byte) dto.b[i];

            if (dto.hasAlpha) {
                a[i] = (byte) dto.a[i];
            }
        }

        IndexColorModel icm;

        if (dto.hasAlpha) {
            icm = new IndexColorModel(
                    dto.bits,
                    size,
                    r, g, b, a
            );
        } else {
            icm = new IndexColorModel(
                    dto.bits,
                    size,
                    r, g, b
            );
        }

        return icm;
    
	}


}
