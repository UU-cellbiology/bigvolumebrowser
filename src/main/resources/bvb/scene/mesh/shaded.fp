	vec3 norm = normalize(Normal);
	vec3 viewDir = normalize(-FragPos);
	vec3 diff = diffuse(norm,  lightDir1, lightColor1);
	colorout = vec4((ambient + diff ) * colorin.rgb, colorin.a);