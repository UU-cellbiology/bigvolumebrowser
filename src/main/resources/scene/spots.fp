out vec4 fragColor;

uniform vec4 colorin;
uniform vec2 ellipseAxes;
uniform int renderType;
uniform int pointShape;
in vec3 posW;
in float sDiamfp;
uniform int clipactive;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform mat4 cliptransform;
uniform int wOIT;

uniform sampler2D lutTexture;
uniform int nMapLUTMode;
uniform int sizeLUT;
uniform float mapMin;
uniform float mapRange;


void checkClipping()
{
    //ROI clipping
	if(clipactive>0)
	{
		vec3 posclip = ( cliptransform * vec4(posW,1.0) ).xyz;
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
		if(s.x * s.y * s.z == clipactive - 1)
		{
			discard;
		}
	}
}

vec4 getInputColor()
{

	if(nMapLUTMode > 0)
	{
		float val = 0.0;
		if(nMapLUTMode<4)
		{
			vec3 axis = vec3(0);
			axis[nMapLUTMode-1] = 1;
			val = dot(axis, posW);
			val = 0.5 + (sizeLUT-1)*((val-mapMin)/mapRange);
		}
		if(nMapLUTMode == 4)
		{
			val = sDiamfp;
			val = 0.5 + (sizeLUT-1)*(1.0-((val-mapMin)/mapRange));
			
		}

		//2D texture with fixed width of 256
		vec2 q = vec2(0);
		q.y = floor(val/256.0);
		q.x = (val/256.0)- q.y;
		q.y = (q.y+0.5)/ceil(sizeLUT/256.0);
		return texture(lutTexture, q);
		
	}
	else
	{
	 	return colorin;
	}
}

void main()
{
	checkClipping();
	
    //transform coordinates to NDC
	vec2 coord = 2.0 * gl_PointCoord - 1.0;
	
	vec4 colorout = getInputColor();
	
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
			colorout.a = exp(-4.5 * norm) * colorin.a; //i.e. 4.5= (-1)/(2.0*0.333*0.333);  
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
				vec2 fade = abs(( 1 /sqrt(ellipseAxes)) - abs(coord));
				colorout.a = fade.x * fade.y * colorin.a; 
			}
		}
		
	}
	
	if(wOIT>0)
	{
		colorout.a = colorout.a * exp( - gl_FragCoord.z * 0.8);
		colorout.xyz = colorout.xyz * colorout.a;

	}
    fragColor = colorout; 
    
}