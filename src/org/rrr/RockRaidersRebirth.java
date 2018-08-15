package org.rrr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Set;



import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.rrr.model.CTexModel;
import org.rrr.model.FullModel;
import org.rrr.model.Loader;
import org.rrr.model.LwobFileData;
import org.rrr.model.LwsAnimation;
import org.rrr.model.LwsFileData;
import org.rrr.model.ObjFileData;
import org.rrr.model.TestData;
import org.rrr.model.PathConverter;
import org.rrr.model.UvFileData;
import org.rrr.model.LwobFileData.Surface;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


public class RockRaidersRebirth {
	
	private long window;
	
	public static final String version = "0.1";
	public static final String TITLE = "Rock Raiders rebirth v."+version;
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
	
	Shader testShader;
	private void run() {
		
		GL.createCapabilities();
		
		glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		
		testShader = new Shader(new File("test.vert"), new File("test.frag"));
		
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
		String lwsFileName = "ToolStnTele";
		File lwsDir = new File("Toolstation");
		File sharedDir = new File("Shared");
		
		RockRaiderPathFilter filter = new RockRaiderPathFilter(lwsDir, sharedDir);
		
		LwsAnimation animation = null;
		try {
			LwsFileData lwsData = LwsFileData.getLwsFileData(new File(lwsDir, lwsFileName));
			animation = LwsAnimation.getAnimation(lwsData, loader, filter);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
//		CTexModel model = null;
//		try {
//			model = loader.getCtexModelFromLwobFile(new File(dir, "VLPHead.lwo"), filter);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		Set<String> keys = loader.texs.keySet();
//		for(int i = 0; i < model.texs.length; i++) {
//			System.out.println(model.surfLen[i]);
//			for(String key : keys) {
//				if(loader.texs.get(key) == model.texs[i]) {
//					System.out.println(key);
//					continue;
//				}
//			}
//		}
		
		
		glDepthFunc(GL_LESS);
		glEnable(GL_CULL_FACE);
		
		boolean animate = true;
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
			
			if(input.justReleased[GLFW_KEY_UP])
				renderer.surfi++;
			if(input.justReleased[GLFW_KEY_DOWN])
				renderer.surfi--;
			
			if(input.justReleased[GLFW_KEY_E]) {
				animate = !animate;
			}
			
			camera.update();
			
			testShader.start();
			testShader.setUniMatrix4f("cam", camera.combined);
			testShader.setUniMatrix4f("transform", m);
			testShader.setUniBoolean("useTex", true);
			
//			renderer.render(model, testShader);
			
			renderer.render(animation, testShader);
			
			testShader.stop();
			
			glfwSwapBuffers(window);
			
			_nano = System.nanoTime();
			delta = (float) (_nano - nano) / 1000000000;
			nano = _nano;
			
			animation.step(delta);
			
			input.update();
			glfwPollEvents();
		}
		
	}
	
	public static void main(String[] args) {
		new RockRaidersRebirth().start();
	}
	
}
