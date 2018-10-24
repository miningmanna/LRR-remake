package org.rrr.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.joml.Matrix4f;

public class LwsAnimation {
	
	private static HashMap<String, BaseData> bds = new HashMap<>();
	
	public BaseData bd;
	
	public Matrix4f[] transforms;
	public float[] alpha;
	public float time;
	public int frame;
	public int nextframe;
	
	public void step(float delta) {
		if(bd.frames == 0) {
			return;
		}
		time += delta;
		time -= (Math.floor(time/bd.runlen) * bd.runlen);
		frame = (int) Math.floor(time*bd.fps)%bd.frames;
		nextframe = (frame+1)%bd.frames;
		float s = (float) (time*bd.fps - Math.floor(time*bd.fps));
		for(int i = 0; i < bd.lobjects; i++) {
			this.transforms[i].set(bd.frametrans[frame][i]).lerp(bd.frametrans[nextframe][i], s);
			this.alpha[i] = bd.framealpha[frame][i] + (bd.framealpha[nextframe][i] - bd.framealpha[frame][i])*s;
		}
		
	}
	
	public static LwsAnimation getAnimation(File lwsFile, Loader loader, PathConverter converter) throws IOException {
		
		LwsAnimation res = new LwsAnimation();
		
		if(!bds.containsKey(lwsFile.getAbsolutePath())) {
			
			LwsFileData lws = LwsFileData.getLwsFileData(lwsFile);
			
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
					bd.models[i] = loader.getCtexModelFromLwobFile(new File(fName), converter);
				}
			}
			
			for(int i = 0; i < bd.frames; i++) {
				for(int j = 0; j < bd.lobjects; j++) {
					bd.frametrans[i][j] = new Matrix4f(lws.frames[i].transform[j]);
					bd.framealpha[i][j] = lws.frames[i].alpha[j];
				}
			}
			
			bds.put(lwsFile.getAbsolutePath(), bd);
		} else {
			res.bd = bds.get(lwsFile.getAbsolutePath());
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
