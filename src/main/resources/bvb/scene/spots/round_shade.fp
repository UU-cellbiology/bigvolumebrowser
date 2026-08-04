    float pz = sqrt(1.0 - 4.0 * r2);
    vec3 n = - vec3(2.0 * vTexCoord.x,  2.0 * vTexCoord.y, pz);
    float diff =  abs(dot(n, lightDir));
    colorout.rgb = colorout.rgb * (diff + ambient);