package org.rrr.assets.model;

import org.joml.Vector3f;
import org.newdawn.slick.opengl.Texture;

public class CTexModel {
	
	public int vao;
	public int[] opaque;
	public int[] translucent;
	public int[] surfStart;
	public int[] surfLen;
	public int[] ltexs;
	public int texIndex;
	public Texture[][] texs;
	public Vector3f[] alpha;
	public boolean[] additive;
	public boolean[] doubleSided;
	
}
