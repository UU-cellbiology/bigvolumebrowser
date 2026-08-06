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

//$insert{preAlphaMap}

uniform float extraAlpha;
const vec3 lightDir = normalize(vec3(0, -0.2, -1));
const vec3 ambient = vec3(0.1, 0.1, 0.1);

//$insert{preClip}

//$insert{preOIT}

void main()
{
//--- clipping, if active  ------
//$insert{mClip}
//--- color of the spots ------
//$insert{spotsColor}
//--- alpha channel of the spots ------
    float fAlpha = colorout.a;
//$insert{spotsAlpha}

    colorout.a = extraAlpha * fAlpha;
//--- spot shape and render type ------
//$insert{spotsShape}	
//--- transparency mode, if active ------
//$insert{wOIT}
    fragColor = colorout;
    
}