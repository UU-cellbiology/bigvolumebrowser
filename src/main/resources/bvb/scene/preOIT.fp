uniform float depthDecay;
uniform float fnratio;

float linearizeDepth(float z)
{
	return z/(z - fnratio*z + fnratio);
}