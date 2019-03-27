package org.rrr.assets.tex;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.rrr.Input;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.map.Map;

public class TexLoader {
	
	private ArrayList<Integer> texsIds;
	private HashMap<String, Texture> texs;
	private HashMap<String, FLHAnimation.BaseData> flhAnims;
	
	public TexLoader() {
		texs = new HashMap<>();
		flhAnims = new HashMap<>();
		texsIds = new ArrayList<>();
	}
	
	public FLHAnimation getAnimation(File f) {
		FLHAnimation res = null;
		FileInputStream in = null;
		try {
			String path = f.getAbsolutePath();
			in = new FileInputStream(f);
			res = getAnimation(path, in);
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
	
	public FLHAnimation getAnimation(String path, InputStream in) {
		if(flhAnims.containsKey(path))
			return new FLHAnimation(flhAnims.get(path), 25);
		
		FLHFile flh = null;
		try {
			flh = FLHFile.getFLHFile(in);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(flh == null)
			return null;
		
		
		FLHAnimation.BaseData bd = FLHAnimation.getBaseData(this, flh);
		flhAnims.put(path, bd);
		
		return new FLHAnimation(bd, 25);
		
	}
	
	public Texture getTexture(String format, File f) throws IOException {
		FileInputStream in = new FileInputStream(f);
		Texture res = getTexture(format, in, f.getAbsolutePath());
		in.close();
		return res;
	}
	
	public Texture getTexture(String format, InputStream in, String path) throws IOException {
		if(texs.containsKey(path)) {
			return texs.get(path);
		} else {
			Texture t = TextureLoader.getTexture(format, in);
			texs.put(path, t);
			texsIds.add(t.getTextureID());
			return t;
		}
	}
	
	public Texture getTexture(BufferedImage img) {
		
		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		
		int[] pixels = new int[img.getHeight()*img.getWidth()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
		ByteBuffer buffer = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4); //4 for RGBA, 3 for RGB
		for(int y = 0; y < img.getHeight(); y++){
			for(int x = 0; x < img.getWidth(); x++){
				int pixel = pixels[y * img.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) | 0xFF));
			}
		}
		
		buffer.flip();
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, img.getWidth(), img.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		glBindTexture(GL_TEXTURE_2D, 0);
		
		texsIds.add(id);
		return new MTexture(id);
	}
	
	public static BufferedImage getBMP(InputStream in) {
		
		BufferedImage img = null;
		try {
			byte[] header = new byte[54];
			
			in.read(header);
			
			int w = getIntLE(header, 18);
			int h = getIntLE(header, 22);
			
			int paletteSize = (int) Math.sqrt(getIntLE(header, 46));
			if(paletteSize == 0)
				paletteSize = 16;
			
			int[] palette = new int[paletteSize*paletteSize];
			
			byte[] bpalette = new byte[paletteSize*paletteSize*4];
			
			in.read(bpalette);
			
			for(int i = 0; i < bpalette.length; i += 4) {
				int rgb = 0;
				rgb |= (0xFF & (255-bpalette[i+3])) << 24;
				rgb |= (0xFF & bpalette[i+2]) << 16;
				rgb |= (0xFF & bpalette[i+1]) << 8;
				rgb |= (0xFF & bpalette[i]);
				palette[i/4] = rgb;
			}
			
			img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			
			short bitsPerPix = getShortLE(header, 28);
			if(bitsPerPix != 8)
				System.out.println("UNUSUAL BITS PER PIX");
			
			int compression = getIntLE(header, 30);
			if(compression != 0)
				System.out.println("USES COMPRESSION");
			
			int rowSize = (int) Math.ceil((bitsPerPix*w)/32)*4;
			
			for(int i = 0; i < h; i++) {
				
				byte[] row = new byte[rowSize];
				in.read(row);
				for(int j = 0; j < w; j++) {
					img.setRGB(j, h-1-i, palette[0x00FF & row[j]]);
				}
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return img;
	}
	
	public Vector3f getColorFromBMPPalet(InputStream in, int aind) throws IOException {
		in.skip(54+aind*4);
		byte[] bc = new byte[4];
		in.read(bc);
		int r = 0x000000FF&bc[2];
		int g = 0x000000FF&bc[1];
		int b = 0x000000FF&bc[0];
		Vector3f c = new Vector3f();
		c.x = r/255.0f;
		c.y = g/255.0f;
		c.z = b/255.0f;
		return c;
	}
	
	public void destroy() {
		for(int id : texsIds)
			glDeleteTextures(id);
	}
	
	private static short getShortLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 2; i++) {
			res = res | (0x000000FF & b[off+1-i]);
			if(i != 1)
				res = res << 8;
		}
		
		return (short) res;
		
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
	
}
