out vec4 fragColor;

in vec3 posW;
in vec2 vTexCoord;
in float fDiamfp;
in float fPropertyfp;
in vec4 fColorsfp;
in vec4 vViewSpaceCenter;
in float scaledPointSize;

uniform vec4 colorin;
uniform int nHasColors;
uniform int renderType;
uniform int pointShape;
uniform int pointShade;

uniform mat4 pm;
uniform float fnratio;

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
		vec3 posclip = ( cliptransform * vec4(posW, 1.0) ).xyz;
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

		val = pow(clamp((val - mapMin) / mapRange, 0.0, 1.0), mapGamma);
		
		if(bInvLUT != 0)
		{
			val = 1.0 - val;
		}
		
		val = 0.5 + (sizeLUT - 1) * val;
		
		//2D texture with fixed width of 256
		vec2 q = vec2(0);
		q.y = floor(val / 256.0);
		q.x = (val / 256.0) - q.y;
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
		
		val = pow(clamp((val - alphaMin) / alphaRange, 0.0, 1.0), alphaGamma);
		
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

float linearizeDepth(float z)
{
	return z/(z - fnratio*z + fnratio);
}

void main()
{
	checkClipping();
	vec4 colorout = getInputColor();
	colorout.a = extraAlpha * getInputAlpha();
	gl_FragDepth = gl_FragCoord.z;
	// round point
	if(pointShape == 0)
	{
		float r2 = vTexCoord.x * vTexCoord.x + vTexCoord.y * vTexCoord.y;
    	if (r2 > 0.25) discard;
    	
    	switch (renderType)
		{
			case 0:
				//add shading + depth
				if(pointShade > 0)
				{
					float z = sqrt(1.0 - 4.0 * r2);
					vec3 n = - vec3(2.0 * vTexCoord.x,  2.0 * vTexCoord.y, z);
					float diff =  abs(dot(n, lightDir));
					colorout.rgb = colorout.rgb * (diff + ambient);
					
					//depth
					float zSphere = sqrt(0.25 - r2);
					vec4 pixelViewPos = vViewSpaceCenter;
					pixelViewPos.z -= zSphere * scaledPointSize;					
					vec4 clipPos = pm * pixelViewPos;
					gl_FragDepth = (clipPos.z / clipPos.w)* 0.5 + 0.5;		
				}
				break;
			//outline/border only
			case 1:
				if ( r2 < 0.16) discard;
				break;
			//gaussian
			case 2:
				colorout.a = exp(- 18. * r2) * colorout.a; //i.e. 18= (-1)/(2.0*(0.333/2)*(0.333/2)); 
				break;
		}
    }
    //square points shape
    else
    {
    	//draw only outline
		//i.e. discard inside
		if(renderType == 1)
		{
			float norm2 = step(0.4, abs(vTexCoord.x)) + step(0.4, abs(vTexCoord.y)); 
			if ( norm2 < 0.5) discard;
		}
		else
		{
			if(renderType >= 2)
			{
				vec2 fade = 2.0 * abs( 0.5  - abs(vTexCoord));
				colorout.a = fade.x * fade.y * colorout.a; 
			}
		}
    }
    
    //transparency rendering
    if(wOIT > 0)
	{
		float z = linearizeDepth(gl_FragCoord.z); // 0.0 to 1.0
		float baseAlpha = colorout.a;
		float weight = exp(- 3.0 * z );
		colorout.xyz = colorout.xyz * baseAlpha * weight;
		colorout.a = baseAlpha * weight;
	}
	
	fragColor = colorout;
    
}