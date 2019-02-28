package org.rrr.assets;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.newdawn.slick.opengl.Texture;
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
	
	private File mShared;
	
	public AssetManager(LegoConfig cfg, File mShared) {
		assets = new HashMap<>();
		this.mShared = mShared;
		mLoader = new ModelLoader();
		tLoader = new TexLoader();
		sLoader = new SoundLoader((Node) cfg.get("Lego*/Samples"));
		splits = (Node) cfg.get("Lego*/Textures");
	}
	
	public void destroy() {
		mLoader.destroy();
		tLoader.destroy();
		sLoader.destroy();
	}

	public LwsAnimation getAnimation(File file) {
		
		PathConverter conv = new ModelPathConverter(file.getParentFile(), mShared);
		LwsAnimation anim = null;
		try {
			anim = LwsAnimation.getAnimation(file, mLoader, tLoader, conv);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return anim;
	}
	
	public BitMapFont getFont(File f) {
		return BitMapFont.getFont(f, tLoader);
	}
	
	public BitMapFont getFont(String string) {
		File f = locateInLegoRR0(string);
		System.out.println(f);
		return getFont(f);
	}

	public Cursor getCursor(RockRaidersRemake par, Node node) {
		Cursor c = new Cursor(node, tLoader, par);
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
				File f = locateInLegoRR0(baseName + y + x + ".bmp");
				if(f != null) {
					int i = y*sw+x;
					System.out.println("FOUND TEX: " + baseName + y + x + ".bmp");
					try {
						map.mesh.texs[i] = tLoader.getTexture("bmp", f);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	public Texture getTexture(String tPath) {
		File f = locateInLegoRR0(tPath);
		String ext = f.getName();
		String[] split = ext.split("\\.");
		ext = split[split.length-1];
		try {
			return tLoader.getTexture(ext, f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public SoundClip getSound(File f) {
		String ext = f.getName();
		String[] split = ext.split("\\.");
		System.out.println(split.length);
		ext = split[split.length-1];
		System.out.println(ext);
		try {
			return sLoader.getSoundClip(f);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public SoundClip getSample(String name) {
		
		if(name.equals("SFX_NULL"))
			return null;
		
		File f = sLoader.getSample(name);
		System.out.println("SAMPLE: " + name + " = " + f);
		if(f == null)
			return null;
		else
			return getSound(f);
		
	}
	
	public SoundStream getSoundStream(File f) {
		try {
			return sLoader.getSoundStream(f);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private File locateInLegoRR0(String path) {
		String[] split = path.split("[\\\\/]");
		String rpath = "";
		for(int i = 0; i < split.length-1; i++) {
			split[i] = split[i].substring(0, 1).toUpperCase() + split[i].substring(1);
			rpath += split[i] + "/";
		}
		
		split[split.length-1] = split[split.length-1].toUpperCase();
		
		File resFolder = new File("LegoRR0/" + rpath);
		System.out.println(resFolder);
		for(File f : resFolder.listFiles()) {
			if(f.getName().toUpperCase().equals(split[split.length-1]))
				return f;
		}
		
		return null;
	}
	
	private File locateInLegoRR1(String path) {
		String[] split = path.split("[\\\\/]");
		String rpath = "";
		for(int i = 0; i < split.length-1; i++) {
			split[i] = split[i].substring(0, 1).toUpperCase() + split[i].substring(1);
			rpath += split[i] + "/";
		}
		
		split[split.length-1] = split[split.length-1].toUpperCase();
		
		File resFolder = new File("LegoRR1/" + rpath);
		for(File f : resFolder.listFiles()) {
			if(f.getName().toUpperCase().equals(split[split.length-1]))
				return f;
		}
		
		return null;
	}

	public ModelLoader getMLoader() {
		return mLoader;
	}

	public TexLoader geTtLoader() {
		return tLoader;
	}

	public SoundLoader getSLoader() {
		return sLoader;
	}
	
	private static class Asset {
		public boolean isWad;
		File dir;
		WadFile wad;
		String path;
	}
}
