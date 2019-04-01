package org.rrr.gui;

import java.util.LinkedList;
import java.util.ResourceBundle.Control;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.newdawn.slick.opengl.Texture;
import org.rrr.DelayedProcessor.Action;
import org.rrr.Input;
import org.rrr.RockRaidersRemake;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.sound.SoundClip;
import org.rrr.assets.sound.Source;
import org.rrr.assets.tex.FLHAnimation;

public class Menu {
	
	private Input input;
	private Node cfg;
	private RockRaidersRemake par;
	private TriggerProcessor triggerProcessor;
	
	private Source overlaySource;
	private Action overlayDelayAction;
	
	public Vector3f scrollOffset;
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
	public boolean autoCenter, displayTitle, canScroll, playRandom, queueNewOverlay;
	
	public Menu(RockRaidersRemake par, Node cfg, Node triggers) {
		
		triggerProcessor = new TriggerProcessor(par);
		
		AssetManager am = par.getAssetManager();
		overlaySource = par.getAudioSystem().getSource();
		
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
		
		title		= cfg.getValue("Title").replaceAll("[_]", " ");
		fullName	= cfg.getValue("FullName").replaceAll("[_]", " ");
		
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
		queueNewOverlay = playRandom;
		
		LinkedList<Overlay> _anims = new LinkedList<>();
		// TODO: hard limit overlay count :(
		for(int i = 1; i <= 10; i++) {
			String key = "!Overlay" + i;
			String val = cfg.getValue(key);
			if(val == null)
				break;
			
			String[] overlaySplit = val.split(":");
			Overlay o = new Overlay();
			o.x = Integer.parseInt(overlaySplit[2]);
			o.y = Integer.parseInt(overlaySplit[3]);
			o.anim = am.getFLHAnimation(overlaySplit[0]);
			
			o.sound = par.getAssetManager().getSample(overlaySplit[1]);
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
		System.out.println("ITEMCOUNT: " + itemCount);
		items = new MenuItem[itemCount];
		
		for(int i = 1; i < itemCount+1; i++) {
			String key = "Item"+i;
			String cfgStr = cfg.getValue(key);
			MenuItem item = null;
			System.out.println(i);
			switch (cfgStr.charAt(0)) {
			case 'T':
				String triggerBindKey = (cfg.getPath() + ":" + key).replaceAll("\\/", ":");
				item = new TriggerItem(key, cfgStr, this);
				((TriggerItem) item).func = triggers.getOptValue(triggerBindKey, "");
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
		
		// Defining items as fixed:
		String fixedStr = cfg.getOptValue("FixedItems", null);
		if(fixedStr != null) {
			String[] fixedSplit = fixedStr.split(":");
			for(String itemNumStr : fixedSplit) {
				int itemNum;
				try {
					itemNum = Integer.parseInt(itemNumStr);
				} catch(Exception e) {
					continue;
				}
				MenuItem item = items[itemNum-1];
				item.fixed = false;
			}
		}
		
		scrollOffset = new Vector3f();
		
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
	public void update(float dt) {
		if(input.mouseJustPressed[0]) {
			if(highlightItem != null) {
				if(highlightItem instanceof NextItem)
					changeMenu(((NextItem) highlightItem).menuLink);
				else if(highlightItem instanceof TriggerItem)
					triggerProcessor.trigger(((TriggerItem) highlightItem).func);
				
				par.getCursor().okay();
			}
		}
		
		int scrollDirection = 0;
		if(input.mouse.y <= (par.getHeight()/3)) {
			scrollDirection = -1;
		} else if(input.mouse.y >= (2*par.getHeight()/3)) {
			scrollDirection = 1;
		}
		
		if(scrollDirection != 0) {
			float y = input.mouse.y;
			if(scrollDirection > 0)
				y -= (2.0f*par.getHeight()/3.0f);
			else
				y = (par.getHeight()/3.0f)-y;
			y /= (par.getHeight()/3.0f);
			y = scrollDirection*y*dt*680.0f;
			scroll(0, y);
		}
		
		boolean highlighted = false;
		for(MenuItem item : items) {
			item.hover = false;
			if(item != null && !highlighted) {
				boolean isHovered;
				if(item.fixed) {
					isHovered = (input.mouse.x >= item.x)
							 && (input.mouse.x <= item.x+item.w)
							 && (input.mouse.y >= item.y)
							 && (input.mouse.y <= item.y+item.h);
				} else
					isHovered = (input.mouse.x >= item.x-scrollOffset.x)
							 && (input.mouse.x <= item.x-scrollOffset.x+item.w)
							 && (input.mouse.y >= item.y-scrollOffset.y)
							 && (input.mouse.y <= item.y-scrollOffset.y+item.h);
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
		
		if(queueNewOverlay) {
			overlayDelayAction = par.getDelayedProcessor().queue(randFloat(5, 10), new Runnable() {
				@Override
				public void run() {
					curOverlay = randInt(overlays.length-1);
					overlays[curOverlay].anim.justFinished = false;
					overlays[curOverlay].anim.time = 0;
					overlaySource.play(overlays[curOverlay].sound);
				}
			});
			queueNewOverlay = false;
		} else {
			if(curOverlay != -1) {
				overlays[curOverlay].anim.step(dt);
				if(overlays[curOverlay].anim.justFinished) {
					curOverlay = -1;
					queueNewOverlay = true;
				}
			}
		}
		
//		if(overlays.length != 0) {
//			if(curOverlay == -1 && playRandom) {
//				curOverlay = randInt(overlays.length-1);
//				overlaySource.play(overlays[curOverlay].sound);
//			} else if(curOverlay != -1) {
//				if(overlays[curOverlay].anim.justFinished && playRandom) {
//					curOverlay = randInt(overlays.length-1);
//					overlays[curOverlay].anim.justFinished = false;
//					overlaySource.play(overlays[curOverlay].sound);
//				} else {
//					overlays[curOverlay].anim.step(dt);
//				}
//			}
//		}
	}
	
	public void scroll(float x, float y) {
		if(!canScroll)
			return;
		scrollOffset.x += x;
		scrollOffset.y += y;
		if(scrollOffset.x < 0)
			scrollOffset.x = 0;
		if(scrollOffset.y < 0)
			scrollOffset.y = 0;
		float limy = bgImage.getImageHeight() - par.getHeight();
		if(scrollOffset.y > limy)
			scrollOffset.y = limy;
		float limx = bgImage.getImageWidth() - par.getWidth();
		if(scrollOffset.x > limx)
			scrollOffset.x = limx;
	}
	
	public void stop() {
		par.getDelayedProcessor().dequeue(overlayDelayAction);
		overlaySource.stop();
	}
	
	private static int randInt(int max) {
		return (int) Math.floor(Math.random() * (max+1));
	}
	
	private static float randFloat(float start, float stop) {
		return (float) (start + Math.random() * (stop-start));
	}
	
	public static class Overlay {
		
		public int x, y;
		public FLHAnimation anim;
		public SoundClip sound;
		
	}
	
}
