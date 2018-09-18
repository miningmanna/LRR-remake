#version 400 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 norm;
layout(location = 2) in vec3 texPos;
layout(location = 3) in float surfType;

out vec3 mPos;
out vec3 mNorm;
out vec3 mTexPos;
out float mSurfType;

uniform mat4 cam;
uniform mat4 mapTrans;
uniform sampler2D tex;
uniform bool lines;
uniform vec3 lightDirect;
uniform float lightDirectI;
uniform vec3 lightPoint;
uniform float lightPointI;

void main() {
	
	gl_Position = cam * mapTrans * vec4(pos.xyz, 1);
	
	mPos = (mapTrans * vec4(pos, 1)).xyz;
	mNorm = norm;
	mTexPos = texPos;
	mSurfType = surfType;
	
}