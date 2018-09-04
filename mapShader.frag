#version 400 core

in vec3 mPos;
in vec3 mTexPos;
in float mSurfType;

out vec4 outColor;

uniform mat4 cam;
uniform mat4 mapTrans;
uniform sampler2D texs[16];

void main() {
	
	if(mSurfType > 4)
		outColor = vec4(1, (mSurfType*0.15), 0, 1);
	else
		outColor = vec4(0,0,0,1);
	
}