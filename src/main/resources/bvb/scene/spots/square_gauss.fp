    vec2 fade = 2.0 * abs( 0.5  - abs(vTexCoord));
    colorout.a = fade.x * fade.y * colorout.a; 