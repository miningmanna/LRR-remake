package org.rrr.model;

import java.io.File;
import java.util.LinkedList;
import java.util.Random;

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
	public float[] texPos;
	public float[] surfType;
	public int vao;
	public int indCount;
	public int width, height;
	public int[][] surf;
	private int[][] hMap;
	private MapData data;
	
	
	public static final float HDIV = 10.0f;
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
		this.texPos = new float[this.points.length];
		this.surfType = new float[w*h*5];
		this.inds = new int[w*h*4*3];
		this.indCount = inds.length;
		
		surf = data.maps[MapData.SURF];
		hMap = data.maps[MapData.HIGH];
		
		for(int i = 0; i < h; i++) {
			for(int j = 0; j < w; j++) {
				setBaseMeshAndSurf(j, i, surf[j][i]);
				if(surf[j][i] <= 4) {
					checkNeighbours(surf, j, i);
				}
				int offset = (i*w+j)*15;
				points[offset+(0*3)+1] += hMap[j][i]/HDIV;
				if(i != h-1)points[offset+(1*3)+1] += hMap[j][i+1]/HDIV;
				if(j != w-1) {
					points[offset+(3*3)+1] += hMap[j+1][i]/HDIV;
					if(i != h-1)
						points[offset+(4*3)+1] += hMap[j+1][i+1]/HDIV;
				}
				
				points[offset+(2*3)+1] += hMap[j][i]/HDIV;
			}
		}
		
		l.bufferMapMesh(this);
		
	}
	
	private void update() {
		for(int i = 0; i < height; i++)
			for(int j = 0; j < width; j++)
				if(surf[j][i] <= 4)
					checkNeighbours(surf, j, i);
	}
	
	private void checkNeighbours(int[][] surf, int x, int y) {
		int offset = (y*width + x)*5;
		boolean slope = false;
		if(y != 0) {
			if(x != 0) {
				if(surf[x-1][y-1] > 4) {
					slope = true;
					points[(offset + 0)*3 + 1] = 0;
				}
			}
			if(surf[x][y-1] > 4) {
				slope = true;
				points[(offset + 0)*3 + 1] = 0;
				points[(offset + 3)*3 + 1] = 0;
			}
		}
		if(x != 0) {
			if(y != height-1) {
				if(surf[x-1][y+1] > 4) {
					slope = true;
					points[(offset + 1)*3 + 1] = 0;
				}
			}
			if(surf[x-1][y] > 4) {
				slope = true;
				points[(offset + 0)*3 + 1] = 0;
				points[(offset + 1)*3 + 1] = 0;
			}
		}
		if(y != height-1) {
			if(x != width-1) {
				if(surf[x+1][y+1] > 4) {
					slope = true;
					points[(offset + 4)*3 + 1] = 0;
				}
			}
			if(surf[x][y+1] > 4) {
				slope = true;
				points[(offset + 1)*3 + 1] = 0;
				points[(offset + 4)*3 + 1] = 0;
			}
		}
		if(x != width-1) {
			if(y != 0) {
				if(surf[x+1][y-1] > 4) {
					slope = true;
					points[(offset + 3)*3 + 1] = 0;
				}
			}
			if(surf[x+1][y] > 4) {
				slope = true;
				points[(offset + 4)*3 + 1] = 0;
				points[(offset + 3)*3 + 1] = 0;
			}
		}
		if(slope) {
			points[(offset + 2)*3 + 1] = 0.5f;
		}
	}
	
	private static final float[] BASE_MESH = {
			0, 0, 0,
			0, 0, 1,
			0.5f, 0, 0.5f,
			1, 0, 0,
			1, 0, 1
	};
	private static final int[] BASE_INDS = {
			0, 2, 1,
			1, 2, 4,
			4, 2, 3,
			3, 2, 0
	};
	private void setBaseMeshAndSurf(int x, int y, int surf) {
		int offset = y*width + x;
		for(int i = 0; i < 5; i++) {
			surfType[offset*5 + i] = surf;
			points[(offset*5 + i)*3 + 0] = x+BASE_MESH[i*3 + 0];
			if(surf > 4)
				points[(offset*5 + i)*3 + 1] = BASE_MESH[i*3 + 1];
			else
				points[(offset*5 + i)*3 + 1] = 1+BASE_MESH[i*3 + 1];
				
			points[(offset*5 + i)*3 + 2] = y+BASE_MESH[i*3 + 2];
			texPos[(offset*5 + i)*3 + 0] = BASE_MESH[i*3 + 0];
			texPos[(offset*5 + i)*3 + 1] = BASE_MESH[i*3 + 2];
		}
		for(int i = 0; i < BASE_INDS.length; i++) {
			inds[offset*4*3 + i] = offset*5 + BASE_INDS[i];
		}
	}
	
}
