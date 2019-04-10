package org.rrr.assets.map;

import java.util.ArrayList;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.map.SurfaceTypeDescription.Surface;
import org.rrr.assets.model.MapMesh;
import org.rrr.assets.model.ModelLoader;

public class Map {
	
	public int w;
	public int h;
	public MapData data;
	public MapMesh mesh;
	public SurfaceTypeDescription sTypes;
	public float unitDist = 40;
	public int[][] surfaces;
	private boolean[][] utilBuffer;
	
	private ModelLoader mLoader;
	
	public Map(AssetManager am, Node cfg) throws Exception {
		
		mLoader = am.getMLoader();
		
		sTypes = am.getSurfaceTypeDescription(cfg.getOptValue("SurfaceTypeDefinition", "Standard"));
		data = MapData.getMapData(am, cfg);
		w = data.width;
		h = data.height;
		utilBuffer = new boolean[h][w];
		mesh = new MapMesh();
		mesh.split = am.getTexSplit(cfg.getOptValue("TextureSet", "Rock"));
		initMapMesh();
		am.getMLoader().loadMapMeshIntoVao(mesh);
		
	}
	
	public void updateRot(int x, int z) {
		Vector3i tAtlasPos = sTypes.getAtlasPos(x, z, data);
		int tex = mesh.split.toIndex(tAtlasPos.x, tAtlasPos.y);
	}
	
