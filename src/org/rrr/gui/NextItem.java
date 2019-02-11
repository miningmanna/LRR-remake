package org.rrr.gui;

import org.rrr.assets.LegoConfig.Node;

public class NextItem extends MenuItem {
	
	public String menuLink;
	
	public NextItem(String key, Node cfg) {
		this(key, cfg.getValue(key));
	}
	
	public NextItem(String key, String cfgStr) {
		
		name = key;
		
		String[] split = cfgStr.split(":");
		
		x = Integer.parseInt(split[1]);
		y = Integer.parseInt(split[2]);
		banner = split[3].trim().replace("_", " ");
		
		menuLink = split[split.length-1];
		
	}
	
}
