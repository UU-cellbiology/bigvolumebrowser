out vec4 fragColor;

in vec2 texCoord;
in vec3 posW;

uniform sampler2D texture1;

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
    fragColor = texture( texture1, texCoord );
}
