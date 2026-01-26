uniform vec4 color;

uniform int dashed;
uniform float antialias;
uniform float thickness;
uniform float linelength;
uniform float spacing;
in vec2 v_uv;
uniform int wOIT;

in vec3 posW;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform int clipactive;
out vec4 fragColor;

void main()
{
    //ROI clipping
	if(clipactive > 0)
	{
		vec3 s = step(clipmin, posW) - step(clipmax, posW);
		if(s.x * s.y * s.z == clipactive - 1)
		{
			discard;
		}
	}
	
	// drawing of antialiased 3D lines
	// taken and adapted from 
	// https://www.labri.fr/perso/nrougier/python-opengl/#d-lines
	// Python & OpenGL for Scientific Visualization
	// Copyright (c) 2018 - Nicolas P. Rougier <Nicolas.Rougier@inria.fr>
	
    float d = 0;
    float w = thickness/2.0 - antialias;

    vec4 colorOut = vec4(color);

	if(dashed == 0)
	{
	    // Cap at start
	    if (v_uv.x < 0)
	    {      
	       d = length(v_uv) - w;
	    }
	    // Cap at end
	    else if (v_uv.x >= linelength)
	    {
			d = length(v_uv - vec2(linelength,0)) - w;
	    }
	    // Body
	    else
	    {
	        d = abs(v_uv.y) - w;
	    }
	}
	else
	{
		//float center = round(mod(v_uv.x, spacing) / spacing);		
		//if(center < 0.1)
		//{
		//	discard;
		//}
		//d = length(v_uv)  - w;
		
		//with tapering at the end of dashes
		float center = mod(v_uv.x, spacing) / spacing;
		
		if(center > 0.5)
		{
			discard;
		}
		float val = min(0.25,thickness/spacing);
		center = abs(center-0.25) - 0.25 + val;
		
		if(center > 0.0)
		{
			center = center/val;
			vec2 endV = vec2(center*w, v_uv.y);
			d = length(endV) - w;
		}
		else
		{
			d = abs(v_uv.y)  - w;
		}


	}
    vec4 colorout; 
    if( d < 0) 
    {
       colorout = vec4(color);     
    } 
    else 
    {
        d /= antialias;
        colorout = vec4(color.xyz, color.a*exp(-d*d));
    }
    
    if(wOIT>0)
	{
		//no depth effect
		//colorout.a = colorout.a * exp( - gl_FragCoord.z * 0.9);
		colorout.xyz = colorout.xyz * colorout.a;
	}
	fragColor = colorout; 
}
