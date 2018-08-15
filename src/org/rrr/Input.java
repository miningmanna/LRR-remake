package org.rrr;

import org.lwjgl.glfw.GLFWKeyCallbackI;

import static org.lwjgl.glfw.GLFW.*;

public class Input implements GLFWKeyCallbackI {
	
	public static final int HIGHEST_KEYCODE = 512;
	
	public boolean[] isDown;
	public boolean[] justPressed;
	public boolean[] justReleased;
	
	public Input() {
		isDown = new boolean[HIGHEST_KEYCODE];
		justPressed = new boolean[HIGHEST_KEYCODE];
		justReleased = new boolean[HIGHEST_KEYCODE];
	}
	
	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		if(key == -1)
			return;
		if(action == GLFW_PRESS) {
			justPressed[key] = true;
			isDown[key] = true;
		} else if(action == GLFW_RELEASE) {
			isDown[key] = false;
			justReleased[key] = true;
		}
	}
	
	public void update() {
		for(int i = 0; i < HIGHEST_KEYCODE; i++) {
			justPressed[i] = false;
			justReleased[i] = false;
		}
	}

}
