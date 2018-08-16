package org.rrr;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL11.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Shader {
	
	private int program, vShader, fShader;
	
	public Shader(File vertexShader, File fragmentShader) {
		this.vShader = loadShader(vertexShader, GL_VERTEX_SHADER);
		this.fShader = loadShader(fragmentShader, GL_FRAGMENT_SHADER);
		program = glCreateProgram();
		glAttachShader(program, vShader);
		glAttachShader(program, fShader);
		glLinkProgram(program);
		glValidateProgram(program);
	}
	
	public void start() {
		glUseProgram(program);
	}
	
	public void stop() {
		glUseProgram(0);
	}
	
	public void destroy() {
		stop();
		glDetachShader(program, vShader);
		glDetachShader(program, fShader);
		glDeleteShader(vShader);
		glDeleteShader(fShader);
		glDeleteProgram(program);
	}
	
	private static int loadShader(File file, int type) {
		
		StringBuilder shaderSource = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line = br.readLine()) != null)
				shaderSource.append(line).append("\n");
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		int shader = glCreateShader(type);
		glShaderSource(shader, shaderSource);
		glCompileShader(shader);
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
			System.err.println(glGetShaderInfoLog(shader, 512));
		return shader;
		
	}

	public void setUniFloat(int pos, float f) {
		glUniform1f(pos, f);
	}
	
	public void setUniFloat(String name, float f) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniFloat(glGetUniformLocation(program, name), f);
	}

	public void setUniVector2f(int pos, Vector2f v) {
		if(v == null)
			throw new IllegalArgumentException("Vector cant be null!");
		glUniform2f(pos, v.x, v.y);
	}
	
	public void setUniVector2f(String name, Vector2f v) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniVector2f(glGetUniformLocation(program, name), v);
	}
	
	public void setUniVector3f(int pos, Vector3f v) {
		if(v == null)
			throw new IllegalArgumentException("Vector cant be null!");
		glUniform3f(pos, v.x, v.y, v.z);
	}
	
	public void setUniVector3f(String name, Vector3f v) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniVector3f(glGetUniformLocation(program, name), v);
	}
	
	public void setUniBoolean(int pos, boolean b) {
		if(b)
			glUniform1i(pos, 1);
		else
			glUniform1i(pos, 0);
	}
	
	public void setUniBoolean(String name, boolean b) {
		setUniBoolean(glGetUniformLocation(program, name), b);
	}
	
	public void setUniMatrix4f(int pos, Matrix4f m) {
		if(m == null)
			throw new IllegalArgumentException("Matrix cant be null!");
		FloatBuffer b = BufferUtils.createFloatBuffer(16);
		m.get(b);
		glUniformMatrix4fv(pos, false, b);
	}
	
	public void setUniMatrix4f(String name, Matrix4f m) {
		if(name == null)
			throw new IllegalArgumentException("Name cant be null!");
		setUniMatrix4f(glGetUniformLocation(program, name), m);
	}
	
	public int[] getUniPos(String... names) {
		if(names == null) {
			return null;
		}
		int[] res = new int[names.length];
		
		for(int i = 0; i < names.length; i++) {
			if(names[i] == null)
				res[i] = -1;
			res[i] = glGetUniformLocation(program, names[i]);
		}
		
		return res;
	}
	
}
