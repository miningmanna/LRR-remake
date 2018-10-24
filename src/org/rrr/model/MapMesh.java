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
				setVertY(j, i, 0, hMap[j][i]/HDIV);
				tex[j][i] = getTexIndex(j, i);
			}
		}
		
		for(int i = 0; i < h; i++) {
			for(int j = 0; j < w; j++) {
				if(surf[j][i] > 4)
					genDiagonal(j, i, true);
				else
					createCliffForm(j, i);
				genNormals(j, i);
			}
		}
		
		l.bufferMapMesh(this);
	}
	
	private void update() {
		for(int i = 0; i < height; i++)
			for(int j = 0; j < width; j++)
				if(surf[j][i] <= 4)
					createCliffForm(j, i);
	}
	
	private void createCliffForm(int x, int y) {
		if(x >= width-1 || y >= height-1)
			return;
		float[] temp = {
				hMap[x][y] / HDIV,
				hMap[x][y+1] / HDIV,
				hMap[x+1][y+1] / HDIV,
				hMap[x+1][y] / HDIV
		};
		boolean isCave = false;
		boolean zeroTwo = true;
		for(int i = 0; i < 4; i++) {
			
			if(!isGround(x, y, i)) {
				setVertY(x, y, i, temp[i]+1);
			} else {
				isCave = true;
			}
			
		}
		
		if(isCave) {
			
		} else {
			System.out.println("not cave");
		}
	}
	
	public boolean isGround(int x, int y, int vert) {
		
		switch (vert) {
		case 1:
			return isGround(x, y+1, 0);
		case 2:
			return isGround(x+1, y+1, 0);
		case 3:
			return isGround(x+1, y, 0);
		}
		
		if(x >= width || y >= height || x < 0 || y < 0)
			return false;
		
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 2; j++) {
				if(x-i >= 0) {
					if(y-j >= 0) {
						int s = surf[x-i][y-j];
						if(s >= 5)
							return true;
					}
				}
			}
		}
		
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
			0, 0, 0,
			0, 0, 1,
			0.5f, 0, 0.5f,
			0, 0, 1,
			1, 0, 1,
			0.5f, 0, 0.5f,
			1, 0, 1,
			1, 0, 0,
			0.5f, 0, 0.5f,
			1, 0, 0,
			0, 0, 0,
			0.5f, 0, 0.5f
	};
	
	private void setBaseMesh(int x, int y) {
		
		int off = (x+y*width)*4*3;
		for(int i = 0; i < 4*3; i++) {
			inds[off+i] = off+i;
			points[(off+i)*3]	= BASE_MESH[i*3]  +x;
			points[(off+i)*3+1]	= BASE_MESH[i*3+1];
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
		
		boolean isCave = caveMap[x][y] != 0;
		boolean hasGround = false;
		for(int i = 0; i < 4; i++) {
			if(isGround(x, y, i)) {
				hasGround = true;
				break;
			}
		}
		
		if(x == 11 && y == 5)
			System.out.println("Has Ground: " + hasGround);
		
		isCave = isCave || hasGround;
		
		if(hasGround) {
			
			if(surf[x][y] < 5) {
				
				// TODO: Cliff textures
				
				int state = 0;
				int type = 5;
				switch (surf[x][y]) {
				case 3:
				case 4:
				case 5:
					type = surf[x][y]-2;
					break;
				default:
					type = 5;
					break;
				}
				
				return type+state*10;
				
			} else {
				
				switch (surf[x][y]) {
				case 5:
					return 0;
				case 6:
					return 46;
				default:
					return 0;
				}
				
			}
			
		}
		return 70;
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
