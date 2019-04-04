package org.rrr.gui;

import org.rrr.RockRaidersRemake;
import org.rrr.assets.LegoConfig.Node;

public class TriggerProcessor {
	
	private RockRaidersRemake par;
	
	public TriggerProcessor(RockRaidersRemake par) {
		this.par = par;
	}
	
	public static final String SETLEVEL_REGEX = "setLevel\\([^)]+\\)";
	
	public void trigger(String func) {
		if(func.equalsIgnoreCase("exit")) {
			par.stop();
		} else if(func.matches(SETLEVEL_REGEX)) {
			String levelName = func.substring(func.indexOf('(')+1, func.indexOf(')'));
			System.out.println("CHANGING LEVEL: " + levelName);
			Node cfg = (Node) par.getAssetManager().getConfig().get("Lego*/Levels/" + levelName);
			if(cfg != null)
				par.setLevel(cfg);
			else
				System.out.println("COULDNT FIND LEVEL");
		}
	}
	
}
