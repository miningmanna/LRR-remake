package org.rrr;

import java.io.File;
import java.util.HashMap;

import org.rrr.model.PathConverter;

public class RockRaiderPathFilter implements PathConverter {
	
	private File dir, sharedDir;
	private HashMap<String, String> map = new HashMap<>();
	private HashMap<String, String> shared = new HashMap<>();
	
	public RockRaiderPathFilter(File dir, File sharedDir) {
		this.dir = dir;
		this.sharedDir = sharedDir;
		for(File f : dir.listFiles()) {
			String name = f.getName();
			String upName = name.toUpperCase();
			map.put(upName, name);
		}
		for(File f : sharedDir.listFiles()) {
			String name = f.getName();
			String upName = name.toUpperCase();
			shared.put(upName, name);
		}
	}
	
	@Override
	public String convert(String input) {
		String name = new File(input).getName();
		String resName = map.get(name.toUpperCase());
		String res = null;
		if(resName == null) {
			resName = shared.get(name.toUpperCase());
			if(resName == null)
				return null;
			res = new File(sharedDir, resName).getAbsolutePath();
		} else {
			res = new File(dir, resName).getAbsolutePath();
		}
		return res;
	}

}
