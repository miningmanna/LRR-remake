package org.rrr.assets.map;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.rrr.Input;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;

public class MapData {

	public static final int	SURF = 0,
							DUGG = 1,
							CROR = 2,
							TUTO = 3,
							HIGH = 4,
							PATH = 5,
							EMRG = 6,
							EROD = 7,
							FALL = 8;
	
	private static final String[] CFG_KEYS = {
			"TerrainMap",
			"PredugMap",
			"CryoreMap",
			"BlockPointersMap",
			"SurfaceMap",
			"PathMap",
			"EmergeMap",
			"ErodeMap",
			"FallinMap"
	};
	
	public int[][][] maps;
	public int width, height;
	
	
	public static MapData getMapData(AssetManager am, Node cfg) throws Exception {
		
		MapData res = new MapData();
		res.maps = new int[9][][];
		
		for(int i = 0; i < CFG_KEYS.length; i++) {
			String f = cfg.getOptValue(CFG_KEYS[i], null);
			if(f != null)
				res.loadData(i, f, am);
		}
		res.ensureAllData();
		
		return res;
		
	}
	
	private void loadData(int mapType, String path, AssetManager am) throws Exception {
		
		InputStream in = am.getAsset(path);
		int[][] data = loadMapDataStream(in);
		in.close();
		if(width == 0 && height == 0) {
			height = data.length;
			width = data[0].length;
		} else {
			if(data.length != height || data[0].length != width)
				throw new Exception("Different map sizes!");
		}
		
		maps[mapType] = data;
		
	}
	
	private void loadData(int mapType, File f) throws Exception {
		
		InputStream in = new FileInputStream(f);
		int[][] data = loadMapDataStream(in);
		in.close();
		if(width == 0 && height == 0) {
			height = data.length;
			width = data[0].length;
		} else {
			if(data.length != height || data[0].length != width)
				throw new Exception("Different map sizes!");
		}
		
		maps[mapType] = data;
		
	}
	
	private void ensureAllData() {
		for(int i = 0; i < 9; i++) {
			if(maps[i] == null)
				maps[i] = new int[height][width];
		}
	}
	
	public static int[][] loadMapDataFile(File f) {
		FileInputStream in = null;
		int[][] res = null;
		try {
			in = new FileInputStream(f);
			res = loadMapDataStream(in);
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	public static int[][] loadMapDataStream(InputStream in) throws IOException {
		in.skip(4);
		byte[] buff = new byte[4];
		in.read(buff);
		int len = getIntLE(buff, 0);
		buff = new byte[len];
		in.read(buff);
		in.close();
		
		int w = getIntLE(buff, 0);
		int h = getIntLE(buff, 4);
		int[][] res = new int[h][w];
		
		int offset = 6;
		for(int i = 0; i < h; i++)
			for(int j = 0; j < w; j++)
				res[i][w-1-j] = getShortLE(buff, (offset += 2));
		
		return res;
	}
	
	public static void printAllValues(int[][] map) {
		
		ArrayList<Integer> vals = new ArrayList<>();
		
		for(int i = 0; i < map.length; i++)
			for(int j = 0; j < map[i].length; j++)
				if(!vals.contains(map[i][j]))
					vals.add(map[i][j]);
		
		for(int i : vals)
			System.out.println("vals: " + i);
		
	}
	
	private static int getIntLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 4; i++) {
			res = res | (0x000000FF & b[off+3-i]);
			if(i != 3)
				res = res << 8;
		}
		
		return res;
		
	}
	
	private static int getShortLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 2; i++) {
			res = res | (0x000000FF & b[off+1-i]);
			if(i != 1)
				res = res << 8;
		}
		
		return res;
		
	}
	
	public static void main(String[] args) {
		
		File f = new File("Surf.map");
		int[][] vals = loadMapDataFile(f);
		for(int z = 0; z < vals.length; z++) {
			for(int x = 0; x < vals[z].length; x++) {
				System.out.print(vals[z][x] + " ");
			}
			System.out.println();
		}
		
	}
	
}
