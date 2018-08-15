package org.rrr.model;

import java.util.ArrayList;

public class TestData {
	public float[] v;
	public float[] vc;
	public int[] iv;
	public int[] ivc;
	
	public void synchronizeIndecis() {
		
		ArrayList<Long> verticesIds = new ArrayList<>();
		verticesIds.ensureCapacity(iv.length);
		
		for(int i = 0; i < iv.length; i++) {
			long l = (((long)iv[i]) << 32) | ivc[i];
			if(!verticesIds.contains(l))
				verticesIds.add(l);
		}
		
		int size = verticesIds.size();
		float[] newVerts = new float[size*3];
		float[] newNorms = new float[size*3];
		int[] newI  = new int[iv.length];
		
		for(int i = 0; i < iv.length; i++) {
			long l = (((long)iv[i]) << 32) | ivc[i];
			int ix = verticesIds.indexOf(l);
			newNorms[ix*3] = vc[ivc[i]*3];
			newNorms[ix*3 +1] = vc[ivc[i]*3 +1];
			newNorms[ix*3 +2] = vc[ivc[i]*3 +2];
			newVerts[ix*3] = v[iv[i]*3];
			newVerts[ix*3 +1] = v[iv[i]*3 +1];
			newVerts[ix*3 +2] = v[iv[i]*3 +2];
			newI[i] = ix;
		}
		
		v = newVerts;
		iv = newI;
		ivc = newI;
		vc = newNorms;
	}
}
