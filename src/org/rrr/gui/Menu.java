package org.rrr.gui;

import java.io.File;
import java.util.LinkedList;

import org.newdawn.slick.opengl.Texture;
import org.rrr.Input;
import org.rrr.RockRaidersRemake;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.tex.FLHAnimation;

public class Menu {
	
	private Input input;
	private Node cfg;
	private RockRaidersRemake par;
	
	public int x, y, ax, ay;
	public String title;
	public String fullName;
	public BitMapFont menuFont;
	public BitMapFont hiFont;
	public BitMapFont loFont;
	public Texture bgImage;
	public int curOverlay;
	public Overlay[] overlays;
	public MenuItem[] items;
	public boolean autoCenter, displayTitle, canScroll, playRandom;
	
	public Menu(RockRaidersRemake par, Node cfg) {
		
		AssetManager am = par.getAssetManager();
		
		this.par = par;
		this.cfg = cfg;
		String coords = cfg.getValue("Position");
		String[] split = coords.split(":");
		x = Integer.parseInt(split[0]);
		y = Integer.parseInt(split[1]);
		
		String acoords = cfg.getOptValue("Anchored", "0:0");
		split = coords.split(":");
		ax = Integer.parseInt(split[0]);
		ay = Integer.parseInt(split[1]);
		
		title		= cfg.getValue("Title");
		fullName	= cfg.getValue("FullName");
		
		String menuFontPath	= cfg.getValue("MenuFont");
		String hiFontPath	= cfg.getValue("HiFont");
		String loFontPath	= cfg.getValue("LoFont");
		menuFont = am.getFont(menuFontPath);
		if(hiFontPath != null)
			hiFont = am.getFont(hiFontPath);
		if(loFontPath != null)
			loFont = am.getFont(loFontPath);
		
		
		autoCenter		= cfg.getOptBoolean("AutoCenter", true);
		displayTitle	= cfg.getOptBoolean("DisplayTitle", true);
		canScroll		= cfg.getOptBoolean("CanScroll", false);
		playRandom		= cfg.getOptBoolean("PlayRandom", false);
		System.out.println("Playrandom " + playRandom);
		
		LinkedList<Overlay> _anims = new LinkedList<>();
		// TODO: hard limit overlay count :(
		for(int i = 1; i <= 10; i++) {
			String key = "!Overlay" + i;
			System.out.println(i + " Overlay: " + key);
			String val = cfg.getValue(key);
			if(val == null)
				break;
			
			String[] overlaySplit = val.split(":");
			Overlay o = new Overlay();
			o.x = Integer.parseInt(overlaySplit[2]);
			o.y = Integer.parseInt(overlaySplit[3]);
			// TODO hardcoded paths :(
			o.anim = am.getFLHAnimation(new File("LegoRR0/" + overlaySplit[0]));
			_anims.add(o);
		}
		
		overlays = new Overlay[_anims.size()];
		for(int i = 0; i < overlays.length; i++)
			overlays[i] = _anims.pop();
		
		curOverlay = -1;
		
		String bgPath = cfg.getValue("MenuImage");
		if(bgPath.contains(":"))
			bgPath = bgPath.split(":")[0];
		bgImage = am.getTexture(bgPath);
		
		int itemCount = cfg.getInteger("ItemCount");
		items = new MenuItem[itemCount];
		
		for(int i = 1; i < itemCount+1; i++) {
			String key = "Item"+i;
			String cfgStr = cfg.getValue(key);
			MenuItem item = null;
			switch (cfgStr.charAt(0)) {
			case 'T':
				item = new TriggerItem(key, cfgStr, this);
				break;
			case 'S':
				item = new SliderItem(key, cfgStr, this);
				break;
			case 'N':
				item = new NextItem(key, cfgStr, this);
				break;
			case 'C':
				item = new CycleItem(key, cfgStr, this);
				break;
			default:
				break;
			}
			items[i-1] = item;
		}
		
	}
	
	public AssetManager getAssetManager() {
		return par.getAssetManager();
	}
	
	public void changeMenu(String menu) {
		Node newCfg = cfg.getParent().getSubNode(menu);
		par.setMenu(newCfg);
	}
	
	public void setInput(Input input) {
		this.input = input;
	}
	
	private MenuItem highlightItem;
<<<<<<< HEAD
	public void update(float dt) {
=======
	public void update() {
>>>>>>> b2dd22ed37680fa03dab0285ee7a034ff4a6873d
		if(input.mouseJustPressed[0]) {
			if(highlightItem != null) {
				if(highlightItem instanceof NextItem)
					changeMenu(((NextItem) highlightItem).menuLink);
			}
		}
			
		boolean highlighted = false;
		for(MenuItem item : items) {
			item.hover = false;
			if(item != null && !highlighted) {
				boolean isHovered = (input.mouse.x >= item.x)
								 && (input.mouse.x <= item.x+item.w)
								 && (input.mouse.y >= item.y)
								 && (input.mouse.y <= item.y+item.h);
				if(!isHovered)
					continue;
				
				highlightItem = item;
				item.hover = true;
				highlighted = true;
			}
		}
		if(!highlighted)
			highlightItem = null;
		
		// Overlays
		if(overlays.length != 0) {
			if(curOverlay == -1 && playRandom) {
				curOverlay = randInt(overlays.length-1);
			} else if(curOverlay != -1) {
				if(overlays[curOverlay].anim.justFinished && playRandom) {
					curOverlay = randInt(overlays.length-1);
					overlays[curOverlay].anim.justFinished = false;
				} else {
<<<<<<< HEAD
					overlays[curOverlay].anim.step(dt);
=======
					overlays[curOverlay].anim.step(1);
>>>>>>> b2dd22ed37680fa03dab0285ee7a034ff4a6873d
				}
			}
		}
	}
	
	private static int randInt(int max) {
		return (int) Math.floor(Math.random() * (max+1));
	}
	
	public static class Overlay {
		
		public int x, y;
		public FLHAnimation anim;
		// TODO SoundFX
		
	}
	
}
