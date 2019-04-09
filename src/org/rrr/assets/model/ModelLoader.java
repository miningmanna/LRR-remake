package org.rrr.assets.model;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.Texture;
import org.rrr.assets.AssetManager;
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
		
		loadVertexIntoVBO(0, verts, 3,  GL_STATIC_DRAW);
		loadVertexIntoVBO(1, colors, 3, GL_STATIC_DRAW);
		
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
	
	public void loadMapMeshIntoVao(MapMesh mesh) {
		
		mesh.vao = glGenVertexArrays();
		glBindVertexArray(mesh.vao);
		
		mesh.vertVbo  = loadVertexIntoVBO(0, mesh.verts, 3,  GL_DYNAMIC_DRAW);
		mesh.nVertVbo = loadVertexIntoVBO(1, mesh.nVerts, 3, GL_DYNAMIC_DRAW);
		mesh.tVertVbo = loadVertexIntoVBO(2, mesh.tVerts, 2, GL_DYNAMIC_DRAW);
		mesh.waveVbo  = loadVertexIntoVBO(3, mesh.wave, 4,   GL_DYNAMIC_DRAW);
		mesh.tVbo     = loadVertexIntoVBO(4, mesh.t, 1,      GL_DYNAMIC_DRAW);
		
		int indVbo = glGenBuffers();
		vbos.add(indVbo);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indVbo);
		IntBuffer intBuff = BufferUtils.createIntBuffer(mesh.inds.length);
		intBuff.put(mesh.inds);
		intBuff.flip();
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuff, GL_STATIC_READ);
		
		glBindVertexArray(0);
		
	}
	
	// Offset in points
	public void updateMapMesh(MapMesh mesh, int off, int l) {
		float[] vCopy = new float[l*3];
		System.arraycopy(mesh.verts, off*3, vCopy, 0, l*3);
		float[] nCopy = new float[l*3];
		System.arraycopy(mesh.nVerts, off*3, nCopy, 0, l*3);
		float[] tCopy = new float[l*2];
		System.arraycopy(mesh.tVerts, off*2, tCopy, 0, l*2);
		
		putVextexIntoVBO(mesh.vertVbo,  3, off, vCopy);
		putVextexIntoVBO(mesh.nVertVbo, 3, off, nCopy);
		putVextexIntoVBO(mesh.tVertVbo, 2, off, tCopy);
	}
	
	public CTexModel getCtexModelFromLwobFile(File f, PathConverter finder, AssetManager am) throws IOException {
		FileInputStream in = new FileInputStream(f);
		CTexModel res = getCtexModelFromLwobFile(f.getAbsolutePath(), in, finder, am);
		in.close();
		return res;
	}
	
	public CTexModel getCtexModelFromLwobFile(String assetPath, InputStream in, PathConverter finder, AssetManager am) throws IOException {
		
		assetPath = assetPath.toUpperCase();
		
		if(ctexmodels.containsKey(assetPath)) {
			return ctexmodels.get(assetPath);
		}
		
		if(finder == null) {
			System.out.println("Finder cant be null");
			return null;
		}
		String uvPath = assetPath.substring(0, assetPath.length()-3) + "UV";
		boolean hasUv = am.exists(uvPath);
		
		InputStream lwobIn = am.getAsset(assetPath);
		LwobFileData lfd = LwobFileData.getLwobFileData(lwobIn);
		lwobIn.close();
		
		CTexModelData ctmd = null;
		if(hasUv) {
			ctmd = CTexModelData.getCTexModelFromLwob(lfd, UvFileData.getUvFileData(am.getAsset(uvPath)));
		} else {
			ctmd = CTexModelData.getCTexModelFromLwob(lfd, null);
		}
		
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
		vaos.add(vao);
		
		loadVertexIntoVBO(0, ctmd.v,  3, GL_STATIC_DRAW);
		loadVertexIntoVBO(1, ctmd.vc, 3, GL_STATIC_DRAW);
		loadVertexIntoVBO(2, ctmd.vt, 3, GL_STATIC_DRAW);
		
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
					if(am.exists(prefix + String.format("%0" + lzeros + "d", texOffset+texNum) + "." + extension)) {
						break;
					}
					texOffset++;
				}
				if(texOffset == 9999)
					System.out.println("over 9000?????");
				while(getTexs) {
					String texFile = prefix + String.format("%0" + lzeros + "d", texOffset+texNum) + "." + extension;
					if(!am.exists(texFile)) {
						break;
					}
					_texs.add(am.getTexture(texFile));
					texNum++;
				}
				ctm.texs[i] = new Texture[texNum];
				for(int j = 0; j < texNum; j++) {
					ctm.texs[i][j] = _texs.pop();
				}
				
			} else {
				ctm.texs[i] = new Texture[1];
				InputStream tIn = am.getAsset(path);
				Texture t = null;
				if(tIn != null) {
					t = am.geTLoader().getTexture(extension, tIn, path);
					in.close();
				}
					
				if(t == null) {
					ctm.alpha[i] = null;
					continue;
				}
				ctm.texs[i][0] = t;
			}
			
			String fName = new File(path).getName();
			
			if(fName.matches("[Aa]\\d{3}.+$")) {
				
				int aind = Integer.parseInt(fName.substring(1, 4));
				
				InputStream bmpIn = am.getAsset(path);
				ctm.alpha[i] = am.geTLoader().getColorFromBMPPalet(bmpIn, aind);
				bmpIn.close();
			}
			
		}
		
		ctm.texIndex = 0;
		
		return ctm;
	}
	
	private int loadVertexIntoVBO(int pos, float[] verts, int dim, int mode) {
		
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
	
	private void putVextexIntoVBO(int vbo, int dim, int off, float[] verts) {
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		
		FloatBuffer buff = BufferUtils.createFloatBuffer(verts.length);
		buff.put(verts);
		buff.flip();
		
		glBufferSubData(GL_ARRAY_BUFFER, off*dim*4, buff);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
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
		
		loadVertexIntoVBO(0, verts, 3, GL_STATIC_DRAW);
		loadVertexIntoVBO(1, texpos, 2, GL_STATIC_DRAW);
		
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
	
	// TODO Remove main
	public static BufferedImage img;
	public static void main(String[] args) {
		
		img = null;
		try {
			InputStream in = new FileInputStream(new File("LegoRR0/Interface/Pointers/PointerOpen.bmp"));
			byte[] header = new byte[54];
			
			in.read(header);
			
			int w = getIntLE(header, 18);
			int h = getIntLE(header, 22);
			
			int paletteSize = (int) Math.sqrt(getIntLE(header, 46));
			if(paletteSize == 0)
				paletteSize = 16;
			
			byte[] bpalette = new byte[paletteSize*paletteSize*4];
			in.read(bpalette);
			img = new BufferedImage(paletteSize, paletteSize, BufferedImage.TYPE_INT_ARGB);
			
			for(int i = 0; i < bpalette.length; i += 4) {
				int rgb = 0;
				rgb |= (0xFF & (255-bpalette[i+3])) << 24;
				rgb |= (0xFF & bpalette[i+2]) << 16;
				rgb |= (0xFF & bpalette[i+1]) << 8;
				rgb |= (0xFF & bpalette[i]);
				img.setRGB((i/4)%paletteSize, Math.floorDiv(i/4, paletteSize), rgb);
			}
			in.close();
			
			JFrame frame = new JFrame("BMP color pallette");
			
			
			frame.add(new JPanel() {
				private static final long serialVersionUID = 1L;
				
				@Override
				public Dimension getPreferredSize() {
					return new Dimension(img.getWidth()*10, img.getWidth()*10);
				}
				
				@Override
				public void paint(Graphics arg0) {
//					arg0.drawImage(img, 0, 0, null);
					arg0.drawImage(img, 0, 0, img.getWidth()*10, img.getHeight()*10, null);
//					frame.repaint();
				}
				
			});
			
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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

	public void updateMapTVOB(MapMesh mesh, int start, int stop) {
		FloatBuffer buff = BufferUtils.createFloatBuffer((stop-start)*12);
		buff.put(mesh.t, start*12, (stop-start)*12);
		buff.flip();
		glBindVertexArray(mesh.vao);
		glBindBuffer(GL_ARRAY_BUFFER, mesh.tVbo);
		glBufferSubData(GL_ARRAY_BUFFER, start*12*4, buff);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
}
