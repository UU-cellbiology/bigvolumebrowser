out vec4 fragColor;

uniform vec4 colorin;
uniform int nHasColors;
uniform vec2 ellipseAxes;
uniform int renderType;
uniform int pointShape;
uniform int pointShade;
in vec3 posW;
in float fDiamfp;
in float fPropertyfp;
in vec4 fColorsfp;
uniform int clipactive;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform mat4 cliptransform;
uniform int wOIT;

uniform sampler2D lutTexture;
uniform int nMapLUTMode;
uniform int sizeLUT;
uniform int bInvLUT;
uniform float mapMin;
uniform float mapRange;
uniform float mapGamma;


uniform int nMapAlphaMode;
uniform int bInvAlpha;
uniform float alphaMin;
uniform float alphaRange;
uniform float alphaGamma;
uniform float extraAlpha;
const vec3 lightDir = normalize(vec3(0, -0.2, -1));
const vec3 ambient = vec3(0.1, 0.1, 0.1);

void checkClipping()
{
    //ROI clipping
	if(clipactive > 0)
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
		
		if(nMapLUTMode < 4)
		{
			vec3 axis = vec3(0);
			axis[nMapLUTMode - 1] = 1;
			val = dot(axis, posW);
		}

		if(nMapLUTMode == 4)
		{
			val = fDiamfp;			
		}
		
		if(nMapLUTMode == 5)
		{
			val = fPropertyfp;			
		}

		val = pow(clamp((val-mapMin)/mapRange,0.0,1.0), mapGamma);
		
		if(bInvLUT != 0)
		{
			val = 1.0 - val;
		}
		
		val = 0.5 + (sizeLUT-1)*val;
		
		//2D texture with fixed width of 256
		vec2 q = vec2(0);
		q.y = floor(val / 256.0);
		q.x = (val / 256.0)- q.y;
		q.y = (q.y + 0.5) / ceil(sizeLUT / 256.0);
		return texture(lutTexture, q);
		
	}
	else
	{
		if(nHasColors > 0)
		{
			return fColorsfp;
		}
		else
		{
	 		return colorin;
	 	}
	}
}

float getInputAlpha()
{

	if(nMapAlphaMode > 0)
	{
		float val = 0.0;
		if(nMapAlphaMode < 4)
		{
			vec3 axis = vec3(0);
			axis[nMapAlphaMode - 1] = 1;
			val = dot(axis, posW);
		}

		if(nMapAlphaMode == 4)
		{
			val = fDiamfp;			
		}
		
		if(nMapLUTMode == 5)
		{
			val = fPropertyfp;			
		}
		
		val = pow(clamp((val-alphaMin)/alphaRange,0.0,1.0), alphaGamma);
		
		if(bInvAlpha != 0)
		{
			val = 1.0 - val;
		}
		
		return val * colorin.a;
		
	}
	else
	{
	 	return colorin.a;
	}
}

void main()
{
	checkClipping();
	
    //transform coordinates to NDC
	vec2 coord = 2.0 * gl_PointCoord - 1.0;
	
	vec4 colorout = getInputColor();
	colorout.a = extraAlpha * getInputAlpha();
	
	if(pointShape == 0)
	{
	
		//ellipse taking into account stretched render window	
		float norm = (coord.x*coord.x*ellipseAxes.x)+(coord.y*coord.y*ellipseAxes.y);		
		
		//cut off everything outside the ellipse
		if ( norm > 1) discard;
		
		switch (renderType)
		{
			case 0:
			if(pointShade>0)
			{
				float z = sqrt(1 - norm);
				vec3 n = - vec3(coord.x * sqrt( ellipseAxes.x), coord.y * sqrt( ellipseAxes.y), z);
				float diff =  abs(dot(n, lightDir));
				colorout.rgb = colorout.rgb * (diff + ambient);
			}
				break;
			case 1:
				//draw only outline,
				//i.e. discard inside
				if ( norm < 0.6) 
					discard;
				break;
			case 2:	
					colorout.a = exp(-4.5 * norm) * colorout.a; //i.e. 4.5= (-1)/(2.0*0.333*0.333);  
				break;
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
				colorout.a = fade.x * fade.y * colorout.a; 
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