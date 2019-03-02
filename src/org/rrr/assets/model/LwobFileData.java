package org.rrr.assets.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.joml.Vector3f;

public class LwobFileData {
	
	public float[] v;
	public int[] iv;
	public int[] ipolsurf;
	public int[] polliv;
	public Surface[] surfs;
	
	public static LwobFileData getLwobFileData(File f) throws IOException {
		FileInputStream in = new FileInputStream(f);
		LwobFileData res = getLwobFileData(in);
		in.close();
		return res;
	}
	
	public static LwobFileData getLwobFileData(InputStream in) throws IOException {
		
		LwobFileData res = new LwobFileData();
		
		byte[] abuff = new byte[8];
		in.read(abuff, 0, 8);
		if(!new String(abuff, 0, 4).equals("FORM")) {
			in.close();
			System.out.println("Invalid LWOB format!");
			return null;
		}
		ByteBuffer seg = ByteBuffer.wrap(abuff, 4, 4);
		int flength = seg.getInt();
		abuff = new byte[flength];
		in.read(abuff, 0, flength);
		int offset = 4;
		if(!new String(abuff, 0, 4).equals("LWOB")) {
			System.out.println("Invalid LWOB format!");
			return null;
		}
		
		String segName;
		int lseg;
		while(flength-offset >= 8) {
			segName = new String(abuff, offset, 4);
			lseg = ByteBuffer.wrap(abuff, offset+4, 4).getInt();
			seg = ByteBuffer.wrap(abuff, offset+8, lseg);
			offset += 8+lseg;
			
			if(segName.equals("PNTS")) {
				parsePNTS(res, seg, lseg);
				continue;
			}
			
			if(segName.equals("SRFS")) {
				parseSRFS(res, abuff, offset-lseg, lseg);
				continue;
			}
			
			if(segName.equals("POLS")) {
				parsePOLS(res, seg);
				continue;
			}
			
			if(segName.equals("SURF")) {
				parseSURF(res, abuff, offset-lseg, lseg);
				continue;
			}
			
		}
		
		return res;
		
	}
	
	private static void parsePNTS(LwobFileData lfd, ByteBuffer seg, int lseg) {
		lfd.v = new float[lseg/4];
		for(int i = 0; i < lseg/12; i++) {
			lfd.v[i*3]		= seg.getFloat();
			lfd.v[i*3+1]	= seg.getFloat();
			lfd.v[i*3+2]	= seg.getFloat();
		}
		
	}
	
	private static void parseSRFS(LwobFileData lfd, byte[] abuff, int offset, int lseg) {
		
		LinkedList<String> _names = new LinkedList<>();
		int lsurfs = 0;
		
		int noffset = offset;
		for(int i = offset; i < offset + lseg; i++) {
			if(abuff[i] == 0) {
				lsurfs++;
				_names.add(new String(abuff, noffset, i-noffset));
				i++;
				noffset = i;
				if(i%2 == 1)
					noffset++;
				
			}
		}
		
		lfd.surfs = new Surface[lsurfs];
		for(int i = 0; i < lsurfs; i++) {
			lfd.surfs[i] = new Surface();
			lfd.surfs[i].name = _names.pop();
		}
	}
	
	private static void parsePOLS(LwobFileData lfd, ByteBuffer seg) {
		
		LinkedList<Integer> _iv			= new LinkedList<>();
		LinkedList<Integer> _liv			= new LinkedList<>();
		LinkedList<Integer> _ipolsurf	= new LinkedList<>();
		
		int lpol = 0;
		int liv = 0;
		int curliv = 0;
		short isurf = 0;
		while(seg.remaining() > 0) {
			curliv = seg.getShort();
			_liv.add(curliv);
			liv += curliv;
			
			for(int i = 0; i < curliv; i++)
				_iv.add((int) seg.getShort());
			
			isurf = seg.getShort();
			if(isurf < 0) {
				System.out.println("Detail polygons??");
				short ldpols = seg.getShort();
				for(int i = 0; i < ldpols; i++) {
					int ldivs = seg.getShort();
					for(int j = 0; j < ldivs; i++)
						seg.getShort();
				}
			}
			_ipolsurf.add(Math.abs(isurf)-1);
			lpol++;
		}
		
		lfd.ipolsurf	= new int[lpol];
		lfd.polliv		= new int[lpol];
		lfd.iv			= new int[liv];
		
		int ivoffset = 0;
		for(int i = 0; i < lpol; i++) {
			lfd.ipolsurf[i]	= _ipolsurf.pop();
			lfd.polliv[i]	= _liv.pop();
			
			for(int j = 0; j < lfd.polliv[i]; j++)
				lfd.iv[ivoffset + j] = _iv.pop();
			
			ivoffset += lfd.polliv[i];
				
		}
		
	}
	
