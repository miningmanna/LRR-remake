package org.rrr.assets.model;

import org.rrr.assets.AssetManager;

public class ModelPathConverter implements PathConverter {
	
	private String dir, sharedDir;
	private AssetManager am;
	
	public ModelPathConverter(String dir, String sharedDir, AssetManager am) {
		this.dir = dir.replaceAll("\\\\", "/");
		if(!this.dir.endsWith("/"))
			this.dir += "/";
		this.sharedDir = sharedDir.replaceAll("\\\\", "/");
		if(!this.sharedDir.endsWith("/"))
			this.sharedDir += "/";
		this.am = am;
	}
	
	@Override
	public String convert(String input) {
		if(input.matches(".*[\\\\\\/].*")) {
			String[] split = input.split("[\\\\\\/]");
			input = split[split.length-1];
		}
		input = input.toUpperCase();
		if(am.exists(dir + input))
			return dir+input;
		else
			return sharedDir+input;
	}

}
