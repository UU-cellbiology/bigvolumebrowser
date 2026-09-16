out vec4 fragColor;
in vec2 posW;
uniform float fTime;
uniform vec2 u_renderSize;
uniform vec3 u_uu; // Right vector (X)
uniform vec3 u_vv; // Up vector (Y)
uniform vec3 u_ww; // Forward/Look vector (Z)

// modified from 
// https://www.shadertoy.com/view/MllcD7


// Signed distance function for a box/cube
float sdBox(vec3 p, vec3 b)
{
    vec3 q = abs(p) - b;
    return length(max(q, 0.0)) + min(max(q.x, max(q.y, q.z)), 0.0);
}

float dist(vec3 p)
{
    // Field of repeating coordinates (20-unit grid interval)
    p = mod(p, 40.0) - 20.0;
    
    //sphere 
    return length(p)-10.;
    // Render cubes with half-extent of 4.0 units (cube width/height/depth = 8.0)
    //return sdBox(p, vec3(4.0));
}

void main()
{
    vec2 uv = posW;
    float iTime = fTime/1000;
    fragColor = vec4(uv, 0.5+0.5*sin(iTime),1.0);

    vec2 t = uv - .5;
    t.x *= u_renderSize.x/u_renderSize.y;

    vec2 p = t.xy;
    
	// Scene moves continuously along World Z
    vec3 ta = vec3(0.0, 0.0, -iTime * 4.0);

    //  Camera position sits behind target along look vector (u_ww)
    vec3 ro = ta - u_ww * 20.0;

    vec3 rd = normalize(p.x * u_uu + p.y * u_vv +  u_ww);


    
    vec3 co = ro;
    vec3 ord = rd;

    vec3 n;
    vec3 oro=ro;


     // Prepare for uniform grid traversal.
     oro=ro;
     vec3 c=floor(ro);
     vec3 ts=(c+max(vec3(0.),sign(rd))-ro)/rd;

     // Perform uniform grid traversal.

     // Use (many) more iterations to reach the same distances as would be reached with space skipping.
     for(int i=0;i<1600;++i)
     {
         // n holds 1.0 in the axis for which ts's component in that axis is
         // the smallest, and 0.0 elsewhere. This indicates the axis plane with the closest intersection.
         n=step(ts,ts.yzx)*step(ts,ts.zxy);
         c+=sign(rd)*n;
         float d=dist(c);
         if(d<-1.7)
         {
	      // Advance the ray march position to the next grid cell.
             ro=oro+rd*min(ts.x,min(ts.y,ts.z));
             break;
         }
         // Increment the axis plane inersections.
         vec3 dd=sign(rd)/rd*n;
         ts+=dd;
     }
        // Advance the ray march position to the next grid cell.
        ro=oro+rd*min(ts.x,min(ts.y,ts.z));
   
    // Checkerboard texture.
    ro+=1e-3;
    float tex=.8+.2*step(.5,fract(ro.x+.5*step(.5,fract(ro.y+.5*step(.5,fract(ro.z))))));

    // Basic lighting.
    vec3 ld=normalize(vec3(3,4,1));
    fragColor.rgb=tex*vec3(.5+.5*dot(n*-sign(rd),ld));

    // Distance fading.
    float fogstrength = .005;
    float fogamount=exp(-distance(ro,co)*fogstrength);
    fragColor.rgb=mix(vec3(0),fragColor.rgb,fogamount);
    
    // Gamma.
    fragColor.rgb=sqrt(fragColor.rgb);
}
