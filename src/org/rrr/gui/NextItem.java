package org.rrr.gui;

import org.rrr.cfg.LegoConfig.Node;

public class NextItem extends MenuItem {
	
	public String menuLink;
	
	public NextItem(String key, Node cfg) {
		
		name = key;
		String cfgStr = cfg.getValue(key);
		
		String[] split = cfgStr.split(":");
		
		x = Integer.parseInt(split[1]);
		y = Integer.parseInt(split[2]);
		banner = split[3];
		
		menuLink = split[split.length-1];
		
	}
	
}
