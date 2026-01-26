out vec4 fragColor;
in vec2 posW;
uniform float fTime;

//random noise

float rand(vec2 n) 
{ 
	return fract(sin(dot(n, vec2(12.9898, 4.1414*fTime))) * (43758.5453+fTime));
}


void main()
{
	
	float fRand = rand(posW);
 	fragColor = vec4(fRand,fRand,fRand,1.0);
    gl_FragDepth = 1.0;
}
