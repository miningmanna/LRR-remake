package org.rrr.model;

import java.util.Random;

import org.rrr.map.MapData;

public class MapMesh {
	
	public int[] inds;
	public float[] points;
	public float[] surfType;
	public int vao;
	public int indCount;
	public int width, height;
	private int[][] surf;
	private int[][] hMap;
	
	public MapMesh(Loader l, MapData data, int w, int h) {
		
		this.width = w;
		this.height = h;
		
		this.points = new float[w*h*5*3];
		this.surfType = new float[w*h*5];
		this.inds = new int[w*h*4*3];
		this.indCount = inds.length;
		
		surf = data.maps[MapData.SURF];
		hMap = data.maps[MapData.HIGH];
		
		for(int i = 0; i < h; i++) {
			for(int j = 0; j < w; j++) {
				setBaseMeshAndSurf(j, i, surf[j][i]);
				if(surf[j][i] <= 4) {
					checkNeighbours(surf, hMap, j, i);
				}
			}
		}
		
		l.bufferMapMesh(this);
		
	}
	
	private void update() {
		for(int i = 0; i < height; i++)
			for(int j = 0; j < width; j++)
				if(surf[j][i] <= 4)
					checkNeighbours(surf, hMap,j, i);
	}
	
	private void checkNeighbours(int[][] surf, int[][] hMap,int x, int y) {
		int mHeight = hMap[x][y];
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
	Random r = new Random();
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
		}
		for(int i = 0; i < BASE_INDS.length; i++) {
			inds[offset*4*3 + i] = offset*5 + BASE_INDS[i];
		}
	}
	
}
