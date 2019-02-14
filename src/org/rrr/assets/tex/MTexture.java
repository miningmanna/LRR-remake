package org.rrr.assets.tex;

import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.opengl.Texture;

public class MTexture implements Texture {
	
	private int id;
	private int filter;
	
	public MTexture(int id) {
		this.id = id;
	}
	
	@Override
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, id);
	}

	@Override
	public float getHeight() {
		return 0;
	}

	@Override
	public int getImageHeight() {
		return 0;
	}

	@Override
	public int getImageWidth() {
		return 0;
	}

	@Override
	public byte[] getTextureData() {
		return null;
	}

	@Override
	public int getTextureHeight() {
		return 0;
	}

	@Override
	public int getTextureID() {
		return id;
	}

	@Override
	public String getTextureRef() {
		return null;
	}

	@Override
	public int getTextureWidth() {
		return 0;
	}

	@Override
	public float getWidth() {
		return 0;
	}

	@Override
	public boolean hasAlpha() {
		return false;
	}

	@Override
	public void release() {
	}

	@Override
	public void setTextureFilter(int filter) {
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
	}

}