	private void initMapMesh() {
		
		float[] baseVerts = {
				0, 0, 0,
				1, 0, 0,
				0.5f, 0, 0.5f,
				1, 0, 0,
				1, 0, 1,
				0.5f, 0, 0.5f,
				1, 0, 1,
				0, 0, 1,
				0.5f, 0, 0.5f,
				0, 0, 1,
				0, 0, 0,
				0.5f, 0, 0.5f
		};
		
		int[][] high = data.maps[MapData.HIGH];
		
		mesh.inds = new int[w * h * baseVerts.length/3];
		mesh.verts = new float[mesh.inds.length*3];
		mesh.nVerts = new float[mesh.inds.length*3];
		mesh.tVerts = new float[mesh.inds.length*2];
		mesh.wave = new float[mesh.inds.length*4];
		mesh.t = new float[mesh.inds.length];
		
		for(int i = 0; i < mesh.inds.length; i++)
			mesh.inds[i] = i;
		
		int ind = 0;
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				
				System.arraycopy(baseVerts, 0, mesh.verts, ind*3, baseVerts.length);
				
				for(int i = 0; i < 12; i++) {
					mesh.verts[(ind+i)*3]	+= x;
					mesh.verts[(ind+i)*3+2]	+= z;
					for(int j = 0; j < 3; j++)
						mesh.verts[(ind+i)*3+j] *= unitDist;
					
					mesh.tVerts[(ind+i)*2]   = baseVerts[i*3];
					mesh.tVerts[(ind+i)*2+1] = baseVerts[i*3+2];
				}
				
				ind += 12;
			}
		}
		
		fillDirt();
		generateMeshDetails();
	}
	
	private void generateMeshDetails() {
		
		float unitDist = 40;
		int[][] high = data.maps[MapData.HIGH];
		
		expandCaves();
		
		for(int z = 0; z <= h; z++) {
			for(int x = 0; x <= w; x++) {
				float height = 0; // Get the height for tile -1 on X axis because im retarded
				int hx = x-1, hz = z;
				if(hx == -1 || hx == w-1) {
					height = 0;
				} else {
					if(z == h) {
						height = 0;
					} else {
						height = high[hz][hx]/7.0f;
					}
				}
				boolean isCave = false;
				if(x != w && z != h) {
					isCave = data.maps[MapData.DUGG][z][x] != 2;
				}
				
				if(isCave) {
					if(isAtGroundlevel(x, z))
						setY(x, z, 0, height*unitDist);
					else
						setY(x, z, 0, unitDist+height*unitDist); // TODO: load roofheight
				} else {
					setY(x, z, 0, unitDist+height*unitDist); // TODO: load roofheight
				}
			}
		}
		
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				
				boolean zeroTwo = true;
				
				if(data.maps[MapData.DUGG][z][x] != 2) {
					Vector4f wave = sTypes.getWave(x, z, data);
					int off = (z*w+x)*12*4;
					for(int i = 0; i < 12; i++) {
						mesh.wave[off+i*4]		= wave.x;
						mesh.wave[off+i*4+1]	= wave.y;
						mesh.wave[off+i*4+2]	= wave.z;
						mesh.wave[off+i*4+3]	= wave.w;
					}
					// TODO: fix map shader to work with an angled wave (dist from orthagonal line to point)
					
					Vector3i tAtlasPos = sTypes.getAtlasPos(x, z, data);
					applyTextureFromAtlas(x, z, tAtlasPos.x, tAtlasPos.y, tAtlasPos.z);
					
					boolean[] groundLevels = new boolean[] {
							isAtGroundlevel(x, z),
							isAtGroundlevel(x+1, z),
							isAtGroundlevel(x+1, z+1),
							isAtGroundlevel(x, z+1)
					};
					int groundPoints = 0;
					int firstAfterZero = -1;
					for(int i = 0; i < 4; i++) {
						if(groundLevels[i]) {
							groundPoints++;
							if(!groundLevels[(i+3)%4])
								firstAfterZero = i;
						}
					}
					
					switch(groundPoints) {
					case 0:
						break;
					case 1:
					case 2:
						zeroTwo = firstAfterZero == 0 || firstAfterZero == 2;
						break;
					case 3:
						zeroTwo = (firstAfterZero+1)%4 == 0 || (firstAfterZero+1)%4 == 2;
						break;
					}
				} else {
					applyTextureFromAtlas(x, z, sTypes.roof.x, sTypes.roof.y, 0);
				}
				
				triangulateTile(x, z, zeroTwo);
				
				calcNormals(x, z);
				
			}
		}
	}
	
	private void updateWaves(float dt) {
		int start = -1, stop = -1;
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				int tPos = (z*w+x)*12;
				if(mesh.wave[tPos*4] != 0) {
					if(start == -1) {
						start = z*w+x;
						stop = z*w+x+1;
					} else {
						if(stop < z*w+x+1) {
							stop = z*w+x+1;
						}
					}
					float newT = mesh.t[tPos];
					newT += dt;
					newT %= (1.0f/mesh.wave[tPos*4+3]);
					for(int i = 0; i < 12; i++) {
						mesh.t[tPos+i] = newT;
					}
				}
			}
		}
		if(start != -1)
			mLoader.updateMapTVOB(mesh, start, stop);
	}
	
	private float getSingleY(int x, int z, int i) {
		return mesh.verts[((x+z*w)*12+i*3)*3+1];
	}
	
	// Returns true if point 0 is at ground level
	private boolean isAtGroundlevel(int x, int z) {
		return !isCliff(x, z) || !isCliff(x-1, z) || !isCliff(x-1, z-1) || !isCliff(x, z-1);
	}
	
	// Returns true if surf of coordinate is a cliff
	private boolean isCliff(int x, int z) {
		if(x >= 0 && x < w && z >= 0 && z < h) {
			if(data.maps[MapData.DUGG][z][x] == 2)
				return true;
			return contains(sTypes.cliffTypes, data.maps[MapData.SURF][z][x]);
		}
		return true;
	}
	
	private boolean contains(int[] a, int j) {
		for(int i = 0; i < a.length; i++) {
			if(a[i] == j)
				return true;
		}
		return false;
	}
	
	// TODO: Safe getY function
