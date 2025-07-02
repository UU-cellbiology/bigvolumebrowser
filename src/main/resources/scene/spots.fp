out vec4 fragColor;

uniform vec4 colorin;
uniform vec2 ellipseAxes;
uniform int renderType;
uniform int pointShape;
in vec3 posW;
in float sRadfp;
uniform float normGauss;
uniform int clipactive;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform mat4 cliptransform;

void checkClipping()
{
    //ROI clipping
	if(clipactive>0)
	{
		vec3 posclip = ( cliptransform * vec4(posW,1.0) ).xyz;
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
		if(s.x * s.y * s.z == 0.0)
		{
			discard;
		}
	}
}

void main()
{
	checkClipping();
	
    //transform coordinates to NDC
	vec2 coord = 2.0 * gl_PointCoord - 1.0;
	
	vec4 colorout = colorin;
	
	//gl_FragDepth = gl_FragCoord.z;
	
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
		else if(renderType >= 2)
		{
			
			float sd = -9.0/2.0; //i.e.  (-1)/(2.0*0.333*0.333);  
			float dCol = 1.0;
			if(renderType == 3)
			{
				//dCol = clamp(normGauss/(pointSizeReal*pointSizeReal*pointSizeReal),0.1,1.0);
				dCol = normGauss/(sRadfp*sRadfp*sRadfp);
			}
			colorout.a = dCol *  exp(sd*norm) * colorin.a; 
			
			//gl_FragDepth = 1.0;
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
			if(renderType >= 2)
			{
				vec2 fade = abs((1/sqrt(ellipseAxes))-abs(coord));
				float dCol = 1.0;
				if(renderType == 3)
				{
					dCol = normGauss/(sRadfp*sRadfp*sRadfp);
					//dCol = clamp(normGauss/(pointSizeReal*pointSizeReal*pointSizeReal),0.1,1.0);
				}
				colorout.a = dCol *  fade.x * fade.y * colorin.a; 
				//gl_FragDepth = 1.0;
			}
		}
		
	}

    fragColor = colorout; 
    
}