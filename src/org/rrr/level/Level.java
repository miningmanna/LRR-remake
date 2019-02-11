package org.rrr.level;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.rrr.Camera;
import org.rrr.Input;
import org.rrr.Renderer;
import org.rrr.RockRaidersRemake;
import org.rrr.Shader;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.model.ModelLoader;
import org.rrr.assets.model.MapMesh;
import org.rrr.gui.Cursor;

public class Level {
	
	
	private Shader mapShader, uiShader, entityShader;
	private Renderer renderer;
	
	private ArrayList<Entity> entities;
	private Input input;
	private MapMesh mapMesh;
	private Camera camera;
	private Cursor cursor;
	private RockRaidersRemake par;
	
	private float speed = 1.0f;
	
	public Level(RockRaidersRemake par) {
		
		this.par = par;
		this.renderer = par.getRenderer();
		this.mapShader = par.getMapShader();
		this.uiShader = par.getUiShader();
		this.entityShader = par.getEntityShader();
		this.cursor = par.getCursor();
		this.input = par.getInput();
		entities = new ArrayList<>();
		
		camera = new Camera();
		float aspect = (float)par.getWidth() / (par.getHeight());
		camera.setFrustum(30, aspect, 0.1f, 10000);
		camera.update();
		
	}
	
	public Level(RockRaidersRemake par, Node cfg) {
		
		this(par);
		
		// TODO - Mapmesh fix
//		try {
//			mapMesh = new MapMesh(loader, cfg);
//		} catch (Exception e2) {
//			e2.printStackTrace();
//		}
		
	}
	
	public void incrementIndex() { // TODO: remove test function
		cursor.curAnimation += 1;
		cursor.curAnimation %= cursor.animations.length;
		cursor.animations[cursor.curAnimation].frame = 0;
		System.out.println("Cursor: " + cursor.animations[cursor.curAnimation].name);
	}
	
	public void render() {
		
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
		camera.rotateX(input.mouse.z * -0.001f);
		camera.rotateY(input.mouse.w * -0.001f);
		camera.update();
		
		cursor.x = (int) input.mouse.x;
		cursor.y = (int) input.mouse.y;
		
		
		mapShader.start();
		mapShader.setUniMatrix4f("cam", camera.combined);
		Matrix4f m = new Matrix4f();
		m.identity();
		m.scale(40);
		mapShader.setUniMatrix4f("mapTrans", m);
		mapShader.setUniVector3f("lightDirect", new Vector3f(camera.right).mul(-1).add(0, -1, 0).normalize());
		cursor.update();
		// Todo - mapmesh fix
		//renderer.render(mapMesh, mapShader);
		mapShader.stop();
		
		mapShader.setUniVector3f("lightDirect", camera.right);
		
		entityShader.start();
		entityShader.setUniMatrix4f("cam", camera.combined);
		entityShader.setUniMatrix4f("modelTrans", m);
		
		for(int i = 0; i < entities.size(); i++) {
			renderer.render(entities.get(i), entityShader);
		}
		
		entityShader.stop();
		
		// Draw UI
		glDisable(GL_DEPTH);
		uiShader.start();
		renderer.render(cursor, uiShader);
		uiShader.stop();
		glEnable(GL_DEPTH);
		
	}
	
	public void spawn(String name) {
		
		try {
			entities.add(Entity.getEntity(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void step(float delta) {
		
		for(Entity e : entities)
			e.step(delta * speed);
		
	}
	
}