//	private float getSafeSingleVertY(int x, int z, int i) {
//		return 0;
//	}
//	
//	private float getY(int x, int z, int i) {
//		switch (i) {
//		case 1:
//			return getSafeSingleVertY(x, z, 0);
//			break;
//		case 2:
//			return getSafeSingleVertY(x, z, 0);
//			break;
//		case 3:
//			return getSafeSingleVertY(x, z, 0);
//			break;
//		}
//		return getSafeSingleVertY(x, z, i);
//	}
	
	private void applyTextureFromAtlas(int x, int z, int tx, int ty, int rot) {
		int off = (z*w+x)*24;
		
		Vector2f glScale = mesh.split.toGLPos(1, 1);
		Vector2f glOffset = mesh.split.toGLPos(tx, ty);
		
		float[] temp = new float[24];
		System.arraycopy(mesh.tVerts, off, temp, 0, 24);
		int j = rot*3;
		for(int i = 0; i < 12; i++) {
			mesh.tVerts[off+2*j] = temp[i*2];
			mesh.tVerts[off+2*j+1] = temp[i*2+1];
			if(i%3 == 1) {
				i++;
				j++;
			}
			j++;
			j %= 12;
		}
		for(int i = 0; i < 12; i++) {
			mesh.tVerts[off+i*2]	= mesh.tVerts[off+i*2]*glScale.x + glOffset.x;
			mesh.tVerts[off+i*2+1]	= mesh.tVerts[off+i*2+1]*glScale.y + glOffset.y;
		}
	}
	
	private void setMidY(int x, int z, float height) {
		for(int i = 0; i < 4; i++)
			mesh.verts[((x+z*w)*12+2+i*3)*3+1] = height;
	}
	
	private void setSingleY(int x, int z, int i, float height) {
		i = i*3;
		int i2 = (i+10)%12;
		mesh.verts[((x+z*w)*12+i)*3+1] = height;
		mesh.verts[((x+z*w)*12+i2)*3+1] = height;
	}
	
	private void setSafeSingleVertY(int x, int z, int i, float height) {
		if(x >= 0 && x < w && z >= 0 && z < h) {
			setSingleY(x, z, i, height);
		}
	}
	
	private void setY(int x, int z, int i, float height) {
		switch(i) {
		case 0:
			setSafeSingleVertY(x, z,	0, height);
			setSafeSingleVertY(x-1, z,	1, height);
			setSafeSingleVertY(x-1, z-1,2, height);
			setSafeSingleVertY(x, z-1,	3, height);
			break;
		case 1:
			setSafeSingleVertY(x+1, z,	0, height);
			setSafeSingleVertY(x, z,	1, height);
			setSafeSingleVertY(x, z-1,	2, height);
			setSafeSingleVertY(x+1, z-1,3, height);
			break;
		case 2:
			setSafeSingleVertY(x+1, z+1,0, height);
			setSafeSingleVertY(x, z+1,	1, height);
			setSafeSingleVertY(x, z,	2, height);
			setSafeSingleVertY(x+1, z,	3, height);
			break;
		case 3:
			setSafeSingleVertY(x, z+1,	0, height);
			setSafeSingleVertY(x-1, z+1,1, height);
			setSafeSingleVertY(x-1, z,	2, height);
			setSafeSingleVertY(x, z,	3, height);
			break;
		}
	}
	
	private void triangulateTile(int x, int z, boolean zeroTwo) {
		float height;
		if(zeroTwo)
			height = (getSingleY(x, z, 0) + getSingleY(x, z, 2))/2.0f;
		else
			height = (getSingleY(x, z, 1) + getSingleY(x, z, 3))/2.0f;
		setMidY(x, z, height);
	}
	
	private void calcNormals(int x, int z) {
		for(int t = 0; t < 4; t++) {
			int offset = ((x+z*w)*12+t*3)*3;
			float[] v1 = new float[3];
			float[] v2 = new float[3];
			for(int i = 0; i < 3; i++) {
				v1[i] = mesh.verts[offset+i+3]-mesh.verts[offset+i];
				v2[i] = mesh.verts[offset+i+6]-mesh.verts[offset+i];
			}
			for(int i = 0; i < 3;i++) {
				float f = v1[(i+1)%3]*v2[(i+2)%3]-(v2[(i+1)%3]*v1[(i+2)%3]);
				for(int j = 0; j < 3; j++)
					mesh.nVerts[offset+j*3+i] = f;
			}
		}
	}
	
	public void update(float dt) {
		updateWaves(dt);
	}
	
	public void setTile(int x, int z, int val) {
		
		int[][] surf = data.maps[MapData.SURF];
		surf[z][x] = val;
		
		generateMeshDetails();
		
	}
	
	public void updateMesh() {
		// TODO: update only the changed tiles
		mLoader.updateMapMesh(mesh, 0, w*h*12);
	}
	
	private static class Point {
		public Point(int x, int z) {
			this.x = x;
			this.z = z;
		}
		int x, z;
		@Override
		public String toString() {
			return "(" + z + ", " + x + ")";
		}
	}
	private void expandCaves() {
		
		int[][] cave = data.maps[MapData.DUGG];
		int[][] surf = data.maps[MapData.SURF];
		
		ArrayList<Point> todo = new ArrayList<>();
		
		resetUtilBuffer(false);
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				if(cave[z][x] == 1 && !contains(sTypes.cliffTypes, surf[z][x])) {
					utilBuffer[z][x] = true;
					todo.add(new Point(x, z));
				}
			}
		}
		
		while(todo.size() > 0) {
			Point p = todo.get(0);
			todo.remove(0);
			for(int i = -1; i < 2; i++) {
				for(int j = -1; j < 2; j++) {
					if((p.x+j < w) && (p.x+j >= 0) && (p.z+i < h) && (p.z+i >= 0) && !(i == 0 && j == 0)) {
						if(!utilBuffer[p.z+i][p.x+j] && !contains(sTypes.cliffTypes, surf[p.z+i][p.x+j])) {
							cave[p.z+i][p.x+j] = 1;
							utilBuffer[p.z+i][p.x+j] = true;
							todo.add(new Point(p.x+j, p.z+i));
						}
					}
				}
			}
		}
	}
	private void fillDirt() {
		
		int[][] cave = data.maps[MapData.DUGG];
		int[][] surf = data.maps[MapData.SURF];
		int[][] path = data.maps[MapData.PATH];
		
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				if(cave[z][x] != 1 && !contains(sTypes.cliffTypes, surf[z][x])) {
					surf[z][x] = 4;
					path[z][x] = 0;
				}
			}
		}
	}
	
	public Vector3f getHit(Vector3f o, Vector3f d) {
		Vector3f res = null;
		float minDist = Float.MAX_VALUE;
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				Vector3f hit = hitsTile(o, d, x, z);
				if(hit != null) {
					float dist = o.distance(hit);
					if(dist < minDist)
						res = hit;
				}
			}
		}
		return res;
	}
	
	public Vector2i getTileUnderPoint(Vector3f pos) {
		Vector2i res = new Vector2i();
		res.x = (int) Math.floor(pos.x/unitDist);
		res.y = (int) Math.floor(pos.z/unitDist);
		return res;
	}
	
	public Vector2i getTileHit(Vector3f o, Vector3f d) {
		Vector2i res = new Vector2i(-1);
		
//		o = new Vector3f(10.2f*40.0f, 1000, 10.5f*40.0f);
//		d = new Vector3f(0, -1, 0);
		
		float minDist = Float.MAX_VALUE;
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				Vector3f hit = hitsTile(o, d, x, z);
				if(hit != null) {
					float dist = o.distance(hit);
					if(dist < minDist)
						res.set(x, z);
				}
			}
		}
		
		return res;
	}
	
	private void resetUtilBuffer(boolean state) {
		for(int z = 0; z < h; z++)
			for(int x = 0; x < w; x++)
				utilBuffer[z][x] = state;
	}
	
	private Vector3f hitsTile(Vector3f _o, Vector3f _d, int x, int z) {
		
//		System.out.println("TEST");
//		double[][] test = {
//				{1, 0, 0},
//				{2, 1, 0},
//				{3, 2, 1},
//				{6, 3, 1}
//		};
//		double[] testres = solveEquations3by3(test);
//		for(int i = 0; i < 3; i++) {
//			System.out.print(testres[i] + " ");
//		}
//		System.out.println();
//		System.out.println("TEST END");
		
		// u is the factor for d
		// s and t are factors for the planes axis
		// order in res: s, t, u
		int off = (z*w+x)*4*3*3;
		
		float[] o = new float[] {_o.x, _o.y, _o.z};
		float[] d = new float[] {_d.x, _d.y, _d.z};
		double[][] gaussMat = new double[4][3];
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++)
				gaussMat[3][j] = o[j] - mesh.verts[off+j];
			
			for(int j = 0; j < 2; j++)
				for(int g = 0; g < 3; g++)
					gaussMat[j][g] = mesh.verts[off+3*(j+1)+g]-mesh.verts[off+g];
			
			for(int j = 0; j < 3; j++)
				gaussMat[2][j] = -d[j];
			
