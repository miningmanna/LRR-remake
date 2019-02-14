package org.rrr.assets.model;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.Texture;
import org.rrr.assets.map.MapData;
import org.rrr.assets.tex.TexLoader;

public class ModelLoader {
	
	private ArrayList<Integer> vaos, vbos;
	private HashMap<String, CTexModel> ctexmodels;
	
	public ModelLoader() {
		vaos = new ArrayList<>();
		vbos = new ArrayList<>();
		ctexmodels = new HashMap<>();
	}
	
	public ColorModel getColorModel(float[] verts, float[] colors, int[] inds) {
		
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
		vaos.add(vao);
		
		loadVertexIntoVBO(0, verts, 3);
		loadVertexIntoVBO(1, colors, 3);
		
		int indVbo = glGenBuffers();
		vbos.add(indVbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indVbo);
		IntBuffer intBuff = BufferUtils.createIntBuffer(inds.length);
		intBuff.put(inds);
		intBuff.flip();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuff, GL_STATIC_READ);
		
		ColorModel model = new ColorModel(vao, inds.length);
		return model;
		
	}
	
	public MapMesh getMapMeshFromMapData(MapData data) {
		
		MapMesh res = new MapMesh();
		
		
		
		return res;
		
	}
	
	public CTexModel getCtexModelFromLwobFile(File f, PathConverter finder, TexLoader tLoader) throws IOException {
		
		if(ctexmodels.containsKey(f.getAbsolutePath())) {
			return ctexmodels.get(f.getAbsolutePath());
		}
		
		if(finder == null) {
			System.out.println("Finder cant be null");
			return null;
		}
		File uvFile  = new File(f.getParent(), f.getName().substring(0, f.getName().length()-3) + "uv");
		boolean hasUv = new File(f.getParent(), f.getName().substring(0, f.getName().length()-3) + "uv").exists();
		
		LwobFileData lfd = LwobFileData.getLwobFileData(f);
		
		CTexModelData ctmd = null;
		if(hasUv) {
			ctmd = CTexModelData.getCTexModelFromLwob(lfd, UvFileData.getUvFileData(uvFile));
		} else {
			ctmd = CTexModelData.getCTexModelFromLwob(lfd, null);
		}
		
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
		vaos.add(vao);
		
		loadVertexIntoVBO(0, ctmd.v,  3);
		loadVertexIntoVBO(1, ctmd.vc, 3);
		loadVertexIntoVBO(2, ctmd.vt, 3);
		
		int indVbo = glGenBuffers();
		vbos.add(indVbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indVbo);
		IntBuffer intBuff = BufferUtils.createIntBuffer(ctmd.iv.length);
		intBuff.put(ctmd.iv);
		intBuff.flip();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuff, GL_STATIC_READ);
		
		CTexModel ctm = new CTexModel();
		ctm.vao = vao;
		ctm.surfLen = new int[ctmd.surfLen.length];
		System.arraycopy(ctmd.surfLen, 0, ctm.surfLen, 0, ctmd.surfLen.length);
		ctm.surfStart = new int[ctmd.surfLen.length];
		ctm.texs = new Texture[ctmd.texs.length][];
		ctm.alpha = new Vector3f[ctmd.texs.length];
		ctm.additive = new boolean[ctmd.texs.length];
		System.arraycopy(ctmd.additive, 0, ctm.additive, 0, ctmd.additive.length);
		ctm.doubleSided = new boolean[ctmd.texs.length];
		System.arraycopy(ctmd.doubleSided, 0, ctm.doubleSided, 0, ctmd.doubleSided.length);
		
		int surfStart = 0;
		for(int i = 0; i < ctm.surfLen.length; i++) {
			ctm.surfStart[i] = surfStart;
			surfStart += ctm.surfLen[i];
		}

		LinkedList<Integer> opaque = new LinkedList<>();
		LinkedList<Integer> translucent = new LinkedList<>();
		int lopaque = 0;
		int ltranslucent = 0;
		
		for(int i = 0; i < ctmd.additive.length; i++) {
			if(ctmd.additive[i]) {
				ltranslucent++;
				translucent.add(i);
			} else {
				lopaque++;
				opaque.add(i);
			}
		}
		
		ctm.opaque = new int[lopaque];
		for(int i = 0; i < lopaque; i++)
			ctm.opaque[i] = opaque.pop();
		
		ctm.translucent = new int[ltranslucent];
		for(int i = 0; i < ltranslucent; i++)
			ctm.translucent[i] = translucent.pop();
		
		for(int i = 0; i < ctmd.texs.length; i++) {
			if(ctmd.texs[i] == null) {
				ctm.alpha[i] = null;
				continue;
			}
			String path = finder.convert(ctmd.texs[i]);
			
			if(path == null) {
				ctm.alpha[i] = null;
				continue;
			}
			String extension = "";
			int k = path.lastIndexOf('.');
			if (k > 0)
				extension = path.substring(k+1);
			
			if(ctmd.sequenced[i]) {
				LinkedList<Texture> _texs = new LinkedList<>();
				int texNum = 0;
				int texOffset = 0;
				int lzeros = 1;
				int oneIndex = path.lastIndexOf("1");
				while(path.charAt(oneIndex-1) == '0') {
					oneIndex--;
					lzeros++;
				}
				String prefix = path.substring(0, path.length()-(4+lzeros));
				boolean getTexs = true;
				for(int j = 0; j < 9999; j++) {
					File texFile = new File(prefix + String.format("%0" + lzeros + "d", texOffset) + "." + extension);
					if(texFile.exists()) {
						break;
					}
					texOffset++;
				}
				if(texOffset == 9999)
					System.out.println("over 9000?????");
				while(getTexs) {
					File texFile = new File(prefix + String.format("%0" + lzeros + "d", texOffset+texNum) + "." + extension);
					if(!texFile.exists()) {
						break;
					}
					_texs.add(tLoader.getTexture(extension, texFile));
					texNum++;
				}
				ctm.texs[i] = new Texture[texNum];
				for(int j = 0; j < texNum; j++) {
					ctm.texs[i][j] = _texs.pop();
				}
				
			} else {
				ctm.texs[i] = new Texture[1];
				Texture t = tLoader.getTexture(extension, new File(path));
				if(t == null) {
					ctm.alpha[i] = null;
					continue;
				}
				ctm.texs[i][0] = t;
			}
			
			String fName = new File(path).getName();
			
			if(fName.matches("[Aa]\\d{3}.+$")) {
				
				int aind = Integer.parseInt(fName.substring(1, 4));
				
				ctm.alpha[i] = tLoader.getColorFromBMPPalet(new File(path), aind);
				
			}
			
		}
		
		ctm.texIndex = 0;
		
		return ctm;
	}
	
	private int loadVertexIntoVBO(int pos, float[] verts, int dim) {
		
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		vbos.add(vbo);
		
		FloatBuffer buff = BufferUtils.createFloatBuffer(verts.length);
		buff.put(verts);
		buff.flip();
		
		glBufferData(GL_ARRAY_BUFFER, buff, GL_STATIC_DRAW);
		glVertexAttribPointer(pos, dim, GL_FLOAT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		return vbo;
	}
	
	@SuppressWarnings("unused")
	private int loadIntegerIntoVBO(int pos, int[] ints, int dim) {
		int vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		vbos.add(vbo);
		
		IntBuffer buff = BufferUtils.createIntBuffer(ints.length);
		buff.put(ints);
		buff.flip();
		
		glBufferData(GL_ARRAY_BUFFER, buff, GL_STATIC_DRAW);
		glVertexAttribPointer(pos, dim, GL_INT, false, 0, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		return vbo;
	}
	
	public void destroy() {
		for(int vbo:vbos)
			glDeleteBuffers(vbo);
		for(int vao:vaos)
			glDeleteVertexArrays(vao);
	}
	
	public ObjFileData getObjFileData(File f) throws IOException {
		return ObjFileData.getObjFileData(f);
	}

	public int getUiModel() {
		
		float[] verts = {
				0, 0, 0,
				1, 0, 0,
				0, -1, 0,
				0, -1, 0,
				1, -1, 0,
				1, 0, 0
		};
		float[] texpos = {
				0, 0,
				1, 0,
				0, 1,
				0, 1,
				1, 1,
				1, 0,
		};
		int[] inds = {
				0, 1, 2,
				3, 4, 5,
		};
		
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
		vaos.add(vao);
		
		loadVertexIntoVBO(0, verts, 3);
		loadVertexIntoVBO(1, texpos, 2);
		
		int indVbo = glGenBuffers();
		vbos.add(indVbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indVbo);
		IntBuffer intBuff = BufferUtils.createIntBuffer(inds.length);
		intBuff.put(inds);
		intBuff.flip();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuff, GL_STATIC_READ);
		
		glBindVertexArray(0);
		
		return vao;
	}
	
	public static BufferedImage getBMP(File f) {
		
		BufferedImage img = null;
		try {
			InputStream in = new FileInputStream(f);
			byte[] header = new byte[54];
			
			in.read(header);
			
			int w = getIntLE(header, 18);
			int h = getIntLE(header, 22);
			
			int paletteSize = (int) Math.sqrt(getIntLE(header, 46));
			if(paletteSize == 0)
				paletteSize = 16;
			
			int[] palette = new int[paletteSize*paletteSize];
			
			byte[] bpalette = new byte[paletteSize*paletteSize*4];
			
			in.read(bpalette);
			
			for(int i = 0; i < bpalette.length; i += 4) {
				int rgb = 0;
				rgb |= (0xFF & (255-bpalette[i+3])) << 24;
				rgb |= (0xFF & bpalette[i+2]) << 16;
				rgb |= (0xFF & bpalette[i+1]) << 8;
				rgb |= (0xFF & bpalette[i]);
				palette[i/4] = rgb;
			}
			
			img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			
			short bitsPerPix = getShortLE(header, 28);
			if(bitsPerPix != 8)
				System.out.println("UNUSUAL BITS PER PIX");
			
			int compression = getIntLE(header, 30);
			if(compression != 0)
				System.out.println("USES COMPRESSION");
			
			int rowSize = (int) Math.ceil((bitsPerPix*w)/32)*4;
			
			for(int i = 0; i < h; i++) {
				
				byte[] row = new byte[rowSize];
				in.read(row);
				for(int j = 0; j < w; j++) {
					img.setRGB(j, h-1-i, palette[0x00FF & row[j]]);
				}
				
			}
			
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return img;
	}
	
	private static short getShortLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 2; i++) {
			res = res | (0x000000FF & b[off+1-i]);
			if(i != 1)
				res = res << 8;
		}
		
		return (short) res;
		
	}
	
	private static int getIntLE(byte[] b, int off) {
		
		int res = 0;
		for(int i = 0; i < 4; i++) {
			res = res | (0x000000FF & b[off+3-i]);
			if(i != 3)
				res = res << 8;
		}
		
		return res;
		
	}
	
}
