package org.rrr.assets.tex;

import org.newdawn.slick.opengl.Texture;

public class FLHAnimation {
	
<<<<<<< HEAD
	public float time, frameTime, tLength;
=======
>>>>>>> b2dd22ed37680fa03dab0285ee7a034ff4a6873d
	public int frame;
	public boolean justFinished;
	public BaseData data;
	
<<<<<<< HEAD
	public FLHAnimation(BaseData data, float fps) {
		this.data = data;
		frame = 0;
		justFinished = false;
		frameTime = 1.0f/fps;
		time = 0;
		tLength = data.frames.length * frameTime;
	}
	
	public void step(float dt) {
		time += dt;
		if(time > tLength) {
			time %= tLength;
			justFinished = true;
		} else {
			justFinished = false;
		}
		frame = (int) Math.floor(time/frameTime);
=======
	public FLHAnimation(BaseData data) {
		this.data = data;
		frame = 0;
		justFinished = false;
	}
	
	public void step(int frames) {
		frame = (frame + frames)%data.frames.length;
		justFinished = (frame == 0);
>>>>>>> b2dd22ed37680fa03dab0285ee7a034ff4a6873d
	}
	
	public static BaseData getBaseData(TexLoader tLoader, FLHFile file) {
		
		BaseData bd = new BaseData();
		bd.w = file.width;
		bd.h = file.height;
		bd.frames = new Texture[file.lframes];
		for(int i = 0; i < file.lframes; i++) {
			bd.frames[i] = tLoader.getTexture(file.frames.get(i));
		}
		
		return bd;
	}
	
	public static class BaseData {
		
		public int w, h;
		public String fName;
		public Texture[] frames;
		
	}
}
