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
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.map.SurfaceTypeDescription;
import org.rrr.assets.model.LwsAnimation;
import org.rrr.assets.sound.AudioSystem;
import org.rrr.assets.sound.SoundClip;
import org.rrr.gui.BitMapFont;
import org.rrr.gui.Cursor;
import org.rrr.gui.Menu;
import org.rrr.level.Entity;
import org.rrr.level.EntityEngine;
import org.rrr.level.Level;
import org.rrr.level.Path;


public class RockRaidersRemake {
	
	private long window;
	
	public static final String TITLE = "Rock Raiders remake";
	public static final int WIDTH = 640;
	public static final int HEIGHT = 480;
	
	private int pWidth = WIDTH, pHeight = HEIGHT;
	
	private AssetManager am;
	private AudioSystem audioSystem;
	private Renderer renderer;
	private DelayedProcessor dProcessor;
	private LegoConfig cfg;
	private Node triggerCfg;
	private EntityEngine eng;
	
	private SoundClip menuTransition;
	private LwsAnimation rockAnim;
	
	private Input input;
	private Cursor cursor;
	
	private Shader entityShader;
	private Shader mapShader;
	private Shader uiShader;
	private Menu curMenu;
	
	private Level currentLevel;
	
	private long nano = 0, _nano = 0;
	
	public void start() {
		
		nano = System.nanoTime();
		
		init();
		run();
		
		entityShader.destroy();
		mapShader.destroy();
		uiShader.destroy();
		
		am.destroy();
		audioSystem.destroy();
		
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		
	}
	
