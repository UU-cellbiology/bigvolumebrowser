	vec3 norm = normalize(Normal);
	vec3 viewDir = normalize(-FragPos);
	float alphax = min(1.0, 1.0 - pow( abs( dot(norm, viewDir)), silDecay));
	colorout = vec4(colorin.rgb, colorin.a * alphax);