#version 400 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 norm;
layout(location = 2) in vec2 texPos;
layout(location = 3) in vec4 wave;
layout(location = 4) in float t;

out vec3 mPos;
out vec3 mNorm;
out vec2 mTexPos;
out vec4 mWave;
out float mt;

uniform mat4 cam;
uniform mat4 mapTrans;

void main() {
	
	gl_Position = cam * mapTrans * vec4(pos.xyz, 1);
	
	mPos = (mapTrans * vec4(pos, 1)).xyz;
	mNorm = norm;
	mt = t;
	mWave = wave;
	mTexPos = texPos;
}