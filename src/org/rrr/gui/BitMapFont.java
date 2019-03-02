package org.rrr.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.rrr.Input;
import org.rrr.assets.AssetManager;
import org.rrr.assets.model.ModelLoader;
import org.rrr.assets.tex.TexLoader;

public class BitMapFont {
	
	public Texture atlas;
	public int[] widths;
	public float[] glWidths;
	
	public int chars;
	public int blockLengthX;
	public int blockLengthY;
	public float glBlockLengthX;
	public float glBlockLengthY;
	
	public static BitMapFont getFont(File f, TexLoader l) {
		return null;
	}
	
	public static BitMapFont getFont(InputStream in, TexLoader l) {
		
		BitMapFont bmf = new BitMapFont();
		
		BufferedImage img = TexLoader.getBMP(in);
		
		bmf.blockLengthX = img.getWidth()/10;
		bmf.blockLengthY = img.getHeight()/19;
		
		bmf.chars = 190;
		bmf.widths = new int[bmf.chars];
		bmf.glWidths = new float[bmf.chars];
		bmf.glBlockLengthX = ((float) bmf.blockLengthX)/img.getWidth();
		bmf.glBlockLengthY = ((float) bmf.blockLengthY)/img.getHeight();
		int stripColor = 0;
		int color1 = img.getRGB(0, 0);
		
		for(int i = 0; i < bmf.blockLengthX; i++) {
			if(img.getRGB(i, 0) != color1) {
				stripColor = img.getRGB(i, 0);
				break;
			}
		}
		
		
		for(int i = 0; i < bmf.chars; i++) {
			int py = (Math.floorDiv(i, 10))*bmf.blockLengthY;
			int px = (i%10)*bmf.blockLengthX;
			int width = bmf.blockLengthX;
			for(int j = 0; j < bmf.blockLengthX; j++) {
				if(img.getRGB(px+j, py) == stripColor) {
					width = j;
					break;
				}
			}
			bmf.widths[i] = width;
			bmf.glWidths[i] = (float) width/img.getWidth();
		}
		
		bmf.atlas = l.getTexture(img);
		bmf.atlas.bind();
		bmf.atlas.setTextureFilter(GL11.GL_NEAREST);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		return bmf;
	}
	
	public int getPixLength(String str) {
		byte[] inds = new byte[str.length()];
		try {
			inds = str.getBytes("Cp850");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		int res = 0;
		for(int i = 0; i < inds.length; i++) {
			int ind = (0x00FF & inds[i])-32;
			res += widths[ind];
		}
		return res;
	}
	
}
