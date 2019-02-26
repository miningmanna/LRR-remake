#version 400 core

in vec2 mTexPos;

out vec4 color;

uniform sampler2D tex;
uniform vec3 translate;
uniform vec2 scale;
uniform mat4 trans;
uniform bool blackAlpha;

bool eps(vec3 v1, vec3 v2, float epsilon) {
	return (abs(v1.x-v2.x) < epsilon) && (abs(v1.y-v2.y) < epsilon) && (abs(v1.z-v2.z) < epsilon);
}

void main() {
	
	color = texture(tex, mTexPos);
	color.w = 1;
	if(blackAlpha)
		if(eps(color.xyz, vec3(0,0,0), 0.0001))
			color.w = 0;
	
}
