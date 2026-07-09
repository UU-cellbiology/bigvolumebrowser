out vec4 fragColor;

in vec3 Normal;
in vec3 FragPos;
in vec3 posW;
in vec2 texCoord;

uniform float fnratio;

uniform int clipactive;
uniform vec3 clipmin;
uniform vec3 clipmax;
uniform mat4 cliptransform;

uniform vec4 colorMesh;
uniform int bUseTexture;
uniform sampler2D texture1;

uniform float depthDecay;

uniform int surfaceRender;
uniform float silDecay;
uniform int silType;
uniform int gridType;
uniform int wOIT;

//const vec3 ObjectColor = vec3(1, 1, 1);


//const vec3 lightColor1 = 0.5 * vec3(0.9, 0.9, 1);
const vec3 lightColor1 = vec3(1.0, 1.0, 1.0);
const vec3 lightDir1 = normalize(vec3(0, -0.2, -1));

const vec3 lightColor2 = 0.5 * vec3(0.1, 0.1, 1);
const vec3 lightDir2 = normalize(vec3(1, 1, 0.5));

const vec3 ambient = vec3(0.1, 0.1, 0.1);

const float specularStrength = 1;


vec3 diffuse(vec3 norm,  vec3 lightDir, vec3 lightColor)
{	
	return max(dot(norm, lightDir), 0.0) * lightColor;
}

vec3 specular(vec3 norm, vec3 viewDir, vec3 lightDir, vec3 lightColor, float shininess, float specularStrength)
{
	vec3 reflectDir = reflect(-lightDir, norm);
	float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
	return specularStrength * spec * lightColor;
}


void checkClipping()
{
    //ROI clipping
	if(clipactive>0)
	{
		vec3 posclip = ( cliptransform * vec4(posW,1.0) ).xyz;
		vec3 s = step(clipmin, posclip) - step(clipmax, posclip);
		if(s.x * s.y * s.z == clipactive - 1)
		{
			discard;
		}
	}
}

float linearizeDepth(float z)
{
	return z/(z - fnratio*z + fnratio);
}

void main()
{

	checkClipping();
	
	vec3 norm = normalize(Normal);
	vec3 viewDir = normalize(-FragPos);

	vec4 colorin = colorMesh;

	if(bUseTexture > 0)
	{
		colorin = texture( texture1, texCoord );
	}
		
	vec4 colorout = colorin;	
	

	//plain, shaded or shiny surface
	if(surfaceRender < 3)
	{
		//plain color -> do nothing
		//shaded/shiny
		if(surfaceRender > 0)
		{			
			vec3 diff = diffuse(norm,  lightDir1, lightColor1);
			vec3 spec = specular( norm, viewDir, lightDir1, lightColor1, 16.0, 1.0 ) * (surfaceRender - 1);
			colorout = vec4((ambient + diff ) * colorin.rgb + spec, colorin.a);
		}	
	}
	//silhouette surface
	else
	{	
		float alphax = min(1.0, 1.0 - pow( abs( dot(norm, viewDir)), silDecay));
		if(silType < 1)
		{
			//all transparent
			colorout = vec4(colorin.rgb, colorin.a * alphax);
		}
		else
		{			
			//front culling
			if(dot(norm,viewDir) > 0)
				discard;
			colorout = vec4(colorin.rgb * alphax, colorin.a);				
		}
	}
	if(wOIT > 0)
	{
		float z = linearizeDepth(gl_FragCoord.z); // 0.0 to 1.0
		float baseAlpha = colorout.a;
		float weight = exp(- depthDecay * z );
		colorout.xyz = colorout.xyz * baseAlpha * weight;
		colorout.a = baseAlpha * weight;
	}
    fragColor = colorout; 
}


