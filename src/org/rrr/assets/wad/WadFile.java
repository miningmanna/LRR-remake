package org.rrr.assets.wad;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

public class WadFile {
	
	private File origFile;
	private String[] entries;
	private long[] fStart;
	private long[] fLength;
	
	public WadStream getStream(String path) {
		int index = -1;
		for(int i = 0; i < entries.length; i++)
			if(entries[i].equals(path))
				index = i;
		if(index == -1)
			return null;
		WadStream stream = null;
		try {
			stream = new WadStream(new RandomAccessFile(origFile, "r"), fStart[index], fLength[index]);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return stream;
	}
	
	public String[] getEntries() {
		return entries;
	}
	
	public static WadFile getWadFile(File f) throws IOException {
		
		WadFile wad = new WadFile();
		
		RandomAccessFile in = new RandomAccessFile(f, "r");
		long pos = 0;
		
		byte[] buff = new byte[2048];
		in.read(buff, 0, 8); pos+=8;
		if(!new String(buff, 0, 4).equals("WWAD")) {
			System.err.println("Different wad file type! : " + new String(buff, 0, 4));
			in.close();
			return null;
		}
		
		int lentries = getIntLE(buff, 4);
		
		int cases = 0;
		
		wad.entries = new String[lentries];
		wad.fStart = new long[lentries];
		wad.fLength = new long[lentries];
		
		int buffOffset = 0;
		in.read(buff, 0, 2048); pos+=2048;
		for(int i = 0; i < lentries; i++) {
			String name = "";
			for(int j = buffOffset; j < 2048; j++) {
				if(buff[j] == 0) {
					name += new String(buff, buffOffset, j-buffOffset);
					wad.entries[i] = name;
					buffOffset = j+1;
					if(buffOffset == 2048) {
						cases++;
						in.read(buff, 0, 2048); pos+=2048;
						buffOffset = 0;
					}
					break;
				}
				if(j == 2047) {
					name += new String(buff, buffOffset, 2048-buffOffset);
					in.read(buff, 0, 2048); pos+=2048;
					buffOffset = 0;
					j = -1;
				}
			}
		}
		for(int i = 0; i < lentries; i++) {
			for(int j = buffOffset; j < 2048; j++) {
				if(buff[j] == 0) {
					buffOffset = j+1;
					if(buffOffset == 2048) {
						cases++;
						in.read(buff, 0, 2048); pos+=2048;
						buffOffset = 0;
					}
					break;
				}
				if(j == 2047) {
					in.read(buff, 0, 2048); pos+=2048;
					buffOffset = 0;
					j = -1;
				}
			}
		}
		in.seek(pos-(2048-buffOffset));
		
		for(int i = 0; i < lentries; i++) {
			in.read(buff, 0, 16);
//			if(i == 1814 || i == 1815) {
//				System.out.println(wad.entries[i]);
//				for(int j = 0; j < 16; j += 4)
//					System.out.println(getIntLE(buff, j));
//			}
			wad.fLength[i] = getIntLE(buff, 8);
			wad.fStart[i] = getIntLE(buff, 12);
		}
		
		in.close();
		
		wad.origFile = f;
		
		return wad;
		
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
	
	public static void main(String[] args) {
		
		try {
			WadFile wad = WadFile.getWadFile(new File("test2.wad"));
			WadStream in = wad.getStream("Interface\\FrontEnd\\Rock_wipe\\RockWipe.lws");
			FileOutputStream out = new FileOutputStream("rockWipe.lws");
			byte[] buff = new byte[2048];
			int len = -1;
			while((len = in.read(buff)) != -1) {
				out.write(buff, 0, len);
			}
			out.close();
			in.close();
			in = wad.getStream("Interface\\FrontEnd\\Rock_wipe\\RockWipe.lws");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while((line = br.readLine()) != null)
				System.out.println(line);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