	public static final char[] texIndices = {'C', 'D', 'S', 'R', 'T', 'B'};
	private static void parseSURF(LwobFileData lfd, byte[] abuff, int offset, int lseg) {
		String name = null;
		int curoffset = 0;
		for(int i = offset; i < offset + lseg; i++) {
			if(abuff[i] == 0) {
				name = new String(abuff, offset, i-offset);
				i++;
				lseg -= (i - offset);
				offset = i;
				if(i%2 == 1) {
					offset++;
					lseg--;
				}
				break;
			}
		}
		Surface s = null;
		for(int i = 0; i < lfd.surfs.length; i++)
			if(lfd.surfs[i].name.equals(name))
				s = lfd.surfs[i];
		if(s == null) {
			System.out.println("Couldnt find Surface: " + name);
			return;
		}
		
		String segName;
		int sublseg;
		ByteBuffer seg;
		int texIndex = 0;
		
		
		while(lseg-curoffset >= 6) {
			segName = new String(abuff, offset+curoffset, 4);
			sublseg = ByteBuffer.wrap(abuff, offset+curoffset+4, 2).getShort();
			seg = ByteBuffer.wrap(abuff, offset+curoffset+6, sublseg);
			curoffset += 6+sublseg;
			
			if(segName.equals("COLR")) {
				parseCOLR(s, seg);
				continue;
			}
			
			if(segName.equals("FLAG")) {
				parseFLAG(s, seg);
				continue;
			}
			
			if(segName.equals("LUMI")) {
				parseLUMI(s, seg);
				continue;
			}
			
			if(segName.equals("DIFF")) {
				parseDIFF(s, seg);
				continue;
			}
			
			if(segName.equals("SPEC")) {
				parseSPEC(s, seg);
				continue;
			}
			
			if(segName.equals("REFL")) {
				parseREFL(s, seg);
				continue;
			}
			
			if(segName.equals("TRAN")) {
				parseTRAN(s, seg);
				continue;
			}
			
			if(segName.equals("TIMG")) {
				parseTIMG(s, texIndex, abuff, offset+curoffset-sublseg, sublseg);
				continue;
			}
			
			if(segName.equals("TFLG")) {
				parseTFLG(s, texIndex, seg);
				continue;
			}
			
			if(segName.equals("TSIZ")) {
				parseTSIZ(s, texIndex, seg);
				continue;
			}
			
			if(segName.equals("TCTR")) {
				parseTCTR(s, texIndex, seg);
				continue;
			}
			
			if(segName.equals("TFAL")) {
				parseTFAL(s, texIndex, seg);
				continue;
			}
			
			if(segName.equals("TVEL")) {
				parseTVEL(s, texIndex, seg);
				continue;
			}
			
			if(segName.equals("TCLR")) {
				parseTCLR(s, texIndex, seg);
				continue;
			}
			
			if(segName.equals("TVAL")) {
				parseTVAL(s, texIndex, seg);
				continue;
			}
			
			if(segName.equals("TAMP")) {
				parseTAMP(s, texIndex, seg);
				continue;
			}
			
			if(segName.equals("TFRQ")) {
				parseTFRQ(s, texIndex, seg);
				continue;
			}
			
			if(segName.startsWith("TSP")) {
				int tspIndex = Integer.parseInt(segName.substring(3));
				parseTSP(tspIndex, s, texIndex, seg);
				continue;
			}
			
			if(segName.endsWith("TEX")) {
				for(int i = 0; i < texIndices.length; i++)
					if(segName.charAt(0) == texIndices[i])
						texIndex = i;
			}
			
		}
	}
	
	private static void parseCOLR(Surface s, ByteBuffer seg) {
		
		Vector3f c = new Vector3f();
		c.x = ((float) (seg.get()&0xFF)) / 256.0f;
		c.y = ((float) (seg.get()&0xFF)) / 256.0f;
		c.z = ((float) (seg.get()&0xFF)) / 256.0f;
		s.color = c;
		
	}
	
	private static void parseFLAG(Surface s, ByteBuffer seg) {
		
		s.flags = seg.getShort();
		
	}
	
	private static void parseLUMI(Surface s, ByteBuffer seg) {
		
		short val = seg.getShort();
		s.lumi = ((float) val) / 256.0f;
		
	}
	
	private static void parseDIFF(Surface s, ByteBuffer seg) {
		
		short val = seg.getShort();
		s.diff = ((float) val) / 256.0f;
		
	}
	
	private static void parseSPEC(Surface s, ByteBuffer seg) {
		
		short val = seg.getShort();
		s.spec = ((float) val) / 256.0f;
		
	}
	
