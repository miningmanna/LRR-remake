#version 400 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 texPos;

out vec2 mTexPos;

uniform sampler2D tex;
uniform vec3 translate;
uniform vec2 scale = vec2(1, 1);
uniform vec2 texScale = vec2(1, 1);
uniform vec2 texOffset = vec2(0, 0);
uniform mat4 trans;

void main() {
	
	vec3 _scale = vec3(scale, 1.0);
	gl_Position = ((trans * vec4(pos*_scale, 1)) + vec4(translate, 0));
	
	mTexPos = texOffset+(texPos*texScale);
	
}
