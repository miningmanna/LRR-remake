package org.rrr;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.awt.image.BufferedImage;
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
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.model.ModelLoader;
import org.rrr.assets.model.MapMesh;
import org.rrr.gui.BitMapFont;
import org.rrr.gui.Cursor;
import org.rrr.gui.Menu;
import org.rrr.level.Entity;
import org.rrr.level.EntityEngine;
import org.rrr.level.Level;


public class RockRaidersRemake {
	
	private long window;
	
	public static final String TITLE = "Rock Raiders remake";
	public static final int WIDTH = 640;
	public static final int HEIGHT = 480;
	
	private int pWidth = WIDTH, pHeight = HEIGHT;
	
	private AssetManager am;
	
	private Renderer renderer;
	private LegoConfig cfg;
	
	private Input input;
	private Cursor cursor;
	
	private Shader entityShader;
	private Shader mapShader;
	private Shader uiShader;
	
	private Level currentLevel;
	
	public void start() {
		
		init();
		run();
		
		am.destroy();
		
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
		am = new AssetManager(new File("LegoRR0/World/Shared"));
		renderer = new Renderer();
		input = new Input();
		
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
		
		glfwSetKeyCallback(window, input.getKbHook());
		
		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		glfwSetCursorPosCallback(window, input.getMsHook());
		
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
		
		Node menu1Node = (Node) cfg.get("Lego*/Menu/MainMenuFull/Menu1");
		menu1Node.printTree();
		Menu menu1 = new Menu(am, menu1Node);
		
		cursor = am.getCursor((Node) cfg.get("Lego*/Pointers"));
		
		entityShader = am.getShader("entityShader");
		mapShader = am.getShader("mapShader");
		uiShader = am.getShader("uiShader");
		
		Matrix4f m = new Matrix4f();
		m.identity();
		m.translate(new Vector3f(0, 0, 5));
		
		
		ArrayList<Entity> entities = new ArrayList<>();
		EntityEngine eng = new EntityEngine();
		
		Entity.setAssetManager(am);
		Entity.loadEntity(new File("LegoRR0/Mini-Figures/CAPTAIN"), "captain");
		Entity.loadEntity(new File("LegoRR0/Buildings/Barracks"), 	"barracks");
		
		Node l2cfg = (Node) cfg.get("Lego*/Levels/Level02");
		
		try {
			currentLevel = new Level(this, l2cfg);
			currentLevel.spawn("captain");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		renderer.init(am);
		
		BitMapFont font = am.getFont("Interface/FrontEnd/Menu_Font_HI.bmp");
		
		// FPS Counting
		float time = 0;
		int frames = 0;
		
		float speed = 1.0f;
		float delta = 0;
		long nano = 0, _nano = 0;
		boolean drawMenu = true;
		while(!glfwWindowShouldClose(window)) {
			
			glDepthMask(true);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			if(input.justReleased[GLFW_KEY_ESCAPE])
				glfwSetWindowShouldClose(window, true);
			
			if(input.justReleased[GLFW_KEY_E])
				speed *= 1.2f;
			if(input.justReleased[GLFW_KEY_Q])
				speed /= 1.2f;
			
			if(input.justReleased[GLFW_KEY_M])
				drawMenu = !drawMenu;
			
			if(input.justReleased[GLFW_KEY_UP])
				currentLevel.incrementIndex();
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
			
			glDepthMask(true);
			if(drawMenu) {
				uiShader.start();
				renderer.drawString(uiShader, 100, 100, font, " *4>", 1);
				renderer.render(menu1, uiShader);
				uiShader.stop();
			}
			glDepthMask(false);
			
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
	
	public static float clamp(float val, float min, float max) {
		return Math.max(min, Math.min(max, val));
	}
	
	public static void main(String[] args) {
		new RockRaidersRemake().start();
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public LegoConfig getCfg() {
		return cfg;
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

	public AssetManager getAssetManager() {
		return am;
	}
	
	public float getWidth() {
		return WIDTH;
	}
	
	public float getHeight() {
		return HEIGHT;
	}
	
}
