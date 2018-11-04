package org.rrr.gui;

import org.rrr.cfg.LegoConfig.Node;

public class CycleItem extends MenuItem {
	
	public int x1, y1;
	public String[] items;
	public int index;
	
	public CycleItem(String key, Node cfg) {
		
		this.name = key;
		
		String cfgStr = cfg.getValue(key);
		
		String[] split = cfgStr.split(":");
		
		if(split.length < 8) {
			System.out.println("Invalid Config for cycle item! :" + key);
		} else {
			
			x	= Integer.parseInt(split[1]);
			y	= Integer.parseInt(split[2]);
			x1	= Integer.parseInt(split[3]);
			y1	= Integer.parseInt(split[4]);
			
			banner = split[5];
			int seln = Integer.parseInt(split[6]);
			items = new String[seln];
			for(int i = 0; i < seln; i++) {
				items[i] = split[7+i];
			}
			
		}
		
	}
	
}
