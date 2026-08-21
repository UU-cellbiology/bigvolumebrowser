    float innerAlpha = smoothstep(0.15, 0.17, r2);
    if (innerAlpha <= 0.0) discard;
    colorout.a = innerAlpha * colorout.a;