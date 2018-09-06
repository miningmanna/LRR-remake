#version 400 core

in vec3 mPos;
in vec3 mTexPos;
in float mSurfType;

out vec4 outColor;

uniform mat4 cam;
uniform mat4 mapTrans;
uniform sampler2D tex;
uniform bool lines;

void main() {
	if(!lines)
		outColor = texture(tex, mTexPos.xy);
	else
		outColor = vec4(1,0,0,1);
}