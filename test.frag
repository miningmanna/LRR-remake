#version 400 core

in vec3 mPosition;
in vec3 mColor;
in vec2 mTexPos;

out vec4 outColor;

uniform mat4 transform;
uniform mat4 cam;
uniform vec3 aColor;
uniform sampler2D tex;
uniform bool calcAlpha;
uniform bool useTex;

void main()
{
	vec4 texColor = texture(tex, mTexPos);
	if(aColor.x >= 0 && aColor.y >= 0 && aColor.z >= 0) {
		bool isAlpha = (abs(texColor.x-aColor.x) < 0.00001) && (abs(texColor.y-aColor.y) < 0.00001) && (abs(texColor.z-aColor.z) < 0.00001);
		if(isAlpha) {
			discard;
		} else {
			float alpha = length(texColor.xyz-aColor)/sqrt(3);
			if(!calcAlpha)
				alpha = 1;
			outColor = vec4(texColor.xyz, alpha);
		}
	} else {
		if(aColor.x > 0) {
			outColor = texColor;
		} else {
			outColor = vec4(mColor, 1);
		}
	}
	
	if(!useTex)
		outColor = vec4(mColor, 1);
	
}