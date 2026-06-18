out vec4 fragColor;

in vec2 uv;

uniform sampler2D colorTex;
uniform sampler2D depthTex;
uniform vec2 texel;
uniform float fnratio;
uniform int radius;
uniform float strength;

float linearizeDepth(float z)
{
	return z/(z - fnratio*z + fnratio);
}

void main()
{
	vec3 color = texture(colorTex, uv).rgb;
     
    vec2 offsets[8] = vec2[](
        vec2( radius, 0), vec2(-radius, 0),
        vec2( 0, radius), vec2( 0,-radius),
        vec2( radius, radius), vec2(-radius, radius),
        vec2( radius,-radius), vec2(-radius,-radius)
    );
    
    float centerDepth = linearizeDepth(texture(depthTex, uv).r);
    float diff = 0.0;
    for (int i = 0; i < 8; i++)
    {
        float d = linearizeDepth(
            texture(depthTex, uv + offsets[i] * texel).r);
        diff += max(0.0, centerDepth - d);
    }
    
    float shade = exp(-1.* strength * diff);
    
    fragColor =  vec4(color * shade, 1.0);
    //fragColor =  vec4(centerDepth,centerDepth,centerDepth, 1.0);  
    
    gl_FragDepth = texture( depthTex, uv ).r;
}
