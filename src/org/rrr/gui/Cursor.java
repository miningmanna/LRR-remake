package org.rrr.gui;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.newdawn.slick.opengl.Texture;
import org.rrr.cfg.LegoConfig.Node;
import org.rrr.flh.FLHFile;
import org.rrr.model.Loader;

public class Cursor {
	
	public int x, y;
	public int w, h;
	public int curAnimation = 0;
	public Texture base;
	public CursorAnimation[] animations;
	
	public void init(Node cfg, Loader l) {
		
		w = 32;
		h = 32;
		
		int lanims = 0;
		LinkedList<CursorAnimation> anims = new LinkedList<>();
		
		for(String key : cfg.getValueKeys()) {
			
			if(key.equals("Pointer_Blank")) {
				
				try {
					base = l.getTexture("bmp", new File("LegoRR0/" + cfg.getValue(key)));
					base.setTextureFilter(GL_NEAREST);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Couldnt load baseTexture");
				}
				
				continue;
			}
			
			CursorAnimation anim = new CursorAnimation();
			anim.name = key;
			anim.frame = 0;
			
			String path = cfg.getValue(key);
			if(path.contains(",")) {
				anim.usesBaseTex = true;
				String[] split = path.split(",");
				if(split.length != 3) {
					System.out.println("pointers: argument mismatch");
					continue;
				}
				try {
					anim.x = Integer.parseInt(split[1]);
					anim.y = Integer.parseInt(split[2]);
				} catch (Exception e) {
					System.out.println("pointers: invalid arguments " + split[1] + " " + split[2]);
					continue;
				}
				
				// TODO: improved loading (static paths)
				FLHFile flh = null;
				try {
					InputStream in = new FileInputStream("LegoRR0/" + split[0]);
					System.out.println("Path: " + split[0]);
					flh = FLHFile.getFLHFile(in);
					in.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.out.println("Couldnt find file: LegoRR0/" + split[0]);
					continue;
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Couldnt load file: LegoRR0/" + split[0]);
					continue;
				}
				
				anim.w = flh.width;
				anim.h = flh.height;
				
				anim.texs = new Texture[flh.lframes];
				for(int i = 0; i < flh.lframes; i++) {
					anim.texs[i] = l.getTexture(flh.frames.get(i));
					anim.texs[i].setTextureFilter(GL_NEAREST);
				}
				
			} else {
				anim.usesBaseTex = false;
				anim.texs = new Texture[1];
				// TODO: improved texture loading (static path)
				try {
					anim.texs[0] = l.getTexture("bmp", new File("LegoRR0/" + path));
					anim.w = anim.texs[0].getImageWidth();
					anim.h = anim.texs[0].getImageHeight();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Failed to load texture for pointer: " + path);
					continue;
				}
			}
			
			anims.add(anim);
			lanims++;
			
		}
		
		animations = new CursorAnimation[lanims];
		for(int i = 0; i < lanims; i++) {
			animations[i] = anims.pop();
			System.out.println(i + ": " + animations[i].name);
		}
		
	}
	
	public static class CursorAnimation {
		
		public String name;
		public int x, y;
		public int w, h;
		public boolean usesBaseTex;
		public Texture[] texs;
		public int frame;
		
	}
	
}
