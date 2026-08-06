	vec3 norm = normalize(Normal);
	vec3 viewDir = normalize(-FragPos);
	vec3 diff = diffuse(norm,  lightDir1, lightColor1);
	vec3 spec = specular( norm, viewDir, lightDir1, lightColor1, 16.0, 1.0 );
	colorout = vec4((ambient + diff ) * colorin.rgb + spec, colorin.a);