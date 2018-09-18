package org.rrr.model;

import java.io.File;
import java.util.LinkedList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.rrr.map.MapData;

public class MapMesh {
	
	public Vector3f pos;
	public Matrix4f trans;
	public float scale;
	
	public Texture[] texs;
	
	public int[] inds;
	public float[] points;
	public float[] surfNorms;
	public float[] texPos;
	public float[] surfType;
	public int vao;
	public int indCount;
	public int width, height;
	public int[][] surf;
	private int[][] hMap;
	private MapData data;
	
	
	public static final float HDIV = 14.0f;
	public MapMesh(Loader l, File dir) throws Exception {
		
		File texDir = new File("LegoRR0/World/WorldTextures/RockSplit");
		
		LinkedList<Texture> _texs = new LinkedList<>();
		int ltexs = 0;
		for(File f : texDir.listFiles()) {
			String name = f.getName();
			if(name.matches("[Rr][Oo][Cc][Kk][0-9]{2}.[Bb][Mm][Pp]")) {
				_texs.add(l.getTexture("bmp", f));
				ltexs++;
			}
		}
		texs = new Texture[ltexs];
		for(int i = 0; i < ltexs; i++)
			texs[i] = _texs.pop();
		
		data = MapData.getMapData(dir);
		int w = data.width;
		int h = data.height;
		
		pos = new Vector3f();
		trans = new Matrix4f();
		scale = 50;
		
		this.width = data.width;
		this.height = data.height;
		
		this.points = new float[w*h*5*3];
		this.surfNorms = new float[w*h*3*4];
		this.texPos = new float[this.points.length];
		this.surfType = new float[w*h];
		this.inds = new int[w*h*4*3];
		this.indCount = inds.length;
		
		surf = data.maps[MapData.SURF];
		hMap = data.maps[MapData.HIGH];
		
		for(int i = 0; i < h; i++) {
			for(int j = 0; j < w; j++) {
				setBaseMesh(j, i);
				if(surf[j][i] <= 4)
					createCliffForm(surf, j, i);
			}
		}
		
		for(int i = 0; i < h; i++)
			for(int j = 0; j < w; j++)
				genNormals(j, i);
		
		l.bufferMapMesh(this);
		
	}
	
	private void update() {
		for(int i = 0; i < height; i++)
			for(int j = 0; j < width; j++)
				if(surf[j][i] <= 4)
					createCliffForm(surf, j, i);
	}
	
	private void createCliffForm(int[][] surf, int x, int y) {
		
		
		
	}
	
	private static final float[] BASE_MESH = {
			0, 0, 0,
			0, 0, 1,
			1, 0, 1,
			1, 0, 0,
			0.5f, 0, 0.5f
	};
	private static final int[] BASE_INDS = {
			0, 4, 1,
			1, 4, 2,
			2, 4, 3,
			3, 4, 0
	};
	private void setBaseMesh(int x, int y) {
		int offset = (x+y*width)*3*5;
		for(int i = 0; i < 5; i++) {
			points[offset+i*3+0] = BASE_MESH[i*3+0] + x;
			points[offset+i*3+1] = BASE_MESH[i*3+1];
			points[offset+i*3+2] = BASE_MESH[i*3+2] + y;
		}
	}
	
	private void genNormals(int x, int y) {
		
		int offset = (x+y*width)*3*5;
		int off4 = offset + 4*3;
		for(int i = 0; i < 4; i++) {
			
			offset += 3;
		}
		
	}
	
}
