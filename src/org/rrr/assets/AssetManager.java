package org.rrr.assets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.lwjgl.opengl.AMDBlendMinmaxFactor;
import org.newdawn.slick.opengl.Texture;
import org.rrr.Input;
import org.rrr.RockRaidersRemake;
import org.rrr.Shader;
import org.rrr.assets.sound.SoundStream;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.map.Map;
import org.rrr.assets.model.LwsAnimation;
import org.rrr.assets.model.ModelLoader;
import org.rrr.assets.model.ModelPathConverter;
import org.rrr.assets.model.PathConverter;
import org.rrr.assets.sound.SoundClip;
import org.rrr.assets.sound.SoundLoader;
import org.rrr.assets.tex.FLHAnimation;
import org.rrr.assets.tex.TexLoader;
import org.rrr.assets.wad.WadFile;
import org.rrr.gui.BitMapFont;
import org.rrr.gui.Cursor;

public class AssetManager {
	
	private ModelLoader mLoader;
	private TexLoader	tLoader;
	private SoundLoader sLoader;
	
	private LegoConfig cfg;
	private Node splits;
	
	private HashMap<String, Asset> assets;
	
	private String mShared;
	
	public AssetManager(File fPriorities) throws IOException {
		
		assets = new HashMap<>();
		
		String[] priorities = readLines(fPriorities);
		System.out.println("ASSET PRIORITIES: ");
		String[] orig = {
			"\\\\",
			"\\.",
			"\\(",
			"\\)",
			"\\[",
			"\\]",
			"%"
		};
		String[] repl = {
			"\\/",
			"\\\\.",
			"\\\\(",
			"\\\\)",
			"\\\\[",
			"\\\\]",
			"(\\\\\\d+)"
		};
		
		for(int i = 0; i < priorities.length; i++) {
			String priority = priorities[i];
			for(int j = 0; j < orig.length; j++)
				priority = priority.replaceAll(orig[j], repl[j]);
			System.out.println(i + " : " + priorities[i]);
			File dir = null;
			if(priority.matches(".+\\/.+")) {
				String[] split = priority.split("\\/");
				String strDir = split[0];
				for(int j = 1; j <  split.length-1; j++)
					strDir += "/" + split[j];
				dir = new File(strDir);
				priority = split[split.length-1];
			} else {
				dir = new File("./");
			}
			System.out.println("CREATING REGEX: " + priority);
			Pattern regex = Pattern.compile(priority);
			SortedSet<File> _matches = new TreeSet<>(new Comparator<File>() {
				public int compare(File o1, File o2) {
					int v1 = -1, v2 = -1;
					Matcher m1 = regex.matcher(o1.getName()),
							m2 = regex.matcher(o2.getName());
					if(m1.matches() && m1.groupCount() == 1)
						v1 = Integer.parseInt(m1.group(1));
					if(m2.matches() && m2.groupCount() == 1)
						v2 = Integer.parseInt(m2.group(1));
					return v1-v2;
				}
			});
			for(File f : dir.listFiles())
				if(f.getName().matches(priority))
					_matches.add(f);
			
			File[] matches = new File[_matches.size()];
			_matches.toArray(matches);
			for(File f : matches) {
				System.out.println(priority + " matched: " + f);
				registerAssets(f);
			}
		}
		
		for(String key : assets.keySet())
			System.out.println("KEY: " + key);
		
		InputStream cfgIn = getAsset("Lego.cfg");
		cfg = LegoConfig.getConfig(cfgIn, "", this);
		cfgIn.close();
		
		this.mShared = (String) cfg.get("Lego*/Main/SharedObjects");
		mLoader = new ModelLoader();
		tLoader = new TexLoader();
		sLoader = new SoundLoader((Node) cfg.get("Lego*/Samples"), this);
		splits = (Node) cfg.get("Lego*/Textures");
	}
	
	public LegoConfig getConfig() {
		return cfg;
	}
	
	private static String[] readLines(File f) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			LinkedList<String> _lines = new LinkedList<>();
			String line;
			while((line = br.readLine()) != null)
				_lines.add(line);
			br.close();
			int size = _lines.size();
			String[] lines = new String[size];
			for(int i = 0; i < size; i++)
				lines[i] = _lines.pop();
			return lines;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void registerAssets(File f) {
		if(f.isDirectory()) {
			regDir(null, f);
		} else if(f.getName().toUpperCase().endsWith(".WAD")) {
			regWad(f);
		}
	}
	
