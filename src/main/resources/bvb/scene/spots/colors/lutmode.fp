    float val = 0.0;
//$insert{shaderMapLUTMode}
    val = pow(clamp((val - mapMin) / mapRange, 0.0, 1.0), mapGamma);
//$insert{invertColorLUT}    
	val = 0.5 + (sizeLUT - 1) * val;
		
    //2D texture with fixed width of 256
    vec2 q = vec2(0);
    q.y = floor(val / 256.0);
    q.x = (val / 256.0) - q.y;
    q.y = (q.y + 0.5) / ceil(sizeLUT / 256.0);
    vec4 colorout = texture(lutTexture, q);