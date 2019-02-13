package org.rrr.assets.tex;

import org.newdawn.slick.opengl.Texture;

public class FLHAnimation {
	
	public int frame;
	public boolean justFinished;
	public BaseData data;
	
	public FLHAnimation(BaseData data) {
		this.data = data;
		frame = 0;
		justFinished = false;
	}
	
	public void step(int frames) {
		frame = (frame + frames)%data.frames.length;
		justFinished = (frame == 0);
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
