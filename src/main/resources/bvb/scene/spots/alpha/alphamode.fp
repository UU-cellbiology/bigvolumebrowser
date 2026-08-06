//$insert{alphaMapMode}
    fAlpha = pow(clamp((fAlpha - alphaMin) / alphaRange, 0.0, 1.0), alphaGamma);
//$insert{invertAlphaMap}  		
    fAlpha =  fAlpha * colorin.a;