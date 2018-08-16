package org.rrr;

import org.newdawn.slick.opengl.Texture;
import org.rrr.model.CTexModel;
import org.rrr.model.FullModel;
import org.rrr.model.LwsAnimation;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Vector3f;

public class Renderer {
	
	public void render(FullModel model) {
		
		glBindVertexArray(model.vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawElements(GL_TRIANGLES, model.indCount, GL_UNSIGNED_INT, 0);
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		
	}
	
	public void render(FullModel model, Texture tex, int frame) {
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, tex.getTextureID());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		render(model);
		glBindTexture(GL_TEXTURE_2D, 0);
		
	}
	
	public int surfi = 0;
	public void render(CTexModel cmodel, Shader s) {
		
		glActiveTexture(GL_TEXTURE0);
		
		glBindVertexArray(cmodel.vao);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
		
//		int offset = 0;
//		for(int j = 0; j < cmodel.surfLen.length; j++) {
//			if(cmodel.texs[j] != null)
//				glBindTexture(GL_TEXTURE_2D, cmodel.texs[j].getTextureID());
//			if(cmodel.additive[j]) {
//				s.setUniBoolean("calcAlpha", false);
//				glBlendFunc(GL_SRC_ALPHA, GL_ONE);
//				glDepthMask(false);
//			} else {
//				s.setUniBoolean("calcAlpha", false);
//				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//				glDepthMask(true);
//			}
//			s.setUniVector2f(0, cmodel.alpha[j]);
//			glDrawElements(GL_TRIANGLES, cmodel.surfLen[j], GL_UNSIGNED_INT, offset*4);
//			offset += cmodel.surfLen[j];
//		}
		
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
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glBindVertexArray(0);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void render(LwsAnimation anim, Shader s) {
		
		for(int i = 0; i < anim.lobjects; i++) {
			
			if(anim.models[i] != null) {
				s.setUniMatrix4f("transform", anim.transforms[i]);
				s.setUniFloat("framealpha", anim.alpha[i]);
				anim.models[i].texIndex = anim.frame;
				render(anim.models[i], s);
			}
			
		}
		
	}
	
}
