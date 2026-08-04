	vec3 posclip = ( cliptransform * vec4(posW, 1.0) ).xyz;
	vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
	if(s.x * s.y * s.z == clipactive - 1)
	{
		discard;
	}