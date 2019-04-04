#version 400 core

#define M_PI 3.1415926535897932384626433832795

in vec3 mPos;
in vec3 mNorm;
in vec2 mTexPos;
in vec4 mWave;
in float mt;

out vec4 outColor;

uniform mat4 cam;
uniform mat4 mapTrans;
uniform sampler2D tex;
uniform float ambient;
uniform float unit;
uniform vec2 atlasCellSize;
uniform bool lines;
uniform vec3 lightDirect = normalize(vec3(-1,-1,-1));
uniform float lightDirectI = 0.3;
uniform vec3 lightPoint = vec3(40*8+20, 180, 40*9+20);
uniform float lightPointI = 1;

float dist2(vec3 v1, vec3 v2) {
	vec3 t = v1-v2;
	return (t.x*t.x + t.y*t.y + t.z*t.z);
}

void main() {
	vec2 newMTexPos = mTexPos;
	if(mWave.x != 0) {
		float angle = (M_PI/180.0)*(mWave.z+90);
		vec2 dir = vec2(cos(angle)*atlasCellSize.x, sin(angle)*atlasCellSize.y);
		vec2 axis = vec2(
		vec2 rel = vec2(mod(mTexPos.x, atlasCellSize.x), mod(mTexPos.y, atlasCellSize.y));
		vec2 off = mTexPos - rel;
		rel += dir*sin(2*M_PI*((length(rel/atlasCellSize)/(mWave.x)) + (unit*mWave.z)))*mWave.y;
		rel.x = mod(rel.x/atlasCellSize.x, 1)*atlasCellSize.x;
		rel.y = mod(rel.y/atlasCellSize.y, 1)*atlasCellSize.y;
		if(rel.x < 0)
			rel.x += atlasCellSize.x;
		if(rel.y < 0)
			rel.y += atlasCellSize.y;
		
		newMTexPos = off+rel;
		
	}
	
	vec4 color = texture(tex, newMTexPos);
	
	mat3 normalMatrix = transpose(inverse(mat3(mapTrans)));
	vec3 tNorm = normalize(normalMatrix * mNorm);
	
	float d2p = dist2(mPos, lightPoint)/lightPointI;
	float a = 1.0/(1.0 + pow(d2p, 2));
	float Kd = dot(lightDirect, tNorm) / (length(lightDirect)*length(tNorm));
	Kd = clamp(Kd, 0.0, 1.0);
	
	vec3 L = mPos - lightPoint;
	float Kp = dot(L, tNorm) / (length(L)/length(tNorm));
//	Kp = 1;
	
	float point = clamp(a, 0, 1);
	
	if(!lines)
		outColor = vec4((ambient*color + point*color + lightDirectI*Kd*color).xyz, color.w);
	else
		outColor = vec4(1,0,0,1);
}