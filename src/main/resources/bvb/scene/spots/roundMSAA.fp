    float r2 = vTexCoord.x * vTexCoord.x + vTexCoord.y * vTexCoord.y;
    float alphaAA = 1.0 - smoothstep(0.24, 0.25, r2);
    if (alphaAA <= 0.0) discard;
    colorout.a = alphaAA * colorout.a;
//$insert{roundRenderType}