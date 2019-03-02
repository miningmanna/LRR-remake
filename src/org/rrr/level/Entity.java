package org.rrr.level;

import java.io.File;
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
		System.out.println(e);
		e.bd = bd;
		e.anims = new LwsAnimation[bd.animFiles.length];
		//for(int i = 0; i < e.anims.length; i++)
		//	e.anims[i] = am.getAnimation(bd.animFiles[i]);
		
		return e;
	}
	
	public static void loadEntity(File dir, String name) {
		
		BaseData bd = new BaseData();
		
		int lFiles = 0;
		LinkedList<File> files = new LinkedList<>();
		for(File f : dir.listFiles()) {
			
			String fName = f.getName();
			if(fName.length() < 5)
				continue;
			fName = fName.substring(fName.length()-3, fName.length());
			if(!fName.equalsIgnoreCase("lws"))
				continue;
			
			System.out.println("Adding: " + f.getName());
			files.add(f);
			lFiles++;
			
		}
		
		bd.name = name;
		bd.animFiles = new File[lFiles];
		for(int i = 0; i < lFiles; i++)
			bd.animFiles[i] = files.pop();
		
		bds.put(name, bd);
		
	}
	
	public static class BaseData {
		public String name;
		public File[] animFiles;
	}

}
