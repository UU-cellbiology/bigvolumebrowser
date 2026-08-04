out vec4 fragColor;

in vec3 posW;
in vec2 vTexCoord;
in float fDiamfp;
in float fPropertyfp;
in vec4 fColorsfp;
in vec4 vViewSpaceCenter;
in float scaledPointSize;

uniform vec4 colorin;

uniform mat4 pm;

//$insert{preColorLUT}

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
		
		if(nMapAlphaMode == 5)
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
//--- clipping, if active  ------
//$insert{mClip}
//--- color of the spot ------
//$insert{spotsColor}

    colorout.a = extraAlpha * getInputAlpha();
//--- spot shape and render type ------
//$insert{spotsShape}	
//--- transparency mode, if active ------
//$insert{wOIT}
    fragColor = colorout;
    
}