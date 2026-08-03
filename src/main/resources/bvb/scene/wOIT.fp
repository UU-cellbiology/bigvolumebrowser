    //transparency rendering
    float z = linearizeDepth(gl_FragCoord.z); // 0.0 to 1.0
    float baseAlpha = colorout.a;
    float weight = exp(- depthDecay * z );
    colorout.xyz = colorout.xyz * baseAlpha * weight;
    colorout.a = baseAlpha * weight;