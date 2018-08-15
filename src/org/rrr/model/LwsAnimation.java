package org.rrr.model;

import java.io.File;
import java.io.IOException;

import org.joml.Matrix4f;

public class LwsAnimation {
	
	public float fps;
	public int firstframe;
	public int lastframe;
	public int frames;
	public int framestep;
	public int lobjects;
	public Matrix4f[][] frametrans;
	public CTexModel[] models;
	public Matrix4f[] transforms;
	public float time, runlen;
	public int frame;
	public int nextframe;
	
	public void step(float delta) {
		time += delta;
		while(time > runlen)
			time -= runlen;
		frame = (int) Math.floor(time*fps)%frames;
		nextframe = (int) Math.ceil(time*fps)%frames;
		float s = (float) (time*fps - Math.floor(time*fps));
		for(int i = 0; i < lobjects; i++)
			this.transforms[i].set(frametrans[frame][i]).lerp(frametrans[nextframe][i], s);
		
	}
	
	public static LwsAnimation getAnimation(LwsFileData lws, Loader loader, PathConverter converter) throws IOException {
		
		LwsAnimation res = new LwsAnimation();
		
		res.fps = lws.framesPerSecond;
		res.firstframe = lws.firstFrame;
		res.lastframe = lws.lastFrame;
		res.frames = res.lastframe - res.firstframe;
		res.framestep = lws.frameStep;
		res.runlen = (res.frames) / res.fps;
		res.lobjects = lws.objFiles.length;
		res.transforms = new Matrix4f[res.lobjects];
		for(int i = 0; i < res.transforms.length; i++)
			res.transforms[i] = new Matrix4f();
		res.models = new CTexModel[res.lobjects];
		res.frametrans = new Matrix4f[res.lastframe-res.firstframe][res.lobjects];
		
		for(int i = 0; i < res.lobjects; i++) {
			
			if(lws.objFiles[i] != null)
				res.models[i] = loader.getCtexModelFromLwobFile(new File(converter.convert(new File(lws.objFiles[i]).getName())), converter);
			
		}
		
		for(int i = 0; i < res.frames; i++)
			for(int j = 0; j < res.lobjects; j++)
				res.frametrans[i][j] = new Matrix4f(lws.frames[i].transform[j]);
		
		return res;
		
	}
	
}
