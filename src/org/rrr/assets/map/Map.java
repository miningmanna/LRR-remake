package org.rrr.assets.map;

import java.io.File;

import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.model.MapMesh;

public class Map {
	
	public int w;
	public int h;
	public MapData data;
	public MapMesh mesh;
	public int[] cliffTypes;
	
	public Map(AssetManager am, Node cfg) throws Exception {
		
		cliffTypes = new int[] {
				1, 2, 3, 4
		};
		
		File dir = new File("LegoRR0/" + cfg.getValue("SurfaceMap")).getParentFile();
		System.out.println(dir);
		data = MapData.getMapData(dir);
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
		
		w = surf[0].length;
		h = surf.length;
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
				if(isAtGroundlevel(x, z))
					setY(x, z, 0, height*40);
				else
					setY(x, z, 0, 40+height*40); // TODO: load roofheight
			}
		}
		
		for(int z = 0; z < h; z++) {
			for(int x = 0; x < w; x++) {
				
				boolean zeroTwo = true;
				
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
				
				triangulateTile(x, z, zeroTwo);
				
				calcNormals(x, z);
				
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
	
}
