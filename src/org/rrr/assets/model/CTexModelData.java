package org.rrr.assets.model;

import java.io.File;
import java.util.LinkedList;

import org.joml.Vector3f;
import org.rrr.assets.model.LwobFileData.Surface;
import org.rrr.assets.model.LwobFileData.Surface.TextureData;

public class CTexModelData {
	
	public String[] texs;
	public int[] ipolsurf;
	public int[] polliv;
	public int[] surfLen;
	public float[] v, vc, vt;
	public int[] iv;
	public Vector3f[] alphaPix;
	public boolean[] additive;
	public boolean[] doubleSided;
	public boolean[] sequenced;
	
	public static CTexModelData getCTexModelFromLwob(LwobFileData lfd, UvFileData uvData) {
		
		CTexModelData ctm = new CTexModelData();
		
		ctm.iv  = new int[lfd.iv.length];
		
		ctm.v = new float[ctm.iv.length*3];
		ctm.texs = new String[lfd.surfs.length];
		ctm.alphaPix = new Vector3f[lfd.surfs.length];
		ctm.additive = new boolean[lfd.surfs.length];
		ctm.ipolsurf = new int[lfd.ipolsurf.length];
		ctm.polliv = new int[lfd.polliv.length];
		ctm.doubleSided = new boolean[lfd.surfs.length];
		ctm.sequenced = new boolean[lfd.surfs.length];
		
		for(int i = 0; i < lfd.surfs.length; i++) {
			Surface s = lfd.surfs[i];
			ctm.additive[i] = (s.flags & 0x00000200) != 0;
			ctm.doubleSided[i] = (s.flags & 0x00000100) != 0;
			if(uvData == null) {
				ctm.texs[i] = s.texData[0].texFile;
			} else {
				ctm.texs[i] = new File(uvData.texFile).getName();
			}
			
			ctm.sequenced[i] = s.texData[0].sequenced;
			
			if(ctm.texs[i] == null)
				if(uvData == null)
					continue;
			
		}
		
		int[] ivt = new int[ctm.iv.length];
		
		// Load indices from lwob to ctm while ordering the surfaces
		int _ivoffset = 0;
		int polIndex = 0;
		for(int i = 0; i < lfd.surfs.length; i++) {
			int offset = 0;
			for(int j = 0; j < lfd.ipolsurf.length; j++) {
				
				if(lfd.ipolsurf[j] == i) {
					for(int k = 0; k < lfd.polliv[j]; k++) {
						ctm.iv[_ivoffset+k] = lfd.iv[offset+k];
					}
					ctm.ipolsurf[polIndex] = lfd.ipolsurf[j];
					ctm.polliv[polIndex] = lfd.polliv[j];
					if(uvData != null) {
						for(int k = 0; k < lfd.polliv[j]; k++) {
							ivt[_ivoffset+k] = uvData.ivt[offset+k];
						}
					}
					polIndex++;
					_ivoffset += lfd.polliv[j];
				}
				offset += lfd.polliv[j];
			}
			
		}
		
		ctm.vc = new float[ctm.v.length];
		ctm.vt = new float[ctm.v.length];
		
		for(int i = 0; i < ctm.iv.length; i++) {
			for(int j = 0; j < 3; j++) {
				ctm.v[i*3+j] = lfd.v[ctm.iv[i]*3+j];
				if(uvData != null) {
					ctm.vt[i*3+j] = uvData.vt[ivt[i]*3+j];
				}
			}
			ctm.iv[i] = i;
			
		}
		
		int index = 0;
		for(int i = 0; i < ctm.ipolsurf.length; i++) {
			
			Surface s = lfd.surfs[ctm.ipolsurf[i]];
			
			for(int j = 0; j < ctm.polliv[i]; j++) {
				ctm.vc[ctm.iv[index+j]*3]	= s.color.x;
				ctm.vc[ctm.iv[index+j]*3+1]	= s.color.y;
				ctm.vc[ctm.iv[index+j]*3+2]	= s.color.z;
			}
			index += ctm.polliv[i];
		}
		
		if(uvData == null) {
			int vi = 0;
			Surface s = null;
			TextureData td = null;
			float[] ctr = new float[3];
			float[] siz = new float[3];
			for(int i = 0; i < ctm.ipolsurf.length; i++) {
				s = lfd.surfs[ctm.ipolsurf[i]];
				if(s == null) {
					System.out.println("Surface null");
					vi += ctm.polliv[i];
					continue;
				}
				if(s.texData[0].texFile == null) {
					vi += ctm.polliv[i];
					continue;
				}
				td = s.texData[0];
				ctr[0] = td.tctr.x;
				ctr[1] = td.tctr.y;
				ctr[2] = td.tctr.z;
				siz[0] = td.tsiz.x;
				siz[1] = td.tsiz.y;
				siz[2] = td.tsiz.z;
				int itx = 0;
				int ity = 1;
				float ssx = 1, ssy = -1;
				if(td.projAxis.y != 0) {
					itx = 0;
					ity = 2;
				} else if(td.projAxis.x != 0) {
					itx = 2;
					ity = 1;
					ssx = 1;
					ssy = -1;
				}
				for(int j = 0; j < ctm.polliv[i]; j++) {
					ctm.vt[ctm.iv[vi+j]*3]		= ((ctm.v[ctm.iv[vi+j]*3 + itx] - ctr[itx]) / (ssx*siz[itx]))+0.5f;
					ctm.vt[ctm.iv[vi+j]*3+1]	= ((ctm.v[ctm.iv[vi+j]*3 + ity] - ctr[ity]) / (ssy*siz[ity]))+0.5f;
					ctm.vt[ctm.iv[vi+j]*3+2]	= 0;
				}
				
				vi += ctm.polliv[i];
			}
		}
		
		int[] pattern = {0, 1, 3, 1, 2, 3};
		
		LinkedList<Integer> _iv = new LinkedList<>();
		LinkedList<Integer> _ipolsurf = new LinkedList<>();
		int _lpol = 0;
		int ivOffset = 0;
		for(int i = 0; i < ctm.polliv.length; i++) {
			if(ctm.polliv[i] == 4) {
				for(int j = 0; j < pattern.length; j++) {
					_iv.add(ctm.iv[ivOffset+pattern[j]]);
				}
				_ipolsurf.add(ctm.ipolsurf[i]);
				_ipolsurf.add(ctm.ipolsurf[i]);
				_lpol += 2;
			} else {
				for(int j = 0; j < 3; j++) {
					_iv.add(ctm.iv[ivOffset+j]);
				}
				_ipolsurf.add(ctm.ipolsurf[i]);
				_lpol += 1;
			}
			ivOffset += ctm.polliv[i];
		}
		
		ctm.iv = new int[_lpol*3];
		ctm.ipolsurf = new int[_lpol];
		ctm.polliv = new int[_lpol];
		ctm.surfLen = new int[ctm.texs.length];
		
		int curisurf = 0;
		int surflen = 0;
		for(int i = 0; i < _lpol; i++) {
			int isurf = _ipolsurf.pop();
			if(isurf > curisurf) {
				ctm.surfLen[curisurf] = surflen;
				surflen = 3;
				curisurf++;
			} else {
				surflen += 3;
			}
			ctm.ipolsurf[i]	= isurf;
			ctm.polliv[i]	= 3;
			ctm.iv[i*3]		= _iv.pop();
			ctm.iv[i*3+1]	= _iv.pop();
			ctm.iv[i*3+2]	= _iv.pop();
		}
		ctm.surfLen[curisurf] = surflen;
		
		return ctm;
		
	}
}