//			if(x == 10 && z == 14) {
//				System.out.println("CONST: " + x + " " + z + " " + i);
//				System.out.println(gaussMat[3][0] + " " + gaussMat[3][1] + " " + gaussMat[3][2]);
//				for(int j = 0; j < 3; j++) {
//					for(int g = 0; g < 4; g++) {
//						System.out.print(gaussMat[g][j] + " ");
//					}
//					System.out.println();
//				}
//			}
			double[] res = solveEquations3by3(gaussMat/*, (x == 6 && z == 9)**/);
			if(res[0] != Float.NaN && res[1] != Float.NaN && res[0] >= 0 && res[1] >= 0) {
				if((res[0]+res[1]) >= 0 && (res[0]+res[1]) <= 1) {
//					System.out.println("RES: " + x + " " + z);
//					System.out.println(res[0] + " " + res[1] + " " + res[2]);
					Vector3f p = new Vector3f(_o);
					p.add(new Vector3f(_d).mul((float) res[2]));
					return p;
				}
			}
			
			off += 3*3;
		}
		
		return null;
	}
	
	private double[] solveEquations3by3(double[][] m/*, boolean print*/) {
		
//		System.out.println("POTENTIAL SORT:");
		// Sort
		if(m[0][0] == 0) {
//			System.out.println("0,0 IS 0:");
//			for(int i = 0; i < 4; i++) {
//				System.out.print(m[i][0] + " ");
//			}
//			System.out.println();
			for(int i = 1; i < 3; i++) {
				if(m[0][i] != 0) {
					for(int j = 0; j < 4; j++) {
						double temp = m[j][0];
//						System.out.print(m[j][i] + " ");
						m[j][0] = m[j][i];
						m[j][i] = temp;
					}
//					System.out.println();
					break;
				}
			}
		}
		
//		if(print) {
//			System.out.println("MATRIX: ");
//			for(int j = 0; j < 3; j++) {
//				for(int g = 0; g < 4; g++) {
//					System.out.print(m[g][j] + " ");
//				}
//				System.out.println();
//			}
//		}
		
		double[] res = new double[3];
		
		for(int i = 1; i < 3; i++) {
			double c = m[0][i]/m[0][0];
			for(int j = 0; j < 4; j++) {
				m[j][i] -= c*m[j][0];
			}
		}
		
//		if(print) {
//			System.out.println("LEFT SIDE MATRIX: ");
//			for(int j = 0; j < 3; j++) {
//				for(int g = 0; g < 4; g++) {
//					System.out.print(m[g][j] + " ");
//				}
//				System.out.println();
//			}
//		}
		
		if(m[1][1] == 0) {
			if(m[1][2] != 0) {
				for(int j = 0; j < 4; j++) {
					double temp = m[j][1];
					m[j][1] = m[j][2];
					m[j][2] = temp;
				}
			}
		}
		
		double c = m[1][2]/m[1][1];
		for(int j = 0; j < 4; j++) {
			m[j][2] -= c*m[j][1];
		}
		
//		if(print) {
//			System.out.println("LOWER TRI MATRIX: ");
//			for(int j = 0; j < 3; j++) {
//				for(int g = 0; g < 4; g++) {
//					System.out.print(m[g][j] + " ");
//				}
//				System.out.println();
//			}
//		}
		
		if(m[2][2] != 0)
			res[2] = m[3][2]/m[2][2];
		else
			res[2] = 0;
		for(int i = 0; i < 2; i++)
			m[3][i] -= m[2][i]*res[2];
		
		if(m[1][1] != 0)
			res[1] = m[3][1]/m[1][1];
		else
			res[1] = 0;
		
		m[3][0] -= res[1]*m[1][0];
		if(m[0][0] != 0)
			res[0] = m[3][0]/m[0][0];
		else
			res[0] = 0;
		
		
		return res;
	}
	
	public void printRot(int x, int z) {
		System.out.println("(" + x + ", " + z + "): " + sTypes.getAtlasPos(x, z, data));
	}
	
}
