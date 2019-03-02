package org.rrr.assets.map;

import java.io.File;
import java.util.ArrayList;

import org.joml.Matrix4x3f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.model.MapMesh;
import org.rrr.assets.model.ModelLoader;

public class Map {
	
	public int w;
	public int h;
	public MapData data;
	public MapMesh mesh;
	public int[] cliffTypes;
	private boolean[][] utilBuffer;
	
	private ModelLoader mLoader;
	
	public Map(AssetManager am, Node cfg) throws Exception {
		
		mLoader = am.getMLoader();
		
//		float[][] test = new float[][] {
//				{1, -1, 1},
//				{1, 0, 0},
//				{0, 0, 1},
//				{0, 3, 0}
//		};
//		System.out.println("GAUS SOLVING:");
//		float[] res = solveEquations3by3(test);
//		System.out.println("RESULT:");
//		for(int i = 0; i < 3; i++)
//			System.out.print(res[i] + " ");
//		System.out.println();
		
		cliffTypes = new int[] {
				1, 2, 3, 4
		};
		
		File dir = new File("LegoRR0/" + cfg.getValue("SurfaceMap")).getParentFile();
		System.out.println(dir);
		data = MapData.getMapData(am, cfg);
		w = data.width;
		h = data.height;
		utilBuffer = new boolean[h][w];
		mesh = new MapMesh();
		am.getTexSplit(this, cfg.getOptValue("TextureSet", "Rock"));
		initMapMesh();
		am.getMLoader().loadMapMeshIntoVao(mesh);
		
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
		
		float unitDist = 40;
		int[][] high = data.maps[MapData.HIGH];
		int[][] surf = data.maps[MapData.SURF];
		int[][] cave = data.maps[MapData.DUGG];
		
		System.out.println("SURF DIMS: " + w + " " + h);
		System.out.println("HIGH DIMS: " + high[0].length + " " + high.length);
		
		mesh.inds = new int[w * h * baseVerts.length/3];
		mesh.verts = new float[mesh.inds.length*3];
		mesh.nVerts = new float[mesh.inds.length*3];
		mesh.tVerts = new float[mesh.inds.length*2];
		mesh.tex = new int[w * h];
		mesh.tRotation = new float[w * h];
		
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
		
		generateMeshDetails();
	}
	
	private void generateMeshDetails() {
		
		float unitDist = 40;
		int[][] high = data.maps[MapData.HIGH];
		int[][] surf = data.maps[MapData.SURF];
		int[][] cave = data.maps[MapData.DUGG];
		
		expandCaves();
		
		for(int z = 0; z <= h; z++) {
			for(int x = 0; x <= w; x++) {
				float height;
				if(x == w) {
					if(z == h) {
						height = high[z-1][x-1]/7.0f;
					} else {
						height = high[z][x-1]/7.0f;
					}
				} else if(z == h) {
					height = high[z-1][x]/7.0f;
				} else {
					height = high[z][x]/7.0f;
				}
				boolean isCave = false;
				if(x != w && z != h) {
					isCave = data.maps[MapData.DUGG][z][x] == 1;
				}
				
				if(isCave) {
					if(isAtGroundlevel(x, z))
						setY(x, z, 0, height*40);
					else
						setY(x, z, 0, 40+height*40); // TODO: load roofheight
				} else {
					setY(x, z, 0, 40+height*40); // TODO: load roofheight
				}
			}
		}
		
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				
				boolean zeroTwo = true;
				
				if(data.maps[MapData.DUGG][z][x] == 1) {
					// TODO: remove hardcode
					switch(surf[z][x]) {
					case 1:
					case 2:
					case 3:
					case 4:
						mesh.tex[z*w+x] = 6-surf[z][x];
						break;
					case 5:
						mesh.tex[z*w+x] = 0;
						break;
					default:
						mesh.tex[z*w+x] = 56;
						break;
					}
					
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
					
					mesh.tRotation[z*w+x] = 0;
					switch(groundPoints) {
					case 0:
						break;
					case 1:
						System.out.println("ONE POINT AT GROUND");
						for(int i = 0; i < 4; i++)
							System.out.println("   " + groundLevels[i]);
						System.out.println(firstAfterZero);
						System.out.println(firstAfterZero == 0 || firstAfterZero == 2);
					case 2:
						zeroTwo = firstAfterZero == 0 || firstAfterZero == 2;
						if(groundLevels[(firstAfterZero+1)%4]) {
							mesh.tRotation[z*w+x] = (float) ((Math.PI+firstAfterZero*(Math.PI/2))%(2*Math.PI));
						}
						break;
					case 3:
						zeroTwo = (firstAfterZero+1)%4 == 0 || (firstAfterZero+1)%4 == 2;
						break;
					}
				} else {
					mesh.tex[z*w+x] = 56;
				}
				
				triangulateTile(x, z, zeroTwo);
				
				calcNormals(x, z);
				
			}
		}
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
			return contains(cliffTypes, data.maps[MapData.SURF][z][x]);
		}
		return true;
	}
	
	private boolean contains(int[] a, int j) {
		for(int i = 0; i < a.length; i++) {
			if(a[i] > j)
				break;
			else
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
		System.out.println("Setting " + x + " " + z + " " + i + " to " + height);
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
	
	public void update(float val) {
//		
//		int off = 0, l = 12;
//		
//		setY(0, 0, 0, getSingleY(0, 0, 0)+val);
//		calcNormals(0, 0);
//		
//		mLoader.updateMapMesh(mesh, off, l);
//		
	}
	
	public void setTile(int x, int z, int val) {
		
		int[][] surf = data.maps[MapData.SURF];
		int[][] high = data.maps[MapData.HIGH];
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
		
		resetUtilBuffer(false);
		int sx = -1, sz = -1;
		for(int z = 0; z < h && sz == -1; z++) {
			for(int x = 0; x < w; x++) {
				if(cave[z][x] == 1 && !contains(cliffTypes, surf[z][x])) {
					sx = x;
					sz = z;
					utilBuffer[z][x] = true;
					break;
				}
			}
		}
		
		System.out.println("EXPANDING CAVES WITH START: " + sz + " " + sx);
		ArrayList<Point> todo = new ArrayList<>();
		todo.add(new Point(sx, sz));
		while(todo.size() > 0) {
			Point p = todo.get(0);
			todo.remove(0);
			for(int i = -1; i < 2; i++) {
				for(int j = -1; j < 2; j++) {
					if((p.x+i) >= 0 && (p.x+i) < w && (p.z+j) >= 0 && (p.z+j) < h && !(i == 0 && j == 0)) {
						if(!utilBuffer[p.z+j][p.x+i]) {
							System.out.println("CHECKING NEIGHBOUR OF " + p + ": " + (p.z+j) + ", " + (p.x+i));;
							cave[p.z+j][p.x+i] = 1;
							utilBuffer[p.z+j][p.x+i] = true;
							if(!contains(cliffTypes, surf[p.z+j][p.x+i])) {
								todo.add(new Point(p.x+i, p.z+j));
							}
						}
					}
				}
			}
		}
	}
	
	public Vector2f getTileHit(Vector3f o, Vector3f d) {
		Vector2f res = new Vector2f(-1);
		
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
	
}
