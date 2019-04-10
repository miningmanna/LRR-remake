package org.rrr.assets.map;

import java.util.LinkedList;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;

public class SurfaceTypeDescription {
	
	public static final int[] CLIFF_PERM_TYPE = { -1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,3 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,3 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,1 ,-1 ,3 ,1 ,1 ,1 ,3 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,1 ,-1 ,-1 ,-1 ,-1 ,1 ,1 ,0 ,2 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,3 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,1 ,3 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,-1 ,-1 ,-1 ,1 ,1 ,1 ,-1 ,1 ,1 ,1 ,3 ,3 ,-1 ,1 ,-1 ,0 ,-1 ,1 ,-1 ,2 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,-1 ,1 ,1 ,-1 ,3 ,1 ,1 ,1 ,3 ,1 ,1 ,-1 ,1 ,1 ,1 ,3 ,3 ,3 ,3 ,-1 ,2 ,3 ,3 ,2 ,-1};
	public static final int[] CLIFF_PERM_ROT = { 0 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,2 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,2 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,2 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,0 ,0 ,3 ,3 ,0 ,0 ,0 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,2 ,2 ,3 ,3 ,3 ,3 ,0 ,0 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,2 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,2 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,2 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,1 ,1 ,3 ,1 ,1 ,1 ,1 ,1 ,3 ,1 ,3 ,3 ,3 ,1 ,3 ,2 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,3 ,0 ,0 ,3 ,3 ,0 ,0 ,0 ,3 ,1 ,1 ,3 ,1 ,1 ,1 ,1 ,1 ,0 ,0 ,3 ,0 ,0 ,0 ,1 ,3};

	public static final int[] CONNECT_PERM_TYPE = { 5 ,4 ,4 ,2 ,4 ,2 ,3 ,1 ,4 ,3 ,2 ,1 ,2 ,1 ,1 ,0};
	public static final int[] CONNECT_PERM_ROT = { 0 ,0 ,1 ,1 ,3 ,0 ,3 ,1 ,2 ,0 ,2 ,2 ,3 ,0 ,3 ,0};
	
	public static final String[] CONNECT_CASES = new String[] {
		"XConnect",
		"TConnect",
		"LConnect",
		"IConnect",
		"EConnect",
		"NConnect"
	};
	public static final String[] CLIFF_CASES = new String[] {
		"Diagonal",
		"OutsideCorner",
		"InsideCorner",
		"Wall"
	};
	
	public int[] cliffTypes;
	public Surface[] surfaces;
	public Surface defaultSurface;
	public Vector2i roof;
	private Vector3i v3;
	