	private void init() {
		
		dProcessor = new DelayedProcessor();
		
		try {
			am = new AssetManager(new File("priorities.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(am == null) {
			System.err.println("ERROR initializing assetmanager");
			System.exit(-1);
		}
		cfg = am.getConfig();
		
		triggerCfg = (Node) cfg.get("Lego*/Triggers");
		
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
		
		cursor = am.getCursor(this, (Node) cfg.get("Lego*/Pointers"));
		cursor.setCursor("Standard");
		
		entityShader = am.getShader("entityShader");
		mapShader = am.getShader("mapShader");
		uiShader = am.getShader("uiShader");
		
		Matrix4f m = new Matrix4f();
		m.identity();
		m.translate(new Vector3f(0, 0, 5));
		
		rockAnim = am.getAnimation("Interface/FrontEnd/Rock_wipe/RockWipe.lws");
		rockAnim.loop = false;
		Camera c = new Camera();
		float aspect = (float)pWidth / (pHeight);
		//c.setOrtho(10, -10, -10, 10, -100, 100);
		//c.setFrustum(30, aspect, 0.1f, 10000);
		c.position.set(0, 0, -15);
		c.update();
		
		Node mainMenuCfg = (Node) cfg.get("Lego*/Menu/MainMenuFull/Menu1");
		curMenu = new Menu(RockRaidersRemake.this, mainMenuCfg, triggerCfg);
		curMenu.setInput(input);
		
		ArrayList<Entity> entities = new ArrayList<>();
		eng = new EntityEngine(this);
		
		Entity.setAssetManager(am);
		Entity.loadEntity("Mini-Figures/CAPTAIN", "captain");
		Entity.loadEntity("Creatures/TinyIM", "tinyIM");
//		Entity.loadEntity(new File("LegoRR0/Buildings/Barracks"), 	"barracks");
		
		Node l2cfg = (Node) cfg.get("Lego*/Levels/Level22");
		Entity im = null;
		try {
			currentLevel = new Level(this, l2cfg);
			currentLevel.spawn("captain");
			im = currentLevel.spawn("tinyIM");
			SurfaceTypeDescription std = currentLevel.getSTypes();
			String[] walkableSurfs = new String[] {
				"DirtFloor",
				"Lava"
			};
			im.walkables = new int[walkableSurfs.length];
			for(int i = 0; i < walkableSurfs.length; i++) {
				int index = -1;
				for(int j = 0; j < std.surfaces.length; j++) {
					if(std.surfaces[j].name.equalsIgnoreCase(walkableSurfs[i])) {
						index = j;
						break;
					}
				}
				im.walkables[i] = index;
				System.out.println("Walkable: " + walkableSurfs[i] + " is " + im.walkables[i]);
			}
			Path p = new Path();
			p.tiles = new ArrayList<>();
			p.tiles.add(new Vector2i(0, 0));
			p.tiles.add(new Vector2i(1, 0));
			p.tiles.add(new Vector2i(1, 1));
			p.tiles.add(new Vector2i(0, 1));
			p.tiles.add(new Vector2i(5, 5));
			im.curPath = p;
			im.curPathStep = 0;
			eng.bindScript(im, new File("TinyIMScript.lua"));
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
		renderer.init(am);
		
		BitMapFont font = am.getFont("Interface/FrontEnd/Menu_Font_HI.bmp");
		
		menuTransition = am.getSample("SFX_RockWipe");
		
//		SoundStream stream = am.getSoundStream(new File("Track01.ogg"));
//		Source s = audioSystem.getSource();
//		s.play(stream);
		
		// FPS Counting
		float time = 0;
		int frames = 0;
		
		float speed = 1.0f;
		float delta = 0;
		boolean drawMenu = true;
		int scale = 2;
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
			
//			if(input.justReleased[GLFW_KEY_UP])
//				currentLevel.incrementIndex();
			
			if(input.isDown[GLFW_KEY_UP])
				curMenu.scroll(0, -10);
			if(input.isDown[GLFW_KEY_DOWN])
				curMenu.scroll(0, 10);
			
			if(input.justReleased[GLFW_KEY_1])
				rockAnim.time = 0;
			if(input.justReleased[GLFW_KEY_2])
				System.out.println(scale);
			if(input.justReleased[GLFW_KEY_3])
				scale++;
			if(input.justReleased[GLFW_KEY_4])
				scale--;
			
			Vector3f unprojOrig = new Vector3f(),
					 unprojDir	= new Vector3f();
			currentLevel.camera.combined.unprojectRay(input.mouse.x, getHeight()-input.mouse.y, new int[] {0,0,(int) getWidth(),(int) getHeight()},  unprojOrig, unprojDir);
			
			Vector3f hitPos = currentLevel.map.getHit(unprojOrig, unprojDir);
			
			Vector2i curPos = null;
			if(hitPos != null) {
				curPos = currentLevel.map.getTileUnderPoint(hitPos);
				hitPos.y += currentLevel.map.unitDist;
			}
			
			if(input.mouseJustPressed[GLFW_MOUSE_BUTTON_1]) {
				Vector2i mapPos = currentLevel.toMapPos(im.pos);
				if(curPos != null)
					im.curPath = currentLevel.pathFind(mapPos.x, mapPos.y, curPos.x, curPos.y, im.walkables);
			}
			
			c.setOrtho(scale*aspect, -scale*aspect, -scale, scale, -100, 100);
			
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
			
			uiShader.start();
			if(drawMenu)
				renderer.render(curMenu, uiShader);
			uiShader.stop();
			
			if(drawMenu)
				curMenu.update(delta);
			
			currentLevel.step(delta);
			currentLevel.render(delta);
			
			uiShader.start();
			renderer.render(cursor, uiShader);
			uiShader.stop();
			
			entityShader.start();
			entityShader.setUniMatrix4f("modelTrans", new Matrix4f().identity());
			c.update();
			entityShader.setUniMatrix4f("cam", c.combined);
			renderer.render(rockAnim, entityShader);
			entityShader.stop();
			
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
			
			dProcessor.update(delta);
			rockAnim.step(delta);
			
			// FPS Counting
			frames++;
			time += delta;
			if(time > 1.0f) {
				time %= 1.0f;
				System.out.println("FPS: " + frames);
				frames = 0;
			}
			
			audioSystem.update();
			input.update();
			glfwPollEvents();
		}
		
	}
	
	public void setMenu(Node cfg) {
		audioSystem.stopPublic();
		audioSystem.playPublic(menuTransition);
		rockAnim.time = 0;
		dProcessor.queue(0.5f, new Runnable() {
			@Override
			public void run() {
				curMenu.stop();
				curMenu = new Menu(RockRaidersRemake.this, cfg, triggerCfg);
				curMenu.setInput(input);
			}
		});
	}
	
	public void setLevel(Node cfg) {
		currentLevel = new Level(this, cfg);
		currentLevel.spawn("captain");
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
	
	public Input getInput() {
		return input;
	}
	
	public Cursor getCursor() {
		return cursor;
	}
	
	public Shader getEntityShader() {
		return entityShader;
	}
	
	public EntityEngine getEntityEngine() {
		return eng;
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
	
	public DelayedProcessor getDelayedProcessor() {
		return dProcessor;
	}
	
	public float getWidth() {
		return pWidth;
	}
	
	public float getHeight() {
		return pHeight;
	}

	public void stop() {
		glfwSetWindowShouldClose(window, true);
	}
	
}
