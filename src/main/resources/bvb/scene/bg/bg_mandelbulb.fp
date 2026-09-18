
// taken from 
// https://www.shadertoy.com/view/ltfSWn
out vec4 fragColor;
in vec2 posW;
uniform float fTime;

uniform vec2 u_renderSize;

// BVV basis uniforms
uniform vec3 u_uu; // Camera Right
uniform vec3 u_vv; // Camera Up
uniform vec3 u_ww; // Camera Forward
uniform float scale;


mat3 rotate(float phi) {
	float c = cos(phi);
    float s = sin(phi);
    return mat3(
        c,   0.0,  -s,   // Column 0
        0.0, 1.0,   0.0, // Column 1
        s,   0.0,   c    // Column 2
    );
}

// https://iquilezles.org/articles/intersectors
vec2 isphere( in vec4 sph, in vec3 ro, in vec3 rd )
{
    vec3 oc = ro - sph.xyz;
	float b = dot(oc,rd);
	float c = dot(oc,oc) - sph.w*sph.w;
    float h = b*b - c;
    if( h<0.0 ) return vec2(-1.0);
    h = sqrt( h );
    return -b + vec2(-h,h);
}

// https://iquilezles.org/articles/mandelbulb
float map( in vec3 p, out vec4 resColor )
{
    vec3 w = p;
    float m = dot(w,w);

    vec4 trap = vec4(abs(w),m);
	float dz = 1.0;
    
	for( int i=0; i<4; i++ )
    {
        // trigonometric version (MUCH faster than polynomial)
        
        // dz = 8*z^7*dz
		dz = 8.0*pow(m,3.5)*dz + 1.0;
      
        // z = z^8+c
        float r = length(w);
        float b = 8.0*acos( w.y/r);
        float a = 8.0*atan( w.x, w.z );
        w = p + pow(r,8.0) * vec3( sin(b)*sin(a), cos(b), sin(b)*cos(a) );       
        
        trap = min( trap, vec4(abs(w),m) );

        m = dot(w,w);
		if( m > 256.0 )
            break;
    }

    resColor = vec4(m,trap.yzw);

    // distance estimation (through the Hubbard-Douady potential)
    return 0.25*log(m)*sqrt(m)/dz;
}

// https://iquilezles.org/articles/normalsSDF
vec3 calcNormal( in vec3 pos, in float t, in float px )
{
    vec4 tmp;
    vec2 e = vec2(1.0,-1.0)*0.5773*0.25*px;
    return normalize( e.xyy*map( pos + e.xyy,tmp ) + 
					  e.yyx*map( pos + e.yyx,tmp ) + 
					  e.yxy*map( pos + e.yxy,tmp ) + 
					  e.xxx*map( pos + e.xxx,tmp ) );
}

// https://iquilezles.org/articles/rmshadows
float softshadow( in vec3 ro, in vec3 rd, in float k )
{
    // bounding sphere
    float tmax = isphere( vec4(0.0,0.0,0.0,1.25), ro, rd ).y;
    
    // raymarch fractal
    float res = 1.0;
    float t = 0.0;
    for( int i=0; i<64 && t<tmax; i++ )
    {
        vec4 kk;
        float h = map(ro + rd*t, kk);
        res = min( res, k*h/t );
        if( res<0.001 ) break;
        t += clamp( h, 0.01, 0.2 );
    }
    return clamp( res, 0.0, 1.0 );
}

float raycast( in vec3 ro, in vec3 rd, out vec4 rescol, in float px )
{
    float res = -1.0;

    // bounding sphere
    vec2 dis = isphere( vec4(0.0,0.0,0.0,1.25), ro, rd );
    if( dis.y<0.0 ) return -1.0;
    dis.x = max( dis.x, 0.0 );
    dis.y = min( dis.y, 10.0 );

    // raymarch fractal distance field
	vec4 trap;

	float t = dis.x;
	for( int i=0; i<128; i++  )
    { 
        vec3 pos = ro + rd*t;
        float th = 0.25*px*t;
		float h = map( pos, trap );
		if( t>dis.y || h<th ) break;
        t += h;
    }
    
    if( t<dis.y )
    {
        rescol = trap;
        res = t;
    }

    return res;
}

const vec3 light1 = vec3(  0.577, 0.577, -0.577 );
const vec3 light2 = vec3( -0.707, 0.000,  0.707 );

vec3 refVector( in vec3 v, in vec3 n )
{
    return v;
    float k = dot(v,n);
    //return (k>0.0) ? v : -v;
    return (k>0.0) ? v : v-2.0*n*k;
}

