package org.rrr.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.rrr.cfg.LegoConfig.Node;
import org.rrr.map.MapData;

public class MapMesh {
	
	public Vector3f pos;
	public Matrix4f trans;
	public float scale;
	
	public HashMap<Integer, Texture> texs;
	
	public int[] inds;
	public float[] points;
	public float[] norms;
	public float[] texPos;
	public float[] surfType;
	public int vao;
	public int indCount;
	public int width, height;
	private int[][] surf;
	private int[][] hMap;
	private int[][] caveMap;
	public int[][] tex;
	private MapData data;
	
	
	public static final float HDIV = 6.0f;
	public MapMesh(Loader l, File dir) throws Exception {
		init(l, dir);
	}
	
	public MapMesh(Loader loader, Node node) throws Exception {
		
		String surfMapPath = node.getValue("SurfaceMap");
		File dir = new File("LegoRR0/" + surfMapPath).getParentFile();
		System.out.println(dir);
		init(loader, dir);
		
	}
	
	private void init(Loader l, File dir) throws Exception {
		File texDir = new File("LegoRR0/World/WorldTextures/RockSplit");
		
		texs = new HashMap<>();
		for(File f : texDir.listFiles()) {
			String name = f.getName();
			if(name.matches("[Rr][Oo][Cc][Kk][0-9]{2}.[Bb][Mm][Pp]")) {
				System.out.println(name);
				String num = name.split("\\.")[0];
				num = num.substring(num.length()-2, num.length());
				System.out.println(num);
				texs.put(Integer.parseInt(num), l.getTexture("bmp", f));
			}
		}
		
		data = MapData.getMapData(dir);
		int w = data.width;
		int h = data.height;
		
		pos = new Vector3f();
		trans = new Matrix4f();
		scale = 40;
		
		this.width = data.width;
		this.height = data.height;
		
		this.points = new float[w*h*4*3*3];
		this.norms = new float[points.length];
		this.texPos = new float[w*h*4*3*2];
		this.surfType = new float[w*h];
		this.inds = new int[w*h*4*3];
		this.indCount = inds.length;
		
		surf	= copyArray(data.maps[MapData.SURF]);
		hMap	= copyArray(data.maps[MapData.HIGH]);
		caveMap	= copyArray(data.maps[MapData.DUGG]);
		tex = new int[surf.length][surf[0].length];
		
		for(int i = 0; i < h; i++) {
			for(int j = 0; j < w; j++) {
				setBaseMesh(j, i);
				setVertY(j, i, 3, 1.0f+hMap[i][j]/HDIV);
				tex[i][j] = 70;
			}
		}
		
		for(int i = 0; i < h; i++) {
			for(int j = 0; j < w; j++) {
				if(caveMap[i][j] == 1) {
					if(surf[i][j] < 5) {
						System.out.println("CAVE BUT NOT GROUND");
						surf[i][j] = 5;
					}
					for(int g = 0; g < 4; g++) {
						setVertY(j, i, g, hMap[i][j]/HDIV);
					}
					for(int x = -1; x < 2; x++) {
						for(int y = -1; y < 2; y++) {
							setTexIndex(j+x, i+y);
						}
					}
				}
			}
		}
		
		// Points are defined. Calculate normals
		for(int i = 0; i < h; i++) {
			for(int j = 0; j < w; j++) {
				genDiagonal(j, i, true);
				genNormals(j, i);
			}
		}
		
		
		l.bufferMapMesh(this);
	}
	

	private void createCliffForm(int x, int y) {
		
		
		
	}
	
	public boolean isGround(int x, int y, int vert) {
		
		return false;
	}
	
	private void setVertY(int x, int y, int vert, float vy) {
		
		if(x >= width || y >= height)
			return;
		
		switch (vert) {
		case 0:
			setSingleVertY(x, y, 0, vy);
			if(x > 0) {
				setSingleVertY(x-1, y, 3, vy);
				if(y > 0)
					setSingleVertY(x-1, y-1, 2, vy);
			}
			if(y > 0)
				setSingleVertY(x, y-1, 1, vy);
			break;
		case 4:
			setSingleVertY(x, y, vert, vy);
			break;
		default:
			if(vert >= 2)
				x += 1;
			y += vert % 2;
			
			setVertY(x, y, 0, vy);
				
			break;
		}
		
	}
	
