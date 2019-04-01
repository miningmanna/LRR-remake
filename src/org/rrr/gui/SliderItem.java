package org.rrr.gui;

import org.rrr.assets.LegoConfig.Node;

public class SliderItem extends MenuItem {
	
	public int x1, y1;
	public int loLim, hiLim;
	
	public SliderItem(String key, Node cfg, Menu menu) {
		this(key, cfg.getValue(key), menu);
	}
	
	public SliderItem(String key, String cfgStr, Menu menu) {
		
		fixed = true;
		
		name = key;
		
		String[] split = cfgStr.split(":");
		
		x	= Integer.parseInt(split[1]);
		y	= Integer.parseInt(split[2]);
		x1	= Integer.parseInt(split[3]);
		y1	= Integer.parseInt(split[4]);
		
		banner = split[5];
		
		loLim	= Integer.parseInt(split[6]);
		hiLim	= Integer.parseInt(split[7]);
		
	}
	
}
