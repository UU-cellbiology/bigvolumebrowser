out vec4 fragColor;

in vec2 uv;

uniform sampler2D colorTex;
uniform sampler2D depthTex;
uniform vec2 screenSize;
uniform float near;
uniform float far;
uniform float xf;


uniform mat4 invPV;

float getViewDepth(vec2 uvi)
{
    float z = texture(depthTex, uvi).r;

    // NDC reconstruction
    vec4 ndc;
    ndc.xy = uvi * 2.0 - 1.0;
    ndc.z  = z * 2.0 - 1.0;
    ndc.w  = 1.0;

    vec4 view = invPV * ndc;
    view /= view.w;
    
    return -view.z;
}

float tw( float zd )
{
	return ( xf * zd ) / ( 2 * xf * zd - xf - zd + 1 );
}
float linearizeDepth(float z)
{
    float z_ndc = z * 2.0 - 1.0;

    return (2.0 * near * far) /
           (far + near - z_ndc * (far - near));
}

void main()
{
	vec3 color = texture(colorTex, uv).rgb;
     
 	vec2 texel = 1.0 / screenSize;
	int radius = 4;
    vec2 offsets[8] = vec2[](
        vec2( radius, 0), vec2(-radius, 0),
        vec2( 0, radius), vec2( 0,-radius),
        vec2( radius, radius), vec2(-radius, radius),
        vec2( radius,-radius), vec2(-radius,-radius)
    );
    
    float centerDepth = tw(texture(depthTex, uv).r);
    float diff = 0.0;
    for (int i = 0; i < 8; i++)
    {
        float d = tw(
            texture(depthTex, uv + offsets[i] * texel).r);
        diff += max(0.0, centerDepth - d);
    }
    
    float shade = exp(-3. * diff);
    
    fragColor =  vec4(color * shade, 1.0);
    //fragColor =  vec4(centerDepth,centerDepth,centerDepth, 1.0);
    //fragColor =  vec4(color*centerDepth*1000, 1.0);
    //fragColor =  vec4(color, 1.0);
    
    
    gl_FragDepth = texture( depthTex, uv ).r;
}
