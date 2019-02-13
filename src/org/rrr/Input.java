package org.rrr;

import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
	
	public static final int HIGHEST_KEYCODE = 512;
	public static final int HIGHEST_MOUSE_KEYCODE = 16;
	
	public boolean[] isDown;
	public boolean[] justPressed;
	public boolean[] justReleased;
	
	public boolean[] mouseIsDown;
	public boolean[] mouseJustPressed;
	public boolean[] mouseJustReleased;
	
	public Vector4f mouse;
	private float lastx = 0, lasty = 0;
	private boolean first = true;
	
	private Keyboard kb;
	private Mouse ms;
	private MouseClick msClck;
	
	private RockRaidersRemake par;
	
	public Input(RockRaidersRemake par) {
		kb = new Keyboard();
		ms = new Mouse();
		msClck = new MouseClick();
		this.par = par;
	}
	
	public GLFWKeyCallbackI getKbHook() {
		return kb;
	}
	
	public GLFWCursorPosCallbackI getMsHook() {
		return ms;
	}
	
	public GLFWMouseButtonCallbackI getMsClckHook() {
		return msClck;
	}
	
	public void update() {
		kb.update();
		ms.update();
		msClck.update();
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
		
		public void update() {
			mouse.z = 0;
			mouse.w = 0;
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
			
			mouse.x = clamp(mouse.x + dx, 0, par.getWidth());
			mouse.y = clamp(mouse.y + dy, 0, par.getHeight());
			mouse.z = dx;
			mouse.w = dy;
			
		}
		
	}
	
	private class MouseClick implements GLFWMouseButtonCallbackI {
		
		public MouseClick() {
			mouseIsDown = new boolean[HIGHEST_MOUSE_KEYCODE];
			mouseJustPressed = new boolean[HIGHEST_MOUSE_KEYCODE];
			mouseJustReleased = new boolean[HIGHEST_MOUSE_KEYCODE];
		}
		
		@Override
		public void invoke(long window, int button, int action, int mods) {
			if(button == -1)
				return;
			if(action == GLFW_PRESS) {
				mouseJustPressed[button] = true;
				mouseIsDown[button] = true;
			} else if(action == GLFW_RELEASE) {
				mouseIsDown[button] = false;
				mouseJustReleased[button] = true;
			}
		}
		
		public void update() {
			for(int i = 0; i < HIGHEST_MOUSE_KEYCODE; i++) {
				mouseJustPressed[i] = false;
				mouseJustReleased[i] = false;
			}
		}
		
	}
	
	public int getMouseAbsX() {
		return (int) (mouse.x + par.getWidth()/2);
	}
	
	public int getMouseAbsY() {
		return (int) (mouse.y + par.getHeight()/2);
	}
	
	private static float clamp(float val, float min, float max) {
		if(val < min)
			return min;
		if(val > max)
			return max;
		return val;
	}
	
}
