package org.rrr;

import java.io.UnsupportedEncodingException;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.rrr.assets.AssetManager;
import org.rrr.assets.model.CTexModel;
import org.rrr.assets.model.ColorModel;
import org.rrr.assets.model.ModelLoader;
import org.rrr.assets.model.LwsAnimation;
import org.rrr.assets.model.MapMesh;
import org.rrr.gui.BitMapFont;
import org.rrr.gui.Cursor;
import org.rrr.gui.Cursor.CursorAnimation;
import org.rrr.gui.Menu;
import org.rrr.gui.Menu.Overlay;
import org.rrr.gui.MenuItem;
import org.rrr.gui.NextItem;
import org.rrr.gui.TriggerItem;
import org.rrr.level.Entity;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Renderer {
	
	public float pWidth = RockRaidersRemake.WIDTH, pHeight = RockRaidersRemake.HEIGHT;
	
	private int uiVao;
	public void init(AssetManager am) {
		
		
		System.out.println("VAO BEFORE: " + uiVao);
		uiVao = am.getUiModel();
		System.out.println("VAO AFTER " + uiVao);
		
	}
	
	// TODO - reimplement
//	public void render(MapMesh mesh, Shader s) {
//		
//		glBindVertexArray(mesh.vao);
//		glEnableVertexAttribArray(0);
//		glEnableVertexAttribArray(1);
//		glEnableVertexAttribArray(2);
//		s.setUniVector3f("pos", mesh.pos);
//		glActiveTexture(GL_TEXTURE0);
//		int texIndex = 0;
//		s.setUniBoolean("lines", false);
//		
//		for(int i = 0; i < mesh.height; i++) {
//			for(int j = 0; j < mesh.width; j++) {
//				texIndex = mesh.tex[i][j];
//				glBindTexture(GL_TEXTURE_2D, mesh.texs.get(texIndex).getTextureID());
//				glDrawElements(GL_TRIANGLES, 4*3, GL_UNSIGNED_INT, (i*mesh.width+j)*3*4*4);
//			}
//		}
////		s.setUniBoolean("lines", true);
////		for(int i = 0; i < mesh.height; i++)
////			glDrawElements(GL_LINE_STRIP, mesh.indCount/mesh.height, GL_UNSIGNED_INT, i*mesh.width*3*4*4);
//		glDisableVertexAttribArray(0);
//		glDisableVertexAttribArray(1);
//		glDisableVertexAttribArray(2);
//		glBindVertexArray(0);
//		
//	}
	
	public void render(ColorModel model) {
		
		glBindVertexArray(model.vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawElements(GL_TRIANGLES, model.indCount, GL_UNSIGNED_INT, 0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		
	}
	
	public void render(ColorModel model, Texture tex, int frame) {
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, tex.getTextureID());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		render(model);
		glBindTexture(GL_TEXTURE_2D, 0);
		
		
	}
	
	public void render(CTexModel cmodel, Shader s) {
		
		glActiveTexture(GL_TEXTURE0);
		
		glBindVertexArray(cmodel.vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		
		s.setUniBoolean("calcAlpha", false);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(true);
		int ind = 0;
		for(int i = 0; i < cmodel.opaque.length; i++) {
			ind = cmodel.opaque[i];
			if(cmodel.doubleSided[ind]) {
				glDisable(GL_CULL_FACE);
			} else {
				glEnable(GL_CULL_FACE);
				glCullFace(GL_FRONT);
			}
			if(cmodel.texs[ind] != null) {
				glBindTexture(GL_TEXTURE_2D, cmodel.texs[ind][cmodel.texIndex%cmodel.texs[ind].length].getTextureID());
				if(cmodel.alpha[i] != null)
					s.setUniVector3f("aColor", cmodel.alpha[i]);
				else
					s.setUniVector3f("aColor", new Vector3f(1, -1, -1));
			} else {
				s.setUniVector3f("aColor", new Vector3f(-1));
			}
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glDrawElements(GL_TRIANGLES, cmodel.surfLen[ind], GL_UNSIGNED_INT, cmodel.surfStart[ind]*4);
		}
		
		s.setUniBoolean("calcAlpha", true);
		glDepthMask(false);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE);
		for(int i = 0; i < cmodel.translucent.length; i++) {
			ind = cmodel.translucent[i];
			if(cmodel.doubleSided[ind]) {
				glDisable(GL_CULL_FACE);
			} else {
				glEnable(GL_CULL_FACE);
				glCullFace(GL_FRONT);
			}
			if(cmodel.texs[ind] != null) {
				glBindTexture(GL_TEXTURE_2D, cmodel.texs[ind][cmodel.texIndex%cmodel.texs[ind].length].getTextureID());
				if(cmodel.alpha[i] != null)
					s.setUniVector3f("aColor", cmodel.alpha[i]);
				else
					s.setUniVector3f("aColor", new Vector3f(1, -1, -1));
			} else {
				s.setUniVector3f("aColor", new Vector3f(-1));
			}
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glDrawElements(GL_TRIANGLES, cmodel.surfLen[ind], GL_UNSIGNED_INT, cmodel.surfStart[ind]*4);
		}
		glDepthMask(true);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void render(Entity e, Shader s) {
		
		Matrix4f temp = new Matrix4f(e.rot);
		temp._m30(temp.m30()+e.pos.x);
		temp._m31(temp.m31()+e.pos.y);
		temp._m32(temp.m32()+e.pos.z);
		s.setUniMatrix4f("modelTrans", temp);
		render(e.anims[e.currentAnimation], s);
		
	}
	
	public void render(LwsAnimation anim, Shader s) {
		
		for(int i = 0; i < anim.bd.lobjects; i++) {
			
			if(anim.bd.models[i] != null) {
				s.setUniMatrix4f("animTrans", anim.transforms[i]);
				s.setUniFloat("framealpha", anim.alpha[i]);
				anim.bd.models[i].texIndex = anim.frame;
				render(anim.bd.models[i], s);
			}
			
		}
		
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		
	}
	
	public void render(Cursor cursor, Shader s) {
		glDepthMask(true);
		glDepthFunc(GL_ALWAYS);
		s.setUniMatrix4f("trans", new Matrix4f().identity());
		Vector2f scale = new Vector2f(transWidth(cursor.w), transHeight(cursor.h));
		s.setUniVector2f("scale", scale);
		Vector2f texScale = new Vector2f(1, 1);
		s.setUniVector2f("texScale", texScale);
		Vector2f texPos = new Vector2f(0, 0);
		s.setUniVector2f("texOffset", texPos);
		Vector3f translate = new Vector3f(transWidth(cursor.x)-1f, 1f-transHeight(cursor.y), 0);
		s.setUniVector3f("translate", translate);
		glDisable(GL_CULL_FACE);
		
		glBindVertexArray(uiVao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		if(cursor.curAnimation == -1) {
			cursor.base.bind();
			glBindTexture(GL_TEXTURE_2D, cursor.base.getTextureID());
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		} else {
			CursorAnimation anim = cursor.animations[cursor.curAnimation];
			if(anim.usesBaseTex) {
				glBindTexture(GL_TEXTURE_2D, cursor.base.getTextureID());
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
			}
			
			if(anim.stillFrame)
				glBindTexture(GL_TEXTURE_2D, anim.tex.getTextureID());
			else
				glBindTexture(GL_TEXTURE_2D, anim.anim.data.frames[anim.anim.frame].getTextureID());
			
			scale.x = transWidth(anim.w);
			scale.y = transHeight(anim.h);
			translate.x = transWidth(cursor.x+anim.x)-1f;
			translate.y = 1f-transHeight(cursor.y+anim.y);
			s.setUniVector2f("scale", scale);
			s.setUniVector3f("translate", translate);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		}
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		glDepthFunc(GL_LESS);
	}
	
	public void render(Menu menu, Shader s) {
		
		glDepthMask(true);
		glDepthFunc(GL_ALWAYS);
		
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		
		s.setUniMatrix4f("trans", new Matrix4f().identity());
		Vector2f scale = new Vector2f(	transWidth(menu.bgImage.getImageWidth()),
										transHeight(menu.bgImage.getImageHeight()));
		s.setUniVector2f("scale", scale);
		Vector2f texScale = new Vector2f(menu.bgImage.getWidth(), menu.bgImage.getHeight());
		s.setUniVector2f("texScale", texScale);
		Vector2f texPos = new Vector2f(0, 0);
		s.setUniVector2f("texOffset", texPos);
		Vector3f translate = new Vector3f(-1, 1, 0);
		s.setUniVector3f("translate", translate);
		
		glDisable(GL_CULL_FACE);
		glBindVertexArray(uiVao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glBindTexture(GL_TEXTURE_2D, menu.bgImage.getTextureID());
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		
		
		if(menu.curOverlay != -1) {
			Overlay overlay = menu.overlays[menu.curOverlay];
			s.setUniMatrix4f("trans", new Matrix4f().identity());
			scale = new Vector2f(	transWidth(overlay.anim.data.w),
									transHeight(overlay.anim.data.h));
			s.setUniVector2f("scale", scale);
			texScale = new Vector2f(1, 1);
			s.setUniVector2f("texScale", texScale);
			translate = new Vector3f(transWidth(overlay.x)-1, 1-transHeight(overlay.y), 0);
			s.setUniVector3f("translate", translate);
			
			glBindTexture(GL_TEXTURE_2D, overlay.anim.data.frames[overlay.anim.frame].getTextureID());
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		}
		
		if(menu.displayTitle) {
			drawString(s, menu.x-(menu.menuFont.getPixLength(menu.fullName)/2), menu.y, menu.loFont, menu.fullName, 1);
		}
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		
		for(MenuItem item : menu.items) {
			if(item instanceof NextItem) {
				drawNextItem(menu, (NextItem) item, s);
			} else if (item instanceof TriggerItem) {
				drawTriggerItem(menu, (TriggerItem) item, s);
			}
		}
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		glDepthMask(true);
		glDepthFunc(GL_LESS);
	}
	
	private void drawNextItem(Menu menu, NextItem item, Shader s) {
		
		if(!item.isImage) {
			if(item.hover)
				drawString(s, item.x, item.y, menu.hiFont, item.banner, 1);
			else
				drawString(s, item.x, item.y, menu.loFont, item.banner, 1);
		} else {
			s.setUniMatrix4f("trans", new Matrix4f().identity());
			Vector2f scale = new Vector2f(	transWidth(item.normTex.getImageWidth()),
											transHeight(item.normTex.getImageHeight()));
			s.setUniVector2f("scale", scale);
			Vector2f texScale = new Vector2f(item.normTex.getWidth(), item.normTex.getHeight());
			s.setUniVector2f("texScale", texScale);
			Vector2f texPos = new Vector2f(0, 0);
			s.setUniVector2f("texOffset", texPos);
			Vector3f translate = new Vector3f(transWidth(item.x)-1, 1-transHeight(item.y), 0);
			s.setUniVector3f("translate", translate);
			
			glDisable(GL_CULL_FACE);
			glBindVertexArray(uiVao);
			glEnableVertexAttribArray(0);
			glEnableVertexAttribArray(1);
			if(item.hover)
				glBindTexture(GL_TEXTURE_2D, item.hiTex.getTextureID());
			else
				glBindTexture(GL_TEXTURE_2D, item.normTex.getTextureID());
			
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
			glDisableVertexAttribArray(0);
			glDisableVertexAttribArray(1);
			glBindVertexArray(0);
		}
	}
	
	private void drawTriggerItem(Menu menu, TriggerItem item, Shader s) {
		
		if(item.hover)
			drawString(s, item.x, item.y, menu.hiFont, item.banner, 1);
		else
			drawString(s, item.x, item.y, menu.loFont, item.banner, 1);
	}
	
	public void drawString(Shader s, int x, int y, BitMapFont f, String str, float scalef) {
		
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(true);
		
		byte[] inds = null;
		try {
			inds = str.getBytes("Cp850");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		glDisable(GL_CULL_FACE);
		
		glBindVertexArray(uiVao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1); 
		
		Vector3f trans = new Vector3f();
		Vector2f tScale = new Vector2f(1, 1);
		Vector2f tPos = new Vector2f();
		Vector2f scale = new Vector2f(transWidth(f.blockLengthX)*scalef, transHeight(f.blockLengthY)*scalef);
		int px = 0;
		glBindTexture(GL_TEXTURE_2D, f.atlas.getTextureID());
		f.atlas.setTextureFilter(GL11.GL_NEAREST);
		tScale.y = f.glBlockLengthY;
		for(int i = 0; i < inds.length; i++) {
			
			trans.x = transWidth(x + (px*scalef))-1f;
			trans.y = 1f-transHeight(y);
			
			int ind = (0x00FF & inds[i])-32;
			
			scale.x = transWidth(f.widths[ind])*scalef;
			
			tScale.x = f.glWidths[ind];
			
			tPos.x = (ind%10)*f.glBlockLengthX;
			tPos.y = (Math.floorDiv(ind, 10))*f.glBlockLengthY;
			
			s.setUniVector3f("translate", trans);
			s.setUniVector2f("scale", scale);
			s.setUniVector2f("texScale", tScale);
			s.setUniVector2f("texOffset", tPos);
			
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
			
			px += f.widths[ind];
		}
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		
		glEnable(GL_CULL_FACE);
		glCullFace(GL_FRONT);
		
		glDepthMask(false);
	}
	
	public float transWidth(float x) {
		return x*2.0f/pWidth;
	}
	public float transHeight(float y) {
		return y*2.0f/pHeight;
	}
}
