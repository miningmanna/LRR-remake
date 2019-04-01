package org.rrr.gui;

import org.newdawn.slick.opengl.Texture;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;

public class NextItem extends MenuItem {
	
	public boolean isImage;
	public Texture normTex, hiTex, loTex;
	public String menuLink;
	
	public NextItem(String key, Node cfg, Menu menu) {
		this(key, cfg.getValue(key), menu);
	}
	
	public NextItem(String key, String cfgStr, Menu menu) {
		
		fixed = true;
		
		name = key;
		
		String[] split = cfgStr.split(":");
		
		x = menu.x + Integer.parseInt(split[1]);
		y = menu.y + Integer.parseInt(split[2]);
		
		banner = split[3].trim().replace("_", " ");
		
		if(split.length != 5) {
			isImage = true;
			AssetManager am = menu.getAssetManager();
			
			normTex = am.getTexture(split[3]);
			hiTex = am.getTexture(split[4]);
			loTex = am.getTexture(split[5]);
			w = normTex.getImageWidth();
			h = normTex.getImageHeight();
			
		} else {
			isImage = false;
			w = menu.menuFont.getPixLength(banner);
			h = menu.menuFont.blockLengthY;
		}
		menuLink = split[split.length-1];
		
		if(menu.autoCenter)
			x = x - (w/2);
		
	}
	
}
