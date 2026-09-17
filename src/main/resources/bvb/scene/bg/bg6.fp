out vec4 fragColor;
in vec2 posW;
uniform float fTime;


float julia(vec2 uv, float t)
{
    int j=0;
    vec2 c=vec2(-0.70176+t,-0.3842 +t);
    for(int i=0;i<300;i++)
    {
        j++;
        uv=vec2(uv.x*uv.x-uv.y*uv.y,2.0*uv.x*uv.y)+c;
        // Check for divergence
        if(length(uv)>float(2))
        {
            break;
        }
    }
    return float(j)/float(300);
}

void main()
{
	vec2 uv = vec2(posW);
    //center better
    uv.y = uv.y-0.33;

    float f = julia(uv, (sin(2.0*3.14*fTime/36000.)+1.0)*18.0/1000.0);

    fragColor=vec4(vec3(f),1.0);

    gl_FragDepth = 1.0;
}
