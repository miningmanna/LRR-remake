package org.rrr.gui;

import org.newdawn.slick.opengl.Texture;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;

public class Menu {
	
	public int x, y, ax, ay;
	public String title;
	public String fullName;
	public BitMapFont menuFont;
	public BitMapFont hiFont;
	public BitMapFont loFont;
	public Texture bgImage;
	public MenuItem[] items;
	public boolean autoCenter, displayTitle, canScroll;
	
	public Menu(AssetManager am, Node cfg) {
		
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
				item = new TriggerItem(key, cfgStr);
				break;
			case 'S':
				item = new SliderItem(key, cfgStr);
				break;
			case 'N':
				item = new NextItem(key, cfgStr);
				break;
			case 'C':
				item = new CycleItem(key, cfgStr);
				break;
			default:
				break;
			}
			items[i-1] = item;
		}
		
	}
	
}