	public float getVertY(int x, int y, int vert) {
		return points[((y*width+x)*4+vert)*9 + 1];
	}
	
	private void setSingleVertY(int x, int y, int vert, float vy) {
		
		int off = (y*width+x)*4*9;
		if(vert == 4) {
			for(int i = 0; i < 4; i++) {
				points[off+(2+i*3)*3+1] = vy;
			}
		} else {
			points[off+vert*3*3+1] = vy;
			points[off+((vert*3-2+3*4)%(3*4))*3+1] = vy;
		}
		
	}
	
	private static final float[] BASE_MESH = {
			0,		0,		0,
			0,		0,		1,
			0.5f,	0,		0.5f,
			0,		0,		1,
			1,		0,		1,
			0.5f,	0,		0.5f,
			1,		0,		1,
			1,		0,		0,
			0.5f,	0,		0.5f,
			1,		0,		0,
			0,		0,		0,
			0.5f,	0,		0.5f
	};
	
	private void setBaseMesh(int x, int y) {
		
		int off = (x+y*width)*4*3;
		for(int i = 0; i < 4*3; i++) {
			inds[off+i] = off+i;
			points[(off+i)*3]	= BASE_MESH[i*3]  +x;
			//points[(off+i)*3+1]	= BASE_MESH[i*3+1];
			points[(off+i)*3+2]	= BASE_MESH[i*3+2]+y;
			texPos[(off+i)*2]	= BASE_MESH[i*3];
			texPos[(off+i)*2+1]	= BASE_MESH[i*3+2];
		}
		
	}
	
	private void genNormals(int x, int y) {
		
		int off = (y*width+x)*4*9;
		for(int i = 0; i < 4; i++) {
			
			float x1 = points[off];
			float y1 = points[off+1];
			float z1 = points[off+2];
			float x2 = points[off+3];
			float y2 = points[off+4];
			float z2 = points[off+5];
			float xm = points[off+6];
			float ym = points[off+7];
			float zm = points[off+8];
			x1 -= xm;
			x2 -= xm;
			y1 -= ym;
			y2 -= ym;
			z1 -= zm;
			z2 -= zm;
			float rx = (y1*z2)-(y2*z1);
			float ry = (z1*x2)-(z2*x1);
			float rz = (x1*y2)-(x2*y1);
			for(int j = 0; j < 3; j++) {
				norms[off+j*3]		= rx;
				norms[off+j*3+1]	= ry;
				norms[off+j*3+2]	= rz;
			}
			
			off += 9;
			
		}
		
	}
	
	private int getTexIndex(int x, int y) {
		
		int s = surf[y][x];
		if(s < 5) {
			return 6-s;
		}
		
		switch (s) {
		case 5:
			return 0;
		case 6:
			return 46;
		default:
			break;
		}
		
		return 70;
	}
	
	private void setTexIndex(int x, int y) {
		
		if(y < 0 || x < 0 || y >= height || x >= width)
			return;
		
		tex[y][x] = getTexIndex(x, y);
		
	}
	
	public void genDiagonal(int x, int y, boolean zeroTwo) {
		float y1, y2;
		if(zeroTwo) {
			y1 = getVertY(x, y, 0);
			y2 = getVertY(x, y, 2);
		} else {
			y1 = getVertY(x, y, 1);
			y2 = getVertY(x, y, 3);
		}
		setVertY(x, y, 4, y1 + (y2-y1)/2.0f);
	}
	
	private static int[][] copyArray(int [][] orig) {
		int[][] res = new int[orig.length][];
		for(int i = 0; i < res.length; i++) {
			res[i] = new int[orig[i].length];
			System.arraycopy(orig[i], 0, res[i], 0, res[i].length);
		}
		return res;
	}
	
}
