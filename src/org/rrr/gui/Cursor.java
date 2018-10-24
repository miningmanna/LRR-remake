package org.rrr.gui;

import org.newdawn.slick.opengl.Texture;
import org.rrr.cfg.LegoConfig.Node;
import org.rrr.model.Loader;

public class Cursor {
	
	public int x, y;
	public Texture base;
	public CursorAnimation[] animations;
	public int vao;
	
	public Cursor() {
		
	}
	
	public void init(Node cfg, Loader l) {
		
		for(String key : cfg.getValueKeys()) {
			
			System.out.println(key);
			
		}
		
	}
	
	public static class CursorAnimation {
		
		public Texture[] texs;
		public int frame;
		
	}
	
}
