    float r2 = vTexCoord.x * vTexCoord.x + vTexCoord.y * vTexCoord.y;
    if (r2 > 0.25) discard;

//$insert{roundRenderType}