package org.rrr.assets.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.joml.Matrix4f;
import org.rrr.assets.AssetManager;
import org.rrr.assets.tex.TexLoader;

public class LwsAnimation {
	
	private static HashMap<String, BaseData> bds = new HashMap<>();
	
	public BaseData bd;
	
	public Matrix4f[] transforms;
	public float[] alpha;
	public float time;
	public int frame;
	public int nextframe;
	public boolean loop;
	
	public void step(float delta) {
		if(bd.frames == 0) {
			return;
		}
		time += delta;
		if(loop && (Math.floor(time/bd.runlen) > 0)) {
			time -= (Math.floor(time/bd.runlen) * bd.runlen);
		}
		frame = (int) Math.floor(time*bd.fps)%bd.frames;
		if(frame != bd.frames-1) {
			nextframe = (frame+1)%bd.frames;
		} else {
			if(loop)
				nextframe = (frame+1)%bd.frames;
			else
				nextframe = frame;
		}
		float s = (float) (time*bd.fps - Math.floor(time*bd.fps));
		for(int i = 0; i < bd.lobjects; i++) {
			this.transforms[i].set(bd.frametrans[frame][i]).lerp(bd.frametrans[nextframe][i], s);
			this.alpha[i] = bd.framealpha[frame][i] + (bd.framealpha[nextframe][i] - bd.framealpha[frame][i])*s;
		}
		
	}
	
	public static LwsAnimation getAnimation(String path, AssetManager am, PathConverter converter) throws IOException {
		
		path = path.toUpperCase();
		
		LwsAnimation res = new LwsAnimation();
		res.loop = true;
		
		if(!bds.containsKey(path)) {
			InputStream lwsIn = am.getAsset(path);
			LwsFileData lws = LwsFileData.getLwsFileData(lwsIn);
			lwsIn.close();
			
			BaseData bd = new BaseData();
			res.bd = bd;
			
			
			bd.fps = lws.framesPerSecond;
			bd.firstframe = lws.firstFrame;
			bd.lastframe = lws.lastFrame;
			bd.frames = bd.lastframe - bd.firstframe;
			bd.framestep = lws.frameStep;
			bd.runlen = (bd.frames) / bd.fps;
			bd.lobjects = lws.objFiles.length;
			bd.models = new CTexModel[bd.lobjects];
			bd.frametrans = new Matrix4f[bd.frames][bd.lobjects];
			bd.framealpha = new float[bd.frames][bd.lobjects];
			
			for(int i = 0; i < bd.lobjects; i++) {
				
				if(lws.objFiles[i] != null) {
					String fName = converter.convert(new File(lws.objFiles[i]).getName());
					if(fName == null)
						continue;
					InputStream objIn = am.getAsset(fName);
					bd.models[i] = am.getMLoader().getCtexModelFromLwobFile(fName, objIn, converter, am);
					objIn.close();
				}
			}
			
			for(int i = 0; i < bd.frames; i++) {
				for(int j = 0; j < bd.lobjects; j++) {
					bd.frametrans[i][j] = new Matrix4f(lws.frames[i].transform[j]);
					bd.framealpha[i][j] = lws.frames[i].alpha[j];
				}
			}
			
			bds.put(path, bd);
		} else {
			res.bd = bds.get(path);
		}
		res.transforms = new Matrix4f[res.bd.lobjects];
		for(int i = 0; i < res.transforms.length; i++)
			res.transforms[i] = new Matrix4f();
		res.alpha = new float[res.bd.lobjects];
		
		return res;
		
	}
	
	public LwsAnimation makeFromBD(BaseData bd) {
		
		LwsAnimation res = new LwsAnimation();
		res.bd = bd;
		res.alpha = new float[bd.lobjects];
		res.transforms = new Matrix4f[bd.lobjects];
		for(int i = 0; i < bd.lobjects; i++)
			res.transforms[i] = new Matrix4f().identity();
		
		return res;
	}
	
//	public static LwsAnimation getAnimation(LwsFileData lws, Loader loader, PathConverter converter) throws IOException {
//		
//		LwsAnimation res = new LwsAnimation();
//		BaseData bd = new BaseData();
//		res.bd = bd;
//		
//		bd.fps = lws.framesPerSecond;
//		bd.firstframe = lws.firstFrame;
//		bd.lastframe = lws.lastFrame;
//		bd.frames = bd.lastframe - bd.firstframe;
//		bd.framestep = lws.frameStep;
//		bd.runlen = (bd.frames) / bd.fps;
//		bd.lobjects = lws.objFiles.length;
//		res.transforms = new Matrix4f[bd.lobjects];
//		for(int i = 0; i < res.transforms.length; i++)
//			res.transforms[i] = new Matrix4f();
//		res.alpha = new float[bd.lobjects];
//		bd.models = new CTexModel[bd.lobjects];
//		bd.frametrans = new Matrix4f[bd.frames][bd.lobjects];
//		bd.framealpha = new float[bd.frames][bd.lobjects];
//		
//		for(int i = 0; i < bd.lobjects; i++) {
//			
//			if(lws.objFiles[i] != null)
//				bd.models[i] = loader.getCtexModelFromLwobFile(new File(converter.convert(new File(lws.objFiles[i]).getName())), converter);
//			
//		}
//		
//		for(int i = 0; i < bd.frames; i++) {
//			for(int j = 0; j < bd.lobjects; j++) {
//				bd.frametrans[i][j] = new Matrix4f(lws.frames[i].transform[j]);
//				bd.framealpha[i][j] = lws.frames[i].alpha[j];
//			}
//		}
//		
//		return res;
//		
//	}
	
	public static class BaseData {
		
		public float fps;
		public int firstframe;
		public int lastframe;
		public int frames;
		public int framestep;
		public int lobjects;
		public Matrix4f[][] frametrans;
		public float[][] framealpha;
		public CTexModel[] models;
		public float runlen;
		
	}
	
}
