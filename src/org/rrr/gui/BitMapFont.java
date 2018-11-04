package org.rrr.gui;

import java.awt.image.BufferedImage;
import java.io.File;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.rrr.model.Loader;

public class BitMapFont {
	
	public Texture atlas;
	public int[] widths;
	public float[] glWidths;
	
	public int chars;
	public int blockLength;
	public float glBlockLengthX;
	public float glBlockLengthY;
	
	public static BitMapFont getFont(File f, Loader l) {
		
		BitMapFont bmf = new BitMapFont();
		
		BufferedImage img = Loader.getBMP(f);
		
		bmf.blockLength = img.getWidth()/10;
		bmf.chars = (img.getHeight()/bmf.blockLength)*10;
		bmf.widths = new int[bmf.chars];
		bmf.glWidths = new float[bmf.chars];
		bmf.glBlockLengthX = ((float) bmf.blockLength)/img.getWidth();
		bmf.glBlockLengthY = ((float) bmf.blockLength)/img.getHeight();
		int stripColor = 0;
		int color1 = img.getRGB(0, 0);
		
		for(int i = 0; i < bmf.blockLength; i++) {
			if(img.getRGB(i, 0) != color1) {
				stripColor = img.getRGB(i, 0);
				break;
			}
		}
		
		
		for(int i = 0; i < bmf.chars; i++) {
			int py = (Math.floorDiv(i, 10))*bmf.blockLength;
			int px = (i%10)*bmf.blockLength;
			int width = bmf.blockLength;
			for(int j = 0; j < bmf.blockLength; j++) {
				if(img.getRGB(px+j, py) == stripColor) {
					width = j;
					break;
				}
			}
			bmf.widths[i] = width;
			bmf.glWidths[i] = (float) width/img.getWidth();
			System.out.println(width);
		}
		
		bmf.atlas = l.getTexture(img);
		bmf.atlas.bind();
		bmf.atlas.setTextureFilter(GL11.GL_NEAREST);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		return bmf;
	}
	
}
