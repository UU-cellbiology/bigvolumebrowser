out vec4 fragColor;

uniform vec4 colorin;
uniform vec2 ellipseAxes;
uniform int renderType;
uniform int pointShape;
in vec3 posW;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform int clipactive;

void main()
{
    //ROI clipping
	if(clipactive>0)
	{
		vec3 s = step(clipmin, posW) - step(clipmax, posW);
		if(s.x * s.y * s.z == 0.0)
		{
			discard;
		}
	}
	
    //transform coordinates to NDC
	vec2 coord = 2.0 * gl_PointCoord - 1.0;
	
	vec4 colorout = colorin;
	
	gl_FragDepth = gl_FragCoord.z;
	
	if(pointShape == 0)
	{
	
		//ellipse taking into account stretched render window	
		float norm = (coord.x*coord.x*ellipseAxes.x)+(coord.y*coord.y*ellipseAxes.y);		
		
		//cut off everything outside the ellipse
		if ( norm > 1) discard;
		
		//draw only outline,
		//i.e. discard inside
		if(renderType == 1)
		{
			if ( norm < 0.6) 
				discard;
		}
		else if(renderType==2)
		{
			colorout.a = colorin.a * exp( ((-1)*norm*norm)/(2.0*(0.3*0.3)) ); 
			gl_FragDepth = 1.0;
		}
	}
	else
	{
		//rectangle 
		float norm = step(1/sqrt(ellipseAxes.x),abs(coord.x)) + step(1/sqrt(ellipseAxes.y),abs(coord.y)); 
		
		//cut off everything outside the rectangle
		if ( norm > 0.5) discard;
		
		//draw only outline
		//i.e. discard inside
		if(renderType == 1)
		{
			float norm2 = step(0.8/sqrt(ellipseAxes.x),abs(coord.x)) + step(0.8/sqrt(ellipseAxes.y),abs(coord.y)); 
			if ( norm2 < 0.5) discard;
		}	
		else
		{
			if(renderType == 2)
			{
				vec2 fade = abs((1/sqrt(ellipseAxes))-abs(coord));
				//colorout.a = colorin.a * exp( ((-4.0)*norm*norm)/(2.0*(0.1*0.1)) ); 
				colorout.a = colorin.a * fade.x * fade.y; 
				gl_FragDepth = 1.0;
			}
		}
		
	}

    fragColor = colorout; 
    
}