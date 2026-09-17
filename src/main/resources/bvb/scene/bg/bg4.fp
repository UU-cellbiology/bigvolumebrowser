// taken from 
// https://www.shadertoy.com/view/4l2cW1
out vec4 fragColor;
in vec2 posW;

uniform float fTime;
uniform vec2 u_renderSize;

// BVV basis uniforms
uniform vec3 u_uu; // Camera Right
uniform vec3 u_vv; // Camera Up
uniform vec3 u_ww; // Camera Forward

void main()
{
    vec2 U = posW;
    vec4 O = vec4(0.0);

    // Convert screen UVs to centered coordinates matching iResolution aspect ratio
    vec2 r_xy = u_renderSize;
    vec2 uv = (U * u_renderSize - 0.5 * r_xy) / r_xy.y;

    // Construct Ray Direction using BVV camera basis (focal length = 1.0)
    
    vec3 d = normalize(uv.x * u_uu + uv.y * u_vv + 1.0 * u_ww);

    // Setup camera origin matching original scale (translating along world X)
    float iTime = fTime / 2000.0;
    vec3 p = vec3(iTime, 0.0, 3.0) * 9.0;
    vec3 r;

    float t = 0.2;

    // Raymarching loop
 
    for (int i = 0; i < 3000; i++) 
    {
       if (t <= 0.1) break;

        // Step along the BVV-controlled ray direction
        p -= t * d;
        r = ceil(p / 3.0);
        
        // Pseudo-random heightmap calculation
        t = fract(4e4 * sin(r.x + r.y * 17.0));
       	//shading
       	O += t;
        // Ground plane height step formula
        t = min(p.z - 8.0 * t * t, 0.2);
    }

    O = sqrt(O/1000);
    O.a = 1.0;

    fragColor = O;
    gl_FragDepth = 1.0;
}