	private static void parseREFL(Surface s, ByteBuffer seg) {
	
		short val = seg.getShort();
		s.refl = ((float) val) / 256.0f;
	
	}

	private static void parseTRAN(Surface s, ByteBuffer seg) {
		
		short val = seg.getShort();
		s.tran = ((float) val) / 256.0f;
		
	}
	
	private static void parseTIMG(Surface s, int texIndex, byte[] abuff, int offset, int lseg) {
		for(int i = offset; i < offset + lseg; i++) {
			if(abuff[i] == 0) {
				String texName = new String(abuff, offset, i-offset);
				if(texName.endsWith(" (sequence)")) {
					s.texData[texIndex].sequenced = true;
					s.texData[texIndex].texFile = texName.substring(0, texName.length()-11);
				} else {
					s.texData[texIndex].sequenced = false;
					s.texData[texIndex].texFile = texName;
				}
				return;
			}
		}
	}
	
	private static void parseTFLG(Surface s, int texIndex, ByteBuffer seg) {
		short tflg = seg.getShort();
		
		s.texData[texIndex].tflg = tflg;
		
		switch (tflg & 7) {
		case 4:
			s.texData[texIndex].projAxis = new Vector3f(0, 0, 1);
			break;
		case 2:
			s.texData[texIndex].projAxis = new Vector3f(0, 1, 0);
			break;
		case 1:
			s.texData[texIndex].projAxis = new Vector3f(1, 0, 0);
			break;
		default:
			break;
		}
		
	}
	
	private static void parseTSIZ(Surface s, int texIndex, ByteBuffer seg) {
		Vector3f v = new Vector3f();
		v.x = seg.getFloat();
		v.y = seg.getFloat();
		v.z = seg.getFloat();
		s.texData[texIndex].tsiz = v;
	}
	
	private static void parseTCTR(Surface s, int texIndex, ByteBuffer seg) {
		Vector3f v = new Vector3f();
		v.x = seg.getFloat();
		v.y = seg.getFloat();
		v.z = seg.getFloat();
		s.texData[texIndex].tctr = v;
	}
	
	private static void parseTFAL(Surface s, int texIndex, ByteBuffer seg) {
		Vector3f v = new Vector3f();
		v.x = seg.getFloat();
		v.y = seg.getFloat();
		v.z = seg.getFloat();
		s.texData[texIndex].tfal = v;
	}
	
	private static void parseTVEL(Surface s, int texIndex, ByteBuffer seg) {
		Vector3f v = new Vector3f();
		v.x = seg.getFloat();
		v.y = seg.getFloat();
		v.z = seg.getFloat();
		s.texData[texIndex].tvel = v;
	}
	
	private static void parseTCLR(Surface s, int texIndex, ByteBuffer seg) {
		Vector3f c = new Vector3f();
		c.x = ((float) (seg.get()&0xFF)) / 256.0f;
		c.y = ((float) (seg.get()&0xFF)) / 256.0f;
		c.z = ((float) (seg.get()&0xFF)) / 256.0f;
		s.texData[texIndex].tclr = c;
	}
	
	private static void parseTVAL(Surface s, int texIndex, ByteBuffer seg) {
		s.texData[texIndex].tval = ((float) seg.getShort()) / 256.0f;
	}
	
	private static void parseTAMP(Surface s, int texIndex, ByteBuffer seg) {
		s.texData[texIndex].tamp = seg.getFloat();
	}
	
	private static void parseTFRQ(Surface s, int texIndex, ByteBuffer seg) {
		s.texData[texIndex].tfrq = seg.getShort();
	}
	
	private static void parseTSP(int tspIndex, Surface s, int texIndex, ByteBuffer seg) {
		s.texData[texIndex].tsp[tspIndex] = seg.getFloat();
	}
	
	public static class Surface {
		
		public String name;
		public Vector3f color;
		public short flags;
		public float lumi, diff, spec, refl, tran;
		public TextureData[] texData = new TextureData[6];
		
		public Surface() {
			for(int i = 0; i < texData.length; i++) {
				texData[i] = new TextureData();
				texData[i].projAxis = new Vector3f();
				texData[i].tsiz = new Vector3f();
				texData[i].tctr = new Vector3f();
				texData[i].tfal = new Vector3f();
				texData[i].tvel = new Vector3f();
				texData[i].tclr = new Vector3f();
			}
		}
		
		public static class TextureData {
			public String	texMapping;
			public String	texFile;
			public boolean  sequenced;
			public short tflg;
			public Vector3f	projAxis;
			public Vector3f	tsiz, tctr, tfal, tvel, tclr;
			public float tval, tamp;
			public int tfrq;
			public float[] tsp = new float[3];
		}
	}
}