	private void regWad(File f) {
		WadFile wad = null;
		try {
			wad = WadFile.getWadFile(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(wad == null)
			return;
		
		String[] entries = wad.getEntries();
		for(int i = 0; i < entries.length; i++) {
			String e = entries[i];
			System.out.println("ENTRY: " + e);
			Asset a = new Asset();
			a.isWad = true;
			a.path = e;
			a.wad = wad;
			assets.put(toAssetPath(e), a);
		}
	}
	
	private void regDir(String prePath, File dir) {
		for(File f : dir.listFiles()) {
			if(f.isDirectory()) {
				System.out.println(prePath + " -> " + f.getName());
				if(prePath == null) 
					regDir(f.getName()+"/", f);
				else
					regDir(prePath+f.getName()+"/", f);
			} else {
				Asset a = new Asset();
				a.path = f.getName();
				System.out.println("REGISTERING FILE: " + a.path.toUpperCase());
				a.dir = dir;
				String key;
				if(prePath == null)
					key = f.getName().toUpperCase();
				else
					key = (prePath + f.getName()).toUpperCase();
				assets.put(key, a);
			}
		}
	}
	
	public void destroy() {
		mLoader.destroy();
		tLoader.destroy();
		sLoader.destroy();
	}
	
	public InputStream getAsset(String path) {
		path = toAssetPath(path);
		Asset a = assets.get(path);
		if(a == null)
			return null;
		else
			return a.asStream();
	}
	
	public LwsAnimation getAnimation(String path) {
		Asset a = assets.get(path.toUpperCase());
		
		String[] split = path.split("\\/");
		String parent = "";
		for(int i = 0; i < split.length-1; i++)
			parent += split[i] + "/";
		
		PathConverter conv = new ModelPathConverter(parent, mShared,this);
		LwsAnimation anim = null;
		try {
			anim = LwsAnimation.getAnimation(path, this, conv);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return anim;
		
	}
	
	public LwsAnimation getAnimation(File file, AssetManager am) {
		
		PathConverter conv = new ModelPathConverter(file.getParentFile().getAbsolutePath(), mShared, this);
		LwsAnimation anim = null;
		try {
			anim = LwsAnimation.getAnimation(file.getAbsolutePath(), am, conv);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return anim;
	}
	
	public boolean exists(String path) {
		path = toAssetPath(path);
		System.out.println("CHECKING FOR ASSET: " + path);
		return assets.containsKey(path.toUpperCase());
	}
	
	public BitMapFont getFont(String path) {
		path = toAssetPath(path);
		System.out.println("GETTING FONT: " + path);
		InputStream in = getAsset(path);
		BitMapFont font = BitMapFont.getFont(in, tLoader);
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return font;
	}

	public Cursor getCursor(RockRaidersRemake par, Node node) {
		Cursor c = new Cursor(node, this, par);
		return c;
	}

	public int getUiModel() {
		return mLoader.getUiModel();
	}
	
	public Shader getShader(String path) {
		return new Shader(new File("shaders/" + path + ".vert"), new File("shaders/" + path + ".frag"));
	}
	
	public FLHAnimation getFLHAnimation(File f) {
		return tLoader.getAnimation(f);
	}
	
	public FLHAnimation getFLHAnimation(String path) {
		path = toAssetPath(path);
		FLHAnimation res = null;
		InputStream in = null;
		in = getAsset(path);
		res = tLoader.getAnimation(path, in);
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public void getTexSplit(Map map, String split) {
		if(split.contains("::"))
			split = split.split("::")[1];
		System.out.println("GETTING SPLIT: " + split);
		Node splitCfg = splits.getSubNode(split);
		int sw = splitCfg.getInteger("surftextwidth");
		int sh = splitCfg.getInteger("surftextheight");
		String baseName = splitCfg.getValue("texturebasename");
		System.out.println("CHECKING WITH BASE: " + baseName);
		
		map.mesh.texs = new Texture[sw*sh];
		for(int x = 0; x < sw; x++) {
			for(int y = 0; y < sh; y++) {
				String path = baseName + y + x + ".bmp";
				if(exists(path)) {
					int i = y*sw+x;
					System.out.println("FOUND TEX: " + path);
					map.mesh.texs[i] = getTexture(path);
				}
			}
		}
		
	}
	
	public Texture getTexture(String tPath) {
		tPath = toAssetPath(tPath);
		String[] split = tPath.split("\\.");
		String ext = split[split.length-1];
		Texture res = null;
		try {
			InputStream in = getAsset(tPath);
			System.out.println(in);
			res = tLoader.getTexture(ext, in, tPath);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public SoundClip getSound(String path) {
		String ext = path;
		String[] split = ext.split("\\.");
		System.out.println(split.length);
		ext = split[split.length-1];
		System.out.println(ext);
		SoundClip res = null;
		InputStream in = null;
		try {
			in = getAsset(path);
			res = sLoader.getSoundClip(in);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	public SoundClip getSample(String name) {
		
		if(name.equals("SFX_NULL"))
			return null;
		
		String path = sLoader.getSample(name);
		System.out.println("SAMPLE: " + name + " = " + path);
		if(path == null)
			return null;
		else
			return getSound(path);
		
	}
	
	public SoundStream getSoundStream(String path) {
		try {
			return sLoader.getSoundStream(path);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ModelLoader getMLoader() {
		return mLoader;
	}

	public TexLoader geTLoader() {
		return tLoader;
	}

	public SoundLoader getSLoader() {
		return sLoader;
	}
	
	private static String toAssetPath(String str) {
		return str.replaceAll("\\\\", "/").toUpperCase();
	}
	
	private static class Asset {
		public boolean isWad;
		File dir;
		WadFile wad;
		String path;
		public InputStream asStream() {
			if(isWad)
				return wad.getStream(path);
			else {
				try {
					return new FileInputStream(new File(dir, path));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}
