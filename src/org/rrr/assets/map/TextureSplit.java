package org.rrr.assets.map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.newdawn.slick.opengl.Texture;
import org.rrr.assets.AssetManager;
import org.rrr.assets.LegoConfig.Node;

public class TextureSplit {
	
	public Texture atlas;
	public int w, h;
	
	public void genAtlas(String split, AssetManager am) {
		
		if(split.contains("::"))
			split = split.split("::")[1];
		Node splitCfg = (Node) am.getConfig().get("Lego*/Textures/" + split);
		w = splitCfg.getInteger("surftextwidth");
		h = splitCfg.getInteger("surftextheight");
		String baseName = splitCfg.getValue("texturebasename");
		
		int mw = 0, mh = 0;
		BufferedImage[] imgs = new BufferedImage[w*h];
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				String path = baseName + x + y + ".bmp";
				if(am.exists(path)) {
					int i = y*w+x;
					try {
						InputStream in = am.getAsset(path);
						if(in == null)
							continue;
						try {
							imgs[i] = ImageIO.read(in);
							if(imgs[i].getWidth() > mw)
								mw = imgs[i].getWidth();
							if(imgs[i].getHeight() > mh)
								mh = imgs[i].getWidth();
						} finally {
							in.close();
						}
					} catch(Exception e) {}
				}
			}
		}
		
		BufferedImage res = new BufferedImage(mw*w, mh*h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) res.getGraphics();
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				int i = y*w+x;
				if(imgs[i] != null) {
					g.drawImage(imgs[i], x*mw, y*mh, mw, mh, null);
				}
			}
		}
		atlas = am.geTLoader().getTexture(res);
	}
	
	public int toIndex(Vector2i v) {
		return v.y*w+v.x;
	}
	public int toIndex(int x, int z) {
		return z*w+x;
	}
	public Vector2f toGLPos(Vector2i pos) {
		return toGLPos(pos.x, pos.y);
	}
	public Vector2f toGLPos(int x, int z) {
		Vector2f res = new Vector2f(x, z);
		res.mul(1.0f/w, 1.0f/h);
		return res;
	}
}
