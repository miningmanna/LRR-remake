package org.rrr;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
	
	public static final int HIGHEST_KEYCODE = 512;
	
	public boolean[] isDown;
	public boolean[] justPressed;
	public boolean[] justReleased;
	
	public Vector4f mouse;
	private float lastx = 0, lasty = 0;
	private boolean first = true;
	
	private Keyboard kb;
	private Mouse ms;
	
	public Input() {
		kb = new Keyboard();
		ms = new Mouse();
	}
	
	public GLFWKeyCallbackI getKbHook() {
		return kb;
	}
	
	public GLFWCursorPosCallbackI getMsHook() {
		return ms;
	}
	
	public void update() {
		kb.update();
	}
	
	private class Keyboard implements GLFWKeyCallbackI {
		
		public Keyboard() {
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
	private class Mouse implements GLFWCursorPosCallbackI {
		
		public Mouse() {
			mouse = new Vector4f();
		}
		
		@Override
		public void invoke(long window, double _x, double _y) {
			
			float x = (float) _x;
			float y = (float) _y;
			
			float dx = x-lastx;
			float dy = y-lasty;
			lastx = x;
			lasty = y;
			
			if(first) {
				first = false;
				return;
			}
			
			mouse.x = x;
			mouse.y = y;
			mouse.z = dx;
			mouse.w = dy;
			
		}
		
	}
	
}
