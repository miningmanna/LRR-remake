package org.rrr.level;

import static org.lwjgl.glfw.GLFW.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.rrr.Camera;
import org.rrr.Input;
import org.rrr.Renderer;
import org.rrr.RockRaidersRemake;
import org.rrr.Shader;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.map.Map;
import org.rrr.assets.map.SurfaceTypeDescription;
import org.rrr.gui.Cursor;

public class Level {
	
	
	private Shader mapShader, entityShader;
	private Renderer renderer;
	
	private ArrayList<Entity> entities;
	private Input input;
	public Camera camera;
	private Cursor cursor;
	private RockRaidersRemake par;
	
	public Map map;
	
	private long[][] costBuffer;
	
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
		if(map != null)
			costBuffer = new long[map.h][map.w];
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
	
	private boolean containsInt(int[] a, int v) {
		for(int i = 0; i < a.length; i++)
			if(a[i] == v)
				return true;
		return false;
	}
	
	public Path pathFind(int sx, int sz, int dx, int dz, int[] walkables) {
		
		boolean[][] walkable = new boolean[map.h][map.w];
		for(int z = 0; z < map.h; z++)
			for(int x = 0; x < map.w; x++)
				walkable[z][x] = containsInt(walkables, map.sTypes.getSurfaceIndex(x, z, map.data));
		
		// The long represents the F-cost, G-cost and coords and originTile
		// the originTile specifies which tile one came from to get to this one
		// It is organized in the following fashion:
		// [F-cost][G-cost][x][z]
		// F-cost is an short
		// G-cost is a short
		// x is a byte
		// z is a byte
		// origX is a byte
		// origZ is a byte
		// This should mean, that it prioritizes F-cost, then G-cost, x, z, origX, origZ
		PriorityQueue<Long> nextNodes = new PriorityQueue<>(new Comparator<Long>() {
			@Override
			public int compare(Long o1, Long o2) {
				int res = Long.compareUnsigned(o1, o2);
				if(res == 0)
					return 0;
				else if(res < 0)
					return -1;
				else
					return 1;
			}
		});
		
		for(int i = 0; i < costBuffer.length; i++)
			for(int j = 0; j < costBuffer[i].length; j++)
				costBuffer[i][j] = -1;
		
		costBuffer[sz][sx] = 0L	| (((long) Math.abs(dx-sx)+Math.abs(dz-sz)) << 6*8)
								| (sx << 3*8) | (sz << 2*8) | (sx << 8) | (sz);
		
		nextNodes.add(costBuffer[sz][sx]);
		
		boolean foundPath = false;
		while(!nextNodes.isEmpty() && !foundPath) {
			long node = nextNodes.poll();
			int cx = (int) ((node & (0x00FFL << 3*8)) >> 3*8);
			int cz = (int) ((node & (0x00FFL << 2*8)) >> 2*8);
			int cGCost = (int) ((node & (0x00FFFFL << 4*8)) >> 4*8);
			for(int z = -1; z < 2 && !foundPath; z++) {
				for(int x = ((2+z)%2)-1; x < 2 && !foundPath; x += 2) {
					if((cx+x) < 0 || (cx+x) >= map.w || (cz+z) < 0 || (cz+z) >= map.h)
						continue;
					if(!walkable[cz+z][cx+x])
						continue;
					if((cx+x) == dx && (cz+z) == dz) {
						foundPath = true;
					}
					
					int newGCost = cGCost+1;
					long neighbourNode = costBuffer[cz+z][cx+x];
					if(neighbourNode != -1) {
						if(newGCost >= (int) ((node & (0x00FFFFL << 4*8)) >> 4*8))
							continue;
						nextNodes.remove(neighbourNode);
					}
					int fCost = newGCost + Math.abs(dx-(cx+x))+Math.abs(dz-(cz+z));
					neighbourNode =  0L	| ((0x00FFFFL & fCost) << 6*8) | ((0x00FFFFL & newGCost) << 4*8)
										| ((0x00FFL & (cx+x)) << 3*8) | ((0x00FFL & (cz+z)) << 2*8) | ((0x00FFL & cx) << 8) | (0x00FFL & cz);
					costBuffer[cz+z][cx+x] = neighbourNode;
					nextNodes.add(neighbourNode);
				}
			}
		}
		
		Path res = null; 
		if(foundPath) {
			res = new Path();
			res.tiles = new ArrayList<>();
			int x = dx, z = dz;
			while(x != sx || z != sz) {
				Vector2i v = new Vector2i(x, z);
				res.tiles.add(v);
				long cTile = costBuffer[z][x];
				x = (int) ((cTile & (0x00FFL << 8)) >> 8);
				z = (int) (cTile & 0x00FFL);
			}
			Collections.reverse(res.tiles);
		}
		
		return res;
	}
	
	public Path pathFindDijkstra(int sx, int sz, int dx, int dy) {
		Path res = new Path();
		
		// TODO: Dijkstra pathfinding
		
		return res;
	}

	public SurfaceTypeDescription getSTypes() {
		return map.sTypes;
	}
}
