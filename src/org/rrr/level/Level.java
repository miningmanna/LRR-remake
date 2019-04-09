package org.rrr.level;

import static org.lwjgl.glfw.GLFW.*;

import java.io.IOException;
import java.util.ArrayList;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.rrr.Camera;
import org.rrr.Input;
import org.rrr.Renderer;
import org.rrr.RockRaidersRemake;
import org.rrr.Shader;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.map.Map;
import org.rrr.assets.map.MapData;
import org.rrr.assets.map.SurfaceTypeDescription;
import org.rrr.assets.model.MapMesh;
import org.rrr.gui.Cursor;

public class Level {
	
	
	private Shader mapShader, entityShader;
	private Renderer renderer;
	
	private ArrayList<Entity> entities;
	private Input input;
	public Camera camera;
	private Cursor cursor;
	private RockRaidersRemake par;
	
	private Map map;
	
	private float speed = 1.0f;
	private float tRot = 0;
	
	public Level(RockRaidersRemake par) {
		
		this.par = par;
		this.renderer = par.getRenderer();
		this.mapShader = par.getMapShader();
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
		try {
			map = new Map(par.getAssetManager(), cfg);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		
	}
	
	public void incrementIndex() { // TODO: remove test function
		cursor.curAnimation += 1;
		cursor.curAnimation %= cursor.animations.length;
		if(!cursor.animations[cursor.curAnimation].stillFrame)
			cursor.animations[cursor.curAnimation].anim.frame = 0;
	}
	
	public void render(float dt) {
		
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
		
		if(input.justPressed[GLFW_KEY_Q])
			tRot = (float) ((tRot+(Math.PI/2.0f))%(2.0f*Math.PI));
		if(input.justPressed[GLFW_KEY_E])
			tRot = (float) ((tRot+(3*Math.PI/2.0f))%(2.0f*Math.PI));
		
		camera.move(move);
		camera.rotateX(input.mouse.z * -0.001f);
		camera.rotateY(input.mouse.w * -0.001f);
		camera.update();
		
		cursor.x = (int) input.mouse.x;
		cursor.y = (int) input.mouse.y;
		
		Vector3f unprojOrig = new Vector3f(),
				 unprojDir	= new Vector3f();
		camera.combined.unprojectRay(input.mouse.x, par.getHeight()-input.mouse.y, new int[] {0,0,(int) par.getWidth(),(int) par.getHeight()},  unprojOrig, unprojDir);
		
		Vector3f hitPos = map.getHit(unprojOrig, unprojDir);
		
		Vector2i mapPos = null;
		if(hitPos != null) {
			mapPos = map.getTileUnderPoint(hitPos);
			hitPos.y += map.unitDist;
		}
//		if(mapPos.x != -1 && mapPos.y != -1)
//			System.out.println("CAVE " + mapPos + ": " + map.data.maps[MapData.DUGG][(int) mapPos.y][(int) mapPos.x]);
		
		
		mapShader.start();
		mapShader.setUniVector3f("lightDirect", camera.right);
		mapShader.setUniFloat("texRot", tRot);
		mapShader.setUniMatrix4f("cam", camera.combined);
		mapShader.setUniVector3f("camPos", camera.position);
		Matrix4f m = new Matrix4f() ;
		m.identity();
		mapShader.setUniMatrix4f("mapTrans", m);
		mapShader.setUniVector3f("lightDirect", new Vector3f(camera.right).mul(-1).add(0, -1, 0).normalize());
		if(hitPos != null)
			mapShader.setUniVector3f("lightPoint", hitPos);
		// Todo - mapmesh fix
		renderer.render(map, mapShader, mapPos, dt);
		mapShader.stop();
		
		entityShader.start();
		entityShader.setUniMatrix4f("cam", camera.combined);
		entityShader.setUniMatrix4f("modelTrans", m);
		
		for(int i = 0; i < entities.size(); i++) {
			renderer.render(entities.get(i), entityShader);
			if(entities.get(i).script != null)
				par.getEntityEngine().call(entities.get(i));
		}
		
		entityShader.stop();
		
	}
	
	public Entity spawn(String name) {
		try {
			Entity ent = Entity.getEntity(name);
			entities.add(ent);
			return ent;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void step(float delta) {
		map.update(delta);
		if(cursor != null)
			cursor.update(delta);
		for(Entity e : entities)
			e.step(delta * speed);
		
	}
	
	public Vector2i toMapPos(Vector3f pos) {
		return map.getTileUnderPoint(pos);
	}
	
	public Path pathFind(int sx, int sz, int dx, int dy) {
		Path res = new Path();
		
		// TODO: A star pathfinding
		
		return res;
	}
	
	public Path pathFindDijkstra(int sx, int sz, int dx, int dy) {
		Path res = new Path();
		
		// TODO: Dijkstra pathfinding
		
		return res;
	}
}
