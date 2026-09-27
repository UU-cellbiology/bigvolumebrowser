    //depth always
    float zSphere = sqrt(0.25 - r2);
    vec4 pixelViewPos = vViewSpaceCenter;
    pixelViewPos.z -= zSphere * scaledPointSize;					
    vec4 clipPos = pm * pixelViewPos;
    gl_FragDepth = (clipPos.z / clipPos.w)* 0.5 + 0.5;	

//$insert{spotsRoundShade}