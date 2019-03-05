package org.rrr.assets.model;

import org.rrr.assets.map.TextureSplit;

public class MapMesh {
	
	// Render data
	public int		vao;
	public int		vertVbo, nVertVbo, tVertVbo;
	public float[]	verts;
	public float[]	nVerts;
	public float[]	tVerts;
	
	public float[]	 tRotation;
	public int[]	 tex;
	public TextureSplit split;
	
	public int[]	inds;
	public int 	indCount;
	
}
