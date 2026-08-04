    float norm2 = step(0.4, abs(vTexCoord.x)) + step(0.4, abs(vTexCoord.y)); 
    if ( norm2 < 0.5) discard;