package org.rrr;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.rrr.cfg.LegoConfig;
import org.rrr.cfg.LegoConfig.Node;
import org.rrr.entity.Entity;
import org.rrr.entity.EntityEngine;
import org.rrr.gui.Cursor;
import org.rrr.model.Loader;
import org.rrr.model.MapMesh;


public class RockRaidersRemake {
	
	private long window;
	
	public static final String TITLE = "Rock Raiders remake";
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	
	private Loader loader;
	private Renderer renderer;
	private LegoConfig cfg;
	
	private Camera camera;
	private Input input;
	private Cursor cursor;
	
	private Shader entityShader;
	private Shader mapShader;
	private Shader uiShader;
	
	private Level currentLevel;
	
	public void start() {
		
		init();
		run();
		
		loader.destroy();
		
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		
	}
	
	private void init() {
		
		
		try {
			FileInputStream in = new FileInputStream("LegoRR1/Lego.cfg");
			cfg = LegoConfig.getConfig(in);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		loader = new Loader();
		renderer = new Renderer();
		camera = new Camera();
		input = new Input();
		float aspect = (float)WIDTH / (HEIGHT);
		camera.setFrustum(30, aspect, 0.1f, 10000);
		camera.update();
		cursor = new Cursor();
		cursor.init((Node) cfg.get("Lego*/Pointers"), loader);
		
		
		
		// ----------------- GLFW INIT --------------
		GLFWErrorCallback.createPrint(System.out).set();
		
		if(!glfwInit())
			throw new IllegalStateException("Couldnt init GLFW");
		
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		
		window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
		if(window == NULL)
			throw new RuntimeException("Coulnt create window!");
		
		glfwSetKeyCallback(window, input);
		
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		glfwSetCursorPosCallback(window, new GLFWCursorPosCallbackI() {
			
			private double lastx = 0, lasty = 0;
			private boolean first = true;
			@Override
			public void invoke(long window, double x, double y) {
				
				double dx = x-lastx;
				double dy = y-lasty;
				lastx = x;
				lasty = y;
				
				if(first) {
					first = false;
					return;
				}
				
				camera.rotateY((float) (-dy * 0.001f));
				camera.rotateX((float) (-dx * 0.001f));
				
			}
		});
		
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			glfwGetWindowSize(window, pWidth, pHeight);
			
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			
			glfwSetWindowPos(window,
					(vidmode.width() - pWidth.get(0))/2,
					(vidmode.height() - pHeight.get(0))/2);
		}
		
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
	}
	
	private void run() {
		
		GL.createCapabilities();
		
		glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		
		entityShader = new Shader(new File("entityShader.vert"), new File("entityShader.frag"));
		mapShader = new Shader(new File("mapShader.vert"), new File("mapShader.frag"));
		uiShader = new Shader(new File("uiShader.vert"), new File("uiShader.frag"));
		
		Matrix4f m = new Matrix4f();
		m.identity();
		m.translate(new Vector3f(0, 0, 5));
		
		
		ArrayList<Entity> entities = new ArrayList<>();
		EntityEngine eng = new EntityEngine();
		
		Entity.setLoader(loader);
		Entity.setSharedFolder(new File("LegoRR0/World/Shared"));
		Entity.loadEntity(new File("LegoRR0/Mini-Figures/CAPTAIN"), "captain");
		Entity.loadEntity(new File("LegoRR0/Buildings/Barracks"), 		"barracks");
		
		Node l2cfg = (Node) cfg.get("Lego*/Levels/Level01");
		
		try {
			currentLevel = new Level(this, l2cfg);
			currentLevel.spawn("captain");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		// FPS Counting
		float time = 0;
		int frames = 0;
		
		float speed = 1.0f;
		float delta = 0;
		long nano = 0, _nano = 0;
		while(!glfwWindowShouldClose(window)) {
			
			glDepthMask(true);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			if(input.justReleased[GLFW_KEY_ESCAPE])
				glfwSetWindowShouldClose(window, true);
			Vector3f move = new Vector3f(0, 0, 0);
			if(input.isDown[GLFW_KEY_W])
				move.z += 0.1f;
			if(input.isDown[GLFW_KEY_A])
				move.x += -0.1f;
			if(input.isDown[GLFW_KEY_S])
				move.z += -0.1f;
			if(input.isDown[GLFW_KEY_D])
				move.x += 0.1f;
			if(input.isDown[GLFW_KEY_LEFT_CONTROL])
				move.y += 0.1f;
			if(input.isDown[GLFW_KEY_SPACE])
				move.y += -0.1f;
			if(input.isDown[GLFW_KEY_LEFT_SHIFT])
				move.mul(40);
			camera.move(move);
			
			if(input.justReleased[GLFW_KEY_E])
				speed *= 1.2f;
			if(input.justReleased[GLFW_KEY_Q])
				speed /= 1.2f;
			
//			if(input.justReleased[GLFW_KEY_UP])
//				ent.pos.add(0, 0, 1);
//			if(input.justReleased[GLFW_KEY_DOWN])
//				ent.pos.add(0, 0, -1);
//			if(input.justReleased[GLFW_KEY_RIGHT])
//				ent.pos.add(1, 0, 0);
//			if(input.justReleased[GLFW_KEY_LEFT])
//				ent.pos.add(-1, 0, 0);
//			if(input.justReleased[GLFW_KEY_R])
//				ent.rot.rotateY((float) (Math.PI/8));
//			
//			if(input.justReleased[GLFW_KEY_UP])
//				ent.currentAnimation = (ent.currentAnimation +1)%ent.anims.length;
			
			camera.update();
			
			
			currentLevel.step(delta);
			currentLevel.render();
			
			
			glfwSwapBuffers(window);
			
			
			// Delta Time
			_nano = System.nanoTime();
			delta = (float) (_nano - nano) / 1000000000;
			nano = _nano;
			
			if(delta < 1.0f) {
				eng.step(delta*speed);
				for(Entity e : entities) {
					e.step(delta*speed);
					eng.call(e);
				}
			}
			
			// FPS Counting
			frames++;
			time += delta;
			if(time > 1.0f) {
				time %= 1.0f;
				System.out.println("FPS: " + frames);
				frames = 0;
			}
			
			input.update();
			glfwPollEvents();
		}
		
	}
	
	public static void main(String[] args) {
		new RockRaidersRemake().start();
	}

	public Loader getLoader() {
		return loader;
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public LegoConfig getCfg() {
		return cfg;
	}

	public Camera getCamera() {
		return camera;
	}

	public Input getInput() {
		return input;
	}

	public Cursor getCursor() {
		return cursor;
	}

	public Shader getEntityShader() {
		return entityShader;
	}

	public Shader getMapShader() {
		return mapShader;
	}

	public Shader getUiShader() {
		return uiShader;
	}

	public Level getCurrentLevel() {
		return currentLevel;
	}
	
}
