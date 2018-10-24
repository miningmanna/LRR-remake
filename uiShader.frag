#version 400 core

in vec2 mTexPos;

out vec4 color;

uniform sampler2D tex;
uniform vec3 translate;
uniform mat4 trans;

void main() {
	
	color = texture(tex, mTexPos);
	
}
