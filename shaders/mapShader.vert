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
uniform sampler2D tex;
uniform float texRot;
uniform bool lines;
uniform vec3 lightDirect;
uniform float lightDirectI;
uniform vec3 lightPoint;
uniform float lightPointI;

void main() {
	
	gl_Position = cam * mapTrans * vec4(pos.xyz, 1);
	
	mPos = (mapTrans * vec4(pos, 1)).xyz;
	mNorm = norm;
	mt = t;
	mWave = wave;
	vec2 tOrig = vec2(0.5);
	mat2 tRot;
	tRot[0] = vec2(cos(texRot), -sin(texRot));
	tRot[1] = vec2(sin(texRot),  cos(texRot));
	mTexPos = tOrig + (tRot * (texPos-tOrig));
	
}