package org.rrr.gui;

import static org.lwjgl.opengl.GL11.GL_NEAREST;

import java.util.LinkedList;

import org.newdawn.slick.opengl.Texture;
import org.rrr.RockRaidersRemake;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.sound.SoundClip;
import org.rrr.assets.sound.Source;
import org.rrr.assets.tex.FLHAnimation;

public class Cursor {
	
	public int x, y;
	public int w, h;
	public int curAnimation = 0;
	public Texture base;
	public CursorAnimation[] animations;
	public Source playSource;
	public SoundClip sfxOkay, sfxNotOkay;
	
	
	public Cursor(Node cfg, AssetManager am, RockRaidersRemake par) {
		
		this.playSource = par.getAudioSystem().getSource();
		sfxOkay = par.getAssetManager().getSample("SFX_Okay");
		sfxNotOkay = par.getAssetManager().getSample("SFX_NotOkay");
		
		w = 32;
		h = 32;
		
		int lanims = 0;
		LinkedList<CursorAnimation> anims = new LinkedList<>();
		// getBMP
		for(String key : cfg.getValueKeys()) {
			if(key.equalsIgnoreCase("Pointer_Blank")) {
				base = am.getTexture(cfg.getValue(key));
				base.setTextureFilter(GL_NEAREST);
				continue;
			}
			
			CursorAnimation anim = new CursorAnimation();
			if(key.startsWith("Pointer_"))
				anim.name = key.substring(8);
			else
				anim.name = key;
			
			String path = cfg.getValue(key).replaceAll("\\\\", "/");
			if(path.contains(",")) {
				anim.usesBaseTex = true;
				anim.stillFrame = false;
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
				
				anim.anim = am.getFLHAnimation(split[0]);
				anim.w = anim.anim.data.w;
				anim.h = anim.anim.data.h;
				
			} else {
				anim.usesBaseTex = false;
				anim.stillFrame = true;
				anim.tex = am.getTexture(path);
				anim.w = anim.tex.getImageWidth();
				anim.h = anim.tex.getImageHeight();
			}
			
			anims.add(anim);
			lanims++;
			
		}
		
		
		animations = new CursorAnimation[lanims];
		for(int i = 0; i < lanims; i++)
			animations[i] = anims.pop();
		
	}
	
	public static class CursorAnimation {
		
		public String name;
		public int x, y, w, h;
		public boolean usesBaseTex, stillFrame;
		public Texture tex;
		public FLHAnimation anim;
		
	}
	
	public void setCursor(String name) {
		for(int i = 0; i < animations.length; i++) {
			if(animations[i].name.equals(name)) {
				curAnimation = i;
				return;
			}
		}
	}
	
	public void update(float dt) {
		if(!animations[curAnimation].stillFrame) {
			animations[curAnimation].anim.step(dt);
		}
	}
	
	public void okay() {
		playSource.play(sfxOkay);
	}
	
	public void notOkay() {
		playSource.play(sfxNotOkay);
	}
	
}
