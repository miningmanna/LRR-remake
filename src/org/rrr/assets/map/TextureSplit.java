package org.rrr.assets.map;

import org.joml.Vector2i;
import org.newdawn.slick.opengl.Texture;

public class TextureSplit {
	
	public Texture atlas;
	public Texture[] texs;
	public int w, h;
	
	public void genAtlas() {
		
	}
	
	public int toIndex(Vector2i v) {
		return v.y*w+v.x;
	}
	public int toIndex(int x, int z) {
		return z*w+x;
	}
}
