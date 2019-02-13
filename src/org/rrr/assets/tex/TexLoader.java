package org.rrr.assets.tex;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

public class TexLoader {
	
	private ArrayList<Integer> texsIds;
	private HashMap<String, Texture> texs;
	private HashMap<String, FLHAnimation.BaseData> flhAnims;
	
	public TexLoader() {
		texs = new HashMap<>();
		flhAnims = new HashMap<>();
		texsIds = new ArrayList<>();
	}
	
	public FLHAnimation getAnimation(File file) {
		String key = file.getName();
		if(flhAnims.containsKey(key))
			return new FLHAnimation(flhAnims.get(key));
		
		FLHFile flh = null;
		try {
			FileInputStream in = new FileInputStream(file);
			flh = FLHFile.getFLHFile(in);
			in.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		if(flh == null)
			return null;
		
		
		FLHAnimation.BaseData bd = FLHAnimation.getBaseData(this, flh);
		flhAnims.put(key, bd);
		
		return new FLHAnimation(bd);
		
	}
	
	public Texture getTexture(String format, File f) throws IOException {
		if(texs.containsKey(f.getAbsolutePath())) {
			return texs.get(f.getAbsolutePath());
		} else {
			Texture t = TextureLoader.getTexture(format, new FileInputStream(f));
			texs.put(f.getAbsolutePath(), t);
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
				buffer.put((byte) ((pixel >> 24) & 0xFF));
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
	
	public Vector3f getColorFromBMPPalet(File file, int aind) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		raf.seek(54+aind*4);
		byte[] bc = new byte[4];
		raf.read(bc);
		int r = 0x000000FF&bc[2];
		int g = 0x000000FF&bc[1];
		int b = 0x000000FF&bc[0];
		raf.close();
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
	
}
