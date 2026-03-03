package bvb.io.codecs;

import java.awt.Color;

public class ColorCodec implements ValueCodec<Color> {

    @Override
    public String getTypeId() {
        return "color";
    }

    @Override
    public Class<Color> getValueClass() {
        return Color.class;
    }

    @Override
    public Object encode(Color value) {
        return String.format(
                "#%02X%02X%02X%02X",
                value.getRed(),
                value.getGreen(),
                value.getBlue(),
                value.getAlpha()
        );
    }
    
    @Override
    public Color decode(Object raw) {
        String hex = (String) raw;

        if (!hex.startsWith("#") || hex.length() != 9) {
            throw new IllegalArgumentException(
                    "Invalid color format: " + hex);
        }

        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        int a = Integer.parseInt(hex.substring(7, 9), 16);

        return new Color(r, g, b, a);
    }

}