	public SurfaceTypeDescription(Node cfg, AssetManager am) {
		
		v3 = new Vector3i();
		
		String defaultSurfaceStr = cfg.getOptValue("DefaultSurface", null);
		
		String roofStr = cfg.getOptValue("Roof", "0,0");
		String[] roofSplit = roofStr.split(",");
		if(roofSplit.length != 2)
			roofStr = "0,0";
		try {
			roof = new Vector2i(
					Integer.parseInt(roofSplit[0]),
					Integer.parseInt(roofSplit[1])
			);
		} catch(Throwable e) {
			roof = new Vector2i(0);
		}
		
		LinkedList<Integer> _cliffTypes = new LinkedList<>();
		LinkedList<Surface> _surfaces = new LinkedList<>();
		for(String key : cfg.getSubNodeKeys()) {
			Node subNode = cfg.getSubNode(key);
			Surface s = new Surface();
			s.name = key;
			_surfaces.add(s);
			
			if(defaultSurface == null) {
				if(defaultSurfaceStr != null)
					if(key.equalsIgnoreCase(defaultSurfaceStr))
						defaultSurface = s;
				else
					defaultSurface = s;
			}
			
			s.atRoofHeight = subNode.getOptBoolean("AtRoofHeight", false);
			s.surfaceValue = subNode.getOptInteger("SurfaceValue", 0);
			s.pathValue = subNode.getOptInteger("PathValue", 0);
			s.connects = subNode.getOptValue("Connects", "").toUpperCase().split(",");
			
			String waveStr = subNode.getOptValue("Wave", "0,1,0,0");
			String[] waveSplit = waveStr.split(",");
			if(waveSplit.length != 4)
				waveSplit = new String[] {"0", "1", "0", "0"};
			s.wave = new Vector4f(
					Float.parseFloat(waveSplit[0]),
					Float.parseFloat(waveSplit[1]),
					Float.parseFloat(waveSplit[2]),
					Float.parseFloat(waveSplit[3])
			);
			
			s.pointer = subNode.getOptValue("Pointer", null);
			String toolTipStr = subNode.getOptValue("ToolTip", ",");
			String[] toolTipSplit = toolTipStr.split(",");
			if(toolTipSplit.length != 2)
				toolTipSplit = new String[] {"",""};
			s.toolTipText = toolTipSplit[0].equals("") ? null : toolTipSplit[0].replaceAll("_", " ");
			s.toolTipSFX  = toolTipSplit[1].equals("") ? null : toolTipSplit[1];
			
			String[] caseKeys;
			if(s.atRoofHeight) {
				caseKeys = CLIFF_CASES;
				_cliffTypes.add(s.surfaceValue);
			} else {
				caseKeys = CONNECT_CASES;
			}
			
			s.texCases = new Vector2i[caseKeys.length];
			Vector2i defaultVal = new Vector2i(0, 0);
			for(int i = 0; i < caseKeys.length; i++) {
				String str = subNode.getOptValue(caseKeys[i], null);
				if(str == null)
					continue;
				String[] split = str.split(",");
				if(split.length != 2)
					continue;
				Vector2i v = null;
				try {
					v = new Vector2i(
							Integer.parseInt(split[0]),
							Integer.parseInt(split[1])
					);
				} catch(Throwable e) {
					continue;
				}
				if(v != null)
					defaultVal = v;
				s.texCases[i] = v;
			}
			for(int i = 0; i < caseKeys.length; i++)
				if(s.texCases[i] == null)
					s.texCases[i] = defaultVal;
			
		}
		
		surfaces = new Surface[_surfaces.size()];
		for(int i = 0; i < surfaces.length; i++)
			surfaces[i] = _surfaces.pop();
		
		cliffTypes = new int[_cliffTypes.size()];
		for(int i = 0; i < cliffTypes.length; i++)
			cliffTypes[i] = _cliffTypes.pop();
		
	}
	
