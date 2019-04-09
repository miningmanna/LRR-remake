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
uniform vec3 camPos;
uniform vec3 lightDirect = normalize(vec3(-1,-0.1,-1));
uniform float lightDirectI = 10000;
uniform vec3 lightPoint = vec3(40*8+20, 180, 40*9+20);
uniform float lightPointI = 10000;

void main() {
	vec2 newMTexPos = mTexPos;
	if(mWave.x != 0) {
		float angle = (M_PI/180.0)*(mWave.z+90);
		vec2 dir = vec2(cos(angle), sin(angle));
		newMTexPos /= atlasCellSize;
		vec2 rel = vec2(mod(mTexPos.x, atlasCellSize.x), mod(mTexPos.y, atlasCellSize.y));
		float dist = length(cross(vec3(rel/atlasCellSize, 0), vec3(normalize(dir), 0)));
		dir *= atlasCellSize;
		vec2 off = mTexPos - rel;
		rel += dir*sin(2*M_PI*(dist/mWave.x) + (2*M_PI*mt*mWave.w))*mWave.y;
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
	
	float Kd = dot(lightDirect, tNorm) / (length(lightDirect)*length(tNorm));
	float d2c = length(mPos+camPos);
	Kd *= lightDirectI/(1.0 + pow(d2c, 2));
	Kd = clamp(Kd, 0.0, 0.3);
	
	float a = lightPointI/(1.0 + pow(length(mPos-lightPoint), 2));
	vec3 L = mPos - lightPoint;
	float Kp = dot(L, tNorm) / (length(L)/length(tNorm));
	
	float point = clamp(a*Kp, 0, 0.3);
	
	outColor = vec4((ambient*color + point*color + Kd*color).xyz, color.w);
}