vec3 render( in vec2 p, in mat4 cam )
{
	// ray setup
    const float fle = 1.0;

 	vec2 sp = (2.0 * p - u_renderSize) / u_renderSize.y;
    float px = 2.0 / (u_renderSize.y * fle);

    vec3  ro = vec3( cam[0].w, cam[1].w, cam[2].w );
	vec3  rd = normalize( (cam*vec4(sp,fle,0.0)).xyz );
    
    // intersect fractal
	vec4 tra;
    float t = raycast( ro, rd, tra, px );
    
	vec3 col;

    // color sky
    if( t<0.0 )
    {
     	col  = vec3(0.8,.9,1.1)*(0.6+0.4*rd.y);
		col += 5.0*vec3(0.8,0.7,0.5)*pow( clamp(dot(rd,light1),0.0,1.0), 32.0 );
	}
    // color fractal
	else
	{
        // color
        col = vec3(0.01);
		col = mix( col, vec3(0.10,0.20,0.30), clamp(tra.y,0.0,1.0) );
	 	col = mix( col, vec3(0.02,0.10,0.30), clamp(tra.z*tra.z,0.0,1.0) );
        col = mix( col, vec3(0.30,0.10,0.02), clamp(pow(tra.w,6.0),0.0,1.0) );
        col *= 0.5;
        
        // lighting terms
        vec3  pos = ro + t*rd;
        vec3  nor = calcNormal( pos, t, px );
        
        nor = refVector(nor,-rd);
        
        vec3  hal = normalize( light1-rd);
        vec3  ref = reflect( rd, nor );
        float occ = clamp(0.05*log(tra.x),0.0,1.0);
        float fac = clamp(1.0+dot(rd,nor),0.0,1.0);

        // sun
        float sha1 = softshadow( pos+0.001*nor, light1, 32.0 );
        float dif1 = clamp( dot( light1, nor ), 0.0, 1.0 )*sha1;
        float spe1 = pow( clamp(dot(nor,hal),0.0,1.0), 32.0 )*dif1*(0.04+0.96*pow(clamp(1.0-dot(hal,light1),0.0,1.0),5.0));
        // bounce
        float dif2 = clamp( 0.5 + 0.5*dot( light2, nor ), 0.0, 1.0 )*occ;
        // sky
        float dif3 = (0.7+0.3*nor.y)*(0.2+0.8*occ);
        
		vec3 lin = vec3(0.0); 
		     lin += 12.0*vec3(1.50,1.10,0.70)*dif1;
		     lin +=  4.0*vec3(0.25,0.20,0.15)*dif2;
        	 lin +=  1.5*vec3(0.10,0.20,0.30)*dif3;
             lin +=  2.5*vec3(0.35,0.30,0.25)*(0.05+0.95*occ);
        	 lin +=  4.0*fac*occ;
		col *= lin;
		col = pow( col, vec3(0.7,0.9,1.0) );
        col += spe1*15.0;
    }

    // gamma
	col = pow( col, vec3(0.4545) );
    
    // vignette
    col *= 1.0 - 0.05*length(sp);

    return col;
}


void main()
{
	float time = fTime * 0.0001;
    float dist = -3.5;

    // 1. Calculate base camera position
    vec3 base_ro = u_ww * scale * dist;

    // 2. Rotate the POSITION around the world origin (0,0,0)
    mat3 rotX = rotate(time);
    vec3 ro = rotX * base_ro;

    // 3. Rotate the DIRECTION vectors directly (NO translation/center shift)
    vec3 cu = rotX * u_uu;
    vec3 cv = rotX * u_vv;
    vec3 cw = rotX * u_ww;

    // Explicit vec4 columns guarantee exact mat4 indexing
    // Column 0: Right vector (x,y,z) + ro.x in w
    // Column 1: Up vector (x,y,z) + ro.y in w
    // Column 2: Forward vector (x,y,z) + ro.z in w
    // Column 3: Homogeneous coordinate row
    mat4 cam = mat4(
        vec4(cu, ro.x),
        vec4(cv, ro.y),
        vec4(cw, ro.z),
        vec4(0.0, 0.0, 0.0, 1.0)
    );

    vec2 p = posW * u_renderSize;
    // render

	vec3 col = render(  p, cam );

	//const vec3 W = vec3(0.2126, 0.7152, 0.0722);
	
	// grayscale 
	//float gray = dot(col.rgb, W);
	
	// Output back as a vec3
	//col.rgb = vec3(gray);

	fragColor = vec4( col, 1.0 );	

    gl_FragDepth = 1.0;
}