	public Vector3i getAtlasPos(int x, int z, MapData data) {
		
		Surface[][] neighbours = getNeighbours(x, z, data);
		Surface tile = neighbours[1][1];
		
		if(data.maps[MapData.DUGG][z][x] == 0 || tile.atRoofHeight) {
			boolean[][] isCliff = getNeighboursIsCave(x, z, data);
			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					isCliff[i][j] = !isCliff[i][j] || neighbours[i][j].atRoofHeight;
				}
			}
			Vector2i v = getCliffCaseRot(isCliff);
			if(v.x == -1)
				return v3.set(roof, 0);
			return v3.set(tile.texCases[v.x], v.y);
		} else {
			boolean[][] shouldConnect = getNeightboursConnect(x, z, neighbours);
			Vector2i v = getGroundCaseRot(shouldConnect);
			return v3.set(tile.texCases[v.x], v.y);
		}
	}
	
	private Vector2i getGroundCaseRot(boolean[][] shouldConnect) {
		int pow = 0, sum = 0;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(!(i == 1 && j == 1) && (i == 1 || j == 1)) {
					if(shouldConnect[i][j])
						sum += Math.pow(2, pow);
					pow++;
				}
			}
		}
		return new Vector2i(CONNECT_PERM_TYPE[sum], CONNECT_PERM_ROT[sum]);
	}
	
	private Vector2i getCliffCaseRot(boolean[][] isCliff) {
		int pow = 0, sum = 0;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(i != 1 || j != 1) {
					if(isCliff[i][j])
						sum += (int) Math.pow(2, pow);
					pow++;
				}
			}
		}
		return new Vector2i(CLIFF_PERM_TYPE[sum], CLIFF_PERM_ROT[sum]);
	}
	
	private static int getCliffCaseIndex(boolean[][] isCliff) {
		int pow = 0, sum = 0;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if(i != 1 || j != 1) {
					if(isCliff[i][j])
						sum += (int) Math.pow(2, pow);
					pow++;
				}
			}
		}
		return sum;
	}
	
	@SuppressWarnings("unused")
	private static void print(boolean[][] r) {
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				System.out.print(r[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	private Surface[][] getNeighbours(int x, int z, MapData data) {
		Surface[][] res = new Surface[3][3];
		int dx = -1, dz = 1;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				res[i][j] = getSurfaceAt(x+dx, z+dz, data);
				dx++;
			}
			dx = -1;
			dz--;
		}
		return res;
	}
	
	private Surface getSurfaceAt(int x, int z, MapData data) {
		
		int[][] surf = data.maps[MapData.SURF],
				path = data.maps[MapData.PATH];
		
		if(x < 0 || x >= surf[0].length || z < 0 || z >= surf.length)
			return defaultSurface;
		
		for(Surface s : surfaces) {
			if(s.surfaceValue == surf[z][x] && s.pathValue == path[z][x]) {
				return s;
			}
		}
		
		return defaultSurface;
	}
	
	public int getSurfaceIndex(int x, int z, MapData data) {
		
		int[][] surf = data.maps[MapData.SURF],
				path = data.maps[MapData.PATH];
		
		if(x < 0 || x >= surf[0].length || z < 0 || z >= surf.length)
			return -1;
		
		for(int i = 0; i < surfaces.length; i++)
			if(surfaces[i].surfaceValue == surf[z][x] && surfaces[i].pathValue == path[z][x])
				return i;
		
		return -1;
	}
	
	private boolean[][] getNeighboursIsCave(int x, int z, MapData data) {
		boolean[][] res = new boolean[3][3];
		int dx = -1, dz = 1;
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				res[i][j] = getSurfaceIsCaveAt(x+dx, z+dz, data);
				dx++;
			}
			dx = -1;
			dz--;
		}
		return res;
	}
	
	private boolean getSurfaceIsCaveAt(int x, int z, MapData data) {
		
		int[][] cave = data.maps[MapData.DUGG];
		
		if(x < 0 || x >= cave[0].length || z < 0 || z >= cave.length)
			return false;
		
		return cave[z][x] == 1;
	}
	
	private boolean[][] getNeightboursConnect(int x, int z, Surface[][] neighbours) {
		boolean[][] res = new boolean[3][3];
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if((i == 1 || j == 1) && !(i == 1 && j == 1)) {
					res[i][j] = neighbours[1][1].shouldConnect(neighbours[i][j]);
				}
			}
		}
		return res;
	}
	
	public static class Surface {
		public int surfaceValue, pathValue;
		public String[] connects;
		public String pointer, toolTipText, toolTipSFX, name;
		public boolean atRoofHeight;
		public Vector2i[] texCases;
		public Vector4f wave;
		
		public boolean shouldConnect(Surface s) {
			return containsString(connects, s.name);
		}
	}
	
	private static boolean containsString(String[] a, String str) {
		str = str.toUpperCase();
		for(String s : a) { 
			if(str.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public Vector2i getCasePos(int x, int z, MapData data) {
		boolean[][] isCliff = getNeighboursIsCave(x, z, data);
		Surface[][] neighbours = getNeighbours(x, z, data);
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				isCliff[i][j] = !isCliff[i][j] || neighbours[i][j].atRoofHeight;
			}
		}
		
		if(data.maps[MapData.DUGG][z][x] == 0) {
			return new Vector2i(0, getCliffCaseIndex(isCliff));
		} else {
			return new Vector2i(1, getCliffCaseIndex(isCliff));
		}
	}
	
	public Vector4f getWave(int x, int z, MapData data) {
		Surface s = getSurfaceAt(x, z, data);
		return s.wave;
	}
}
