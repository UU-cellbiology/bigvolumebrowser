out vec4 fragColor;

in vec2 texCoord;

uniform sampler2D texture1;

void main()
{
	//fragColor = vec4(1.0,1.0,1.0,1.0);
    fragColor = texture( texture1, texCoord );
}
