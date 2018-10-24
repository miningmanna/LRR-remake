package org.rrr;

import static org.lwjgl.opengl.GL11.GL_DEPTH;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.rrr.cfg.LegoConfig;
import org.rrr.cfg.LegoConfig.Node;
import org.rrr.entity.Entity;
import org.rrr.gui.Cursor;
import org.rrr.model.Loader;
import org.rrr.model.MapMesh;

public class Level {
	
	private Shader mapShader, uiShader, entityShader;
	private Renderer renderer;
	private Loader loader;
	
	private ArrayList<Entity> entities;
	private MapMesh mapMesh;
	private Camera camera;
	private Cursor cursor;
	
	private float speed = 1.0f;
	
	public Level(RockRaidersRemake par) {
		
		this.loader = par.getLoader();
		this.renderer = par.getRenderer();
		this.mapShader = par.getMapShader();
		this.uiShader = par.getUiShader();
		this.entityShader = par.getEntityShader();
		this.camera = par.getCamera();
		entities = new ArrayList<>();
		
	}
	
	public Level(RockRaidersRemake par, Node cfg) {
		
		this(par);
		
		try {
			mapMesh = new MapMesh(loader, cfg);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
	}
	
	public void render() {
		
		mapShader.start();
		mapShader.setUniMatrix4f("cam", camera.combined);
		Matrix4f m = new Matrix4f();
		m.identity();
		m.scale(40);
		mapShader.setUniMatrix4f("mapTrans", m);
		renderer.render(mapMesh, mapShader);
		mapShader.stop();
		
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
		renderer.render(cursor);
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
