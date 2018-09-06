package org.rrr.map;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javax.swing.JFrame;

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
	
	private static final String[] fprefix = {
			"surf",
			"dugg",
			"cror",
			"tuto",
			"high",
			"path",
			"emrg",
			"erod",
			"fall"
	};
	
	public int[][][] maps;
	public int width, height;
	
	
	public static MapData getMapData(File dir) throws Exception {
		
		MapData res = new MapData();
		res.maps = new int[9][][];
		
		File[] files = new File[9];
		for(File f : dir.listFiles()) {
			String name = f.getName();
			String prefix = name.substring(0, 4).toLowerCase();
			String end = name.substring(name.length()-3);
			for(int i = 0; i < 9; i++)
				if(prefix.equals(fprefix[i]) && end.equalsIgnoreCase("map"))
					files[i] = f;
		}
		
		
		
		for(int i = 0; i < 9; i++)
			res.loadData(i, files[i]);
		res.ensureAllData();
		
		return res;
		
	}
	
	private void loadData(int mapType, File f) throws Exception {
		
		if(f == null)
			return;
		
		int[][] data = loadMapFileData(f);
		if(width == 0 && height == 0) {
			width = data.length;
			height = data[0].length;
		} else {
			if(data.length != width || data[0].length != height)
				throw new Exception("Different map sizes!");
		}
		
		maps[mapType] = data;
		
	}
	
	private void ensureAllData() {
		for(int i = 0; i < 9; i++) {
			if(maps[i] == null)
				maps[i] = new int[width][height];
		}
	}
	
	public static int[][] loadMapFileData(File f) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(f, "r");
		raf.seek(4);
		byte[] buff = new byte[4];
		raf.read(buff);
		int len = getIntLE(buff, 0);
		buff = new byte[len];
		raf.read(buff);
		raf.close();
		
		int w = getIntLE(buff, 0);
		int h = getIntLE(buff, 4);
		int[][] res = new int[w][h];
		
		int offset = 6;
		for(int i = 0; i < h; i++)
			for(int j = 0; j < w; j++)
				res[j][i] = getShortLE(buff, (offset += 2));
		
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
	
	static MapData data;
	static int ind = 0;
	static MapVis mapVis;
	public static void main(String[] args) {
		
		try {
			data = getMapData(new File("LegoRR0/Levels/GameLevels/Level01"));
			
			JFrame f = new JFrame("MapData");
			
			mapVis = new MapVis(data.maps[DUGG]);
			printAllValues(mapVis.data);
			f.add(mapVis);
			f.pack();
			f.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void keyReleased(KeyEvent arg0) {
					if(arg0.getKeyCode() == KeyEvent.VK_Q) {
						ind--;
						if(ind < 0)
							ind = 8;
						System.out.println(fprefix[ind]);
						mapVis.data = data.maps[ind];
						printAllValues(mapVis.data);
						mapVis.repaint();
					}
					if(arg0.getKeyCode() == KeyEvent.VK_E) {
						ind++;
						ind %= 9;
						System.out.println(fprefix[ind]);
						mapVis.data = data.maps[ind];
						printAllValues(mapVis.data);
						mapVis.repaint();
					}
						
				}
				
				@Override
				public void keyPressed(KeyEvent arg0) {
					// TODO Auto-generated method stub
					
				}
			});
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setVisible(true);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	
}
