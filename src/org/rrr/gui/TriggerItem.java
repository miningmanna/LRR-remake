package org.rrr.gui;

import org.rrr.assets.LegoConfig.Node;

public class TriggerItem extends MenuItem {
	
	// Unknown use?
	public int end;
	
	public TriggerItem(String key, Node cfg, Menu menu) {
		this(key, cfg.getValue(key), menu);
	}
	
	public TriggerItem(String key, String cfgStr, Menu menu) {
		
name = key;
		
		String[] split = cfgStr.split(":");
		
		x = menu.x + Integer.parseInt(split[1]);
		y = menu.y + Integer.parseInt(split[2]);
		
		banner = split[3].trim().replace("_", " ");
		
		if(menu.autoCenter)
			x = x - (menu.menuFont.getPixLength(banner)/2);
		
		w = menu.menuFont.getPixLength(banner);
		h = menu.menuFont.blockLengthY;
		
		end = Integer.parseInt(split[split.length-1]);
		
	}
	
}
