package org.rrr;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat.Type;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.sound.AudioSystem;
import org.rrr.assets.sound.SoundClip;
import org.rrr.assets.sound.SoundStream;
import org.rrr.assets.sound.Source;
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
	private AudioSystem audioSystem;
	private Renderer renderer;
	private LegoConfig cfg;
	
	private Input input;
	private Cursor cursor;
	
	private Shader entityShader;
	private Shader mapShader;
	private Shader uiShader;
	private Menu curMenu;
	
	private Level currentLevel;
	
	public void start() {
		
		init();
		run();
		
		am.destroy();
		audioSystem.destroy();
		
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
		am = new AssetManager(cfg, new File("LegoRR0/World/Shared"));
		renderer = new Renderer();
		input = new Input(this);
		
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
		
		glfwSetMouseButtonCallback(window, input.getMsClckHook());
		
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			
			glfwGetWindowSize(window, pWidth, pHeight);
			
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
			
			glfwSetWindowPos(window,
					(vidmode.width() - pWidth.get(0))/2,
					(vidmode.height() - pHeight.get(0))/2);
		}
		
		audioSystem = new AudioSystem();
		audioSystem.init();
		
		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
	}
	
	private void run() {
		
//		String deviceName = alcGetString(0, ALC_DEFAULT_ALL_DEVICES_SPECIFIER);
//		long device = alcOpenDevice(deviceName);
//		
//		int[] attributes = {0};
//		long context = alcCreateContext(device, attributes);
//		alcMakeContextCurrent(context);
//		
//		ALCCapabilities	alcCap	= ALC.createCapabilities(device);
//		ALCapabilities	alCap	= AL.createCapabilities(alcCap);
//		
//		if(alCap.OpenAL10) {
//			System.out.println("OpenAL 1.0 supported");
//		}
//		
//		Audio a = null;
//		try {
//			File f = new File("LegoRR0/Sounds/DRIP1.WAV");
//			FileInputStream in = new FileInputStream(f);
//			a = AudioLoader.getAudio("wav", in);
//			in.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		int source = alGenSources();
//		
//		alSourcei(source, AL_BUFFER, a.getBufferID());
//		
//		alSourcePlay(source);
		
		GL.createCapabilities();
		
		glClearColor(0, 0, 0, 0);
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_BLEND);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		
		cursor = am.getCursor((Node) cfg.get("Lego*/Pointers"));
		cursor.setCursor("Standard");
		
		entityShader = am.getShader("entityShader");
		mapShader = am.getShader("mapShader");
		uiShader = am.getShader("uiShader");
		
		Matrix4f m = new Matrix4f();
		m.identity();
		m.translate(new Vector3f(0, 0, 5));
		
		Node mainMenuCfg = (Node) cfg.get("Lego*/Menu/MainMenuFull/Menu1");
		setMenu(mainMenuCfg);
		
		Type[] audioTypes = javax.sound.sampled.AudioSystem.getAudioFileTypes();
		System.out.println("SUPPORTED FORMATS:");
		for(Type t : audioTypes) {
			System.out.println(t.getExtension());
		}
		
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
		
		SoundStream stream = am.getSoundStream(new File("Track01.ogg"));
		Source s = audioSystem.getSource();
		s.play(stream);
		
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
			
			curMenu.update(delta);
			
			currentLevel.step(delta);
			currentLevel.render();
			
			uiShader.start();
			if(drawMenu)
				renderer.render(curMenu, uiShader);
			renderer.render(cursor, uiShader);
			uiShader.stop();
			
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
				Runtime.getRuntime().gc();
			}
			
			audioSystem.update();
			input.update();
			glfwPollEvents();
		}
		
	}
	
	public void setMenu(Node cfg) {
		curMenu = new Menu(this, cfg);
		curMenu.setInput(input);
	}
	
	public static void main(String[] args) {
		new RockRaidersRemake().start();
	}
	
	public AudioSystem getAudioSystem() {
		return audioSystem;
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
		return pWidth;
	}
	
	public float getHeight() {
		return pHeight;
	}
	
}
