out vec4 fragColor;
in vec2 posW;
uniform float fTime;

// taken from 
// https://www.shadertoy.com/view/4l2cW1

void main()
{
	vec2 U  = posW;
	vec4 O = vec4(0.0,0.0,0.0,0.0);
	
	
    vec3 p = vec3( fTime/1000, 3, 0 ) *9.;
    vec3 r = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3( ( U - .5*r.xy ) / r.y, 1 );
    float t = .2;
    for( d.yz *= mat2(4,-3,3,4)*t ; t>.1; t = min( p.y - 8.*t*t , .2 ) )
        p += t*d, r = ceil(p/3.),
        O += t = fract( 4e4* sin(r.x+r.z*17.) );
    O/=2e2;  
      
	fragColor = O;
	
    gl_FragDepth = 1.0;
}
