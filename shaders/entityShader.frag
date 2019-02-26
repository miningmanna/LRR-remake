#version 400 core

in vec3 mPosition;
in vec3 mColor;
in vec2 mTexPos;

out vec4 outColor;

uniform mat4 modelTrans;
uniform mat4 animTrans;
uniform mat4 cam;
uniform vec3 aColor;
uniform sampler2D tex;
uniform bool calcAlpha;
uniform float framealpha;

bool eps(vec3 v1, vec3 v2, float epsilon) {
	return (abs(v1.x-v2.x) < epsilon) && (abs(v1.y-v2.y) < epsilon) && (abs(v1.z-v2.z) < epsilon);
}

void main()
{
	vec4 texColor = texture(tex, mTexPos);
	if(aColor.x >= 0 && aColor.y >= 0 && aColor.z >= 0) {
		bool isAlpha = eps(texColor.xyz, aColor, 0.00001);
		if(isAlpha) {
			discard;
		} else {
			float alpha = length(texColor.xyz-aColor)/sqrt(3);
			if(!calcAlpha)
				alpha = 1;
			outColor = vec4(texColor.xyz, alpha*framealpha);
		}
	} else {
		if(aColor.x > 0) {
			outColor = vec4(texColor.xyz, framealpha);
		} else {
			outColor = vec4(mColor, framealpha);
		}
	}
	
}