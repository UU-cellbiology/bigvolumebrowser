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

//$insert{preClip}

//$insert{preOIT}


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

void main()
{
//$insert{mClip}
    vec4 colorout = getInputColor();
    colorout.a = extraAlpha * getInputAlpha();
//$insert{spotsShape}	
//$insert{wOIT}
    fragColor = colorout;
    
}