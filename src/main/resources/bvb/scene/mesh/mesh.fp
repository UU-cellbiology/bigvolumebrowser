out vec4 fragColor;

in vec3 Normal;
in vec3 FragPos;
in vec3 posW;
in vec2 texCoord;

//$insert{preClip}

uniform vec4 colorMesh;
uniform int bUseTexture;
uniform sampler2D texture1;

uniform float silDecay;

//const vec3 ObjectColor = vec3(1, 1, 1);
//const vec3 lightColor1 = 0.5 * vec3(0.9, 0.9, 1);
const vec3 lightColor1 = vec3(1.0, 1.0, 1.0);
const vec3 lightDir1 = normalize(vec3(0, -0.2, -1));

const vec3 lightColor2 = 0.5 * vec3(0.1, 0.1, 1);
const vec3 lightDir2 = normalize(vec3(1, 1, 0.5));

const vec3 ambient = vec3(0.1, 0.1, 0.1);

const float specularStrength = 1;

//$insert{preOIT}

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

void main()
{

//--- clipping, if active  ------
//$insert{mClip}
	
	vec4 colorin = colorMesh;

	if(bUseTexture > 0)
	{
		colorin = texture( texture1, texCoord );
	}
		
	vec4 colorout = colorin;	

//--- surface render ------
//$insert{meshSurfaceRender}
		
//--- transparency mode, if active ------
//$insert{wOIT}
   fragColor = colorout; 
}


