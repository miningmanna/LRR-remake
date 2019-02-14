package org.rrr.assets;

import java.io.File;
import java.io.IOException;

import org.newdawn.slick.opengl.Texture;
import org.rrr.Shader;
import org.rrr.assets.LegoConfig.Node;
import org.rrr.assets.model.LwsAnimation;
import org.rrr.assets.model.ModelLoader;
import org.rrr.assets.model.ModelPathConverter;
import org.rrr.assets.model.PathConverter;
import org.rrr.assets.sound.SoundClip;
import org.rrr.assets.sound.SoundLoader;
import org.rrr.assets.tex.FLHAnimation;
import org.rrr.assets.tex.TexLoader;
import org.rrr.gui.BitMapFont;
import org.rrr.gui.Cursor;

public class AssetManager {
	
	private ModelLoader mLoader;
	private TexLoader	tLoader;
	private SoundLoader sLoader;
	
	private File mShared;
	
	public AssetManager(File mShared) {
		this.mShared = mShared;
		mLoader = new ModelLoader();
		tLoader = new TexLoader();
		sLoader = new SoundLoader();
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

	public Cursor getCursor(Node node) {
		Cursor c = new Cursor();
		c.init(node, tLoader);
		return c;
	}

	public int getUiModel() {
		return mLoader.getUiModel();
	}
	
	public Shader getShader(String path) {
		return new Shader(new File(path + ".vert"), new File(path + ".frag"));
	}
	
	public FLHAnimation getFLHAnimation(File f) {
		return tLoader.getAnimation(f);
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
		System.out.println(ext);
		String[] split = ext.split("\\.");
		System.out.println(split.length);
		ext = split[split.length-1];
		if(ext.equalsIgnoreCase("wav")) {
			try {
				return sLoader.getWavClip(f);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
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
	
}
