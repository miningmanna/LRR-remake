package org.rrr;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.File;
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
import org.rrr.map.MapData;
import org.rrr.model.Loader;
import org.rrr.model.LwsAnimation;
import org.rrr.model.LwsFileData;
import org.rrr.model.MapMesh;

import de.mm.entity.Entity;
import de.mm.entity.EntityEngine;


public class RockRaidersRebirth {
	
	private long window;
	
	public static final String VERSION = "0.1";
	public static final String TITLE = "Rock Raiders remake v."+VERSION;
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	
	private Loader loader;
	private Renderer renderer;
	
	private Camera camera;
	private Input input;
	
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
		loader = new Loader();
		renderer = new Renderer();
		camera = new Camera();
		input = new Input();
		float aspect = (float)WIDTH / (HEIGHT);
		camera.setFrustum(30, aspect, 0.1f, 1000);
		System.out.println(camera.frustrum);
		camera.update();
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
	
	Shader entityShader;
	Shader mapShader;
	private void run() {
		
		GL.createCapabilities();
		
		glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glEnable(GL_CULL_FACE);
		
		entityShader = new Shader(new File("entityShader.vert"), new File("entityShader.frag"));
		mapShader = new Shader(new File("mapShader.vert"), new File("mapShader.frag"));
		
		Matrix4f m = new Matrix4f();
		m.identity();
		m.translate(new Vector3f(0, 0, 5));
		
		
		/**
		*	TODO:
		*	TODO:
		*	TODO:
		*	Change the lwsFileName and the lwsDir, to match the wished models lws files name and dir.
		*	The sharedDir should be left as is. In all cases (i think) it is the right choice.
		*	Maybe the vehicles are a bit different, they have a shared folder as well.
		*
		*/
		String lwsFileName = "NEW_Captain_Point_CALL_T_ARMS.lws";
		File lwsDir = new File("CAPTAIN");
		File sharedDir = new File("Shared");
		
		ArrayList<Entity> entities = new ArrayList<>();
		
		EntityEngine eng = new EntityEngine();
		
		Entity.setLoader(loader);
		Entity.setSharedFolder(new File("Shared"));
		Entity.loadEntity(new File("CAPTAIN"), "captain");
		Entity.loadEntity(new File("Slug"), "slug");
		Entity.loadEntity(new File("Pilot"), "Pilot");
		Entity ent = null;
		try {
			entities.add(0, Entity.getEntity("slug"));
			ent = Entity.getEntity("slug");
			eng.bindScript(ent, "d = delta()\n"
								+ "move(0, 0, 5*d)\n"
								+ "turn(d)"); // Lua script
			ent.pos.z = 5;
			entities.add(1, ent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		MapData data = null;
		try {
			data = MapData.getMapData(new File("Level01"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		MapMesh mapMesh = new MapMesh(loader, data, data.width, data.height);
		
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
				move.mul(10);
			camera.move(move);
			
			if(input.justReleased[GLFW_KEY_E])
				speed *= 1.2f;
			if(input.justReleased[GLFW_KEY_Q])
				speed /= 1.2f;
			
			if(input.justReleased[GLFW_KEY_UP])
				ent.pos.add(0, 0, 1);
			if(input.justReleased[GLFW_KEY_DOWN])
				ent.pos.add(0, 0, -1);
			if(input.justReleased[GLFW_KEY_RIGHT])
				ent.pos.add(1, 0, 0);
			if(input.justReleased[GLFW_KEY_LEFT])
				ent.pos.add(-1, 0, 0);
			if(input.justReleased[GLFW_KEY_R])
				ent.rot.rotateY((float) (Math.PI/8));
			
			if(input.justReleased[GLFW_KEY_UP])
				ent.currentAnimation = (ent.currentAnimation +1)%ent.anims.length;
			
			camera.update();
			
			mapShader.start();
			mapShader.setUniMatrix4f("cam", camera.combined);
			m.identity();
			m.scale(10);
			mapShader.setUniMatrix4f("mapTrans", m);
//			renderer.render(mapMesh);
			mapShader.stop();
			
			entityShader.start();
			entityShader.setUniMatrix4f("cam", camera.combined);
			entityShader.setUniMatrix4f("modelTrans", m);
			
			for(int i = 0; i < entities.size(); i++) {
				renderer.render(entities.get(i), entityShader);
			}
			
			entityShader.stop();
				
			glfwSwapBuffers(window);
			
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
			
			input.update();
			glfwPollEvents();
		}
		
	}
	
	public static void main(String[] args) {
		new RockRaidersRebirth().start();
	}
	
}
