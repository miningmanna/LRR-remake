package org.rrr.level;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.rrr.assets.AssetManager;
import org.rrr.assets.model.LwsAnimation;

public class Entity {
	
	private static AssetManager am;
	private static HashMap<String, BaseData> bds = new HashMap<>();
	
	public BaseData bd;
	public Vector3f pos;
	public Matrix4f rot;
	public int currentAnimation;
	public LwsAnimation[] anims;
	public LuaValue script;
	public LuaTable mVars;
	public float colRadius, colHeight;
	public int[] walkables;
	public Path curPath;
	public int curPathStep;
	
	public Entity() {
		pos = new Vector3f();
		rot = new Matrix4f().identity();
		mVars = new LuaTable();
	}
	
	public void step(float delta) {
		anims[currentAnimation].step(delta);
	}
	
	public static void setAssetManager(AssetManager am) {
		Entity.am = am;
	}
	
	public static Entity getEntity(String type) throws IOException {
		BaseData bd = bds.get(type);
		if(bd == null)
			return null;
		
		Entity e = new Entity();
		e.bd = bd;
		e.anims = new LwsAnimation[bd.animFiles.length];
		for(int i = 0; i < e.anims.length; i++)
			e.anims[i] = am.getAnimation(bd.animFiles[i]);
		
		return e;
	}
	
	public static void loadEntity(String dir, String name) {
		BaseData bd = new BaseData();
		bd.name = name;
		String[] dirFiles = am.getAllSubFiles(dir);
		LinkedList<String> _animFiles = new LinkedList<>();
		for(String f : dirFiles)
			if(f.endsWith("LWS"))
				_animFiles.add(f);
		bd.animFiles = new String[_animFiles.size()];
		for(int i = 0; i < bd.animFiles.length; i++)
			bd.animFiles[i] = _animFiles.pop();
		bds.put(name, bd);
		
	}
	
	public static class BaseData {
		public String name;
		public String[] animFiles;
	}

}
