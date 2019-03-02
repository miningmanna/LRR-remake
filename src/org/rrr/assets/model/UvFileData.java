package org.rrr.assets.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.rrr.Input;

public class UvFileData {
	
	public String texFile;
	public float[] vt;
	public int[] ivt;
	
	public static UvFileData getUvFileData(File f) throws IOException {
		FileInputStream in = new FileInputStream(f);
		UvFileData res = getUvFileData(in);
		in.close();
		return res;
	}
	
	public static UvFileData getUvFileData(InputStream in) throws IOException {
		
		UvFileData res = new UvFileData();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		br.readLine();
		int lsurf = Integer.parseInt(br.readLine());
		for(int i = 0; i < lsurf*2-1; i++)
			br.readLine();
		res.texFile = br.readLine();
		
		int lfaces = Integer.parseInt(br.readLine());
		res.vt = new float[lfaces*3*3];
		
		LinkedList<Float> _vt = new LinkedList<>();
		
		int livt = 0;
		for(int i = 0; i < lfaces; i++) {
			int lv = Integer.parseInt(br.readLine().split(" ")[1]);
			for(int j = 0; j < lv; j++) {
				String line = br.readLine();
				String[] split = line.split(" ");
				_vt.add(Float.parseFloat(split[0]));
				_vt.add(Float.parseFloat(split[1]));
				_vt.add(Float.parseFloat(split[2]));
			}
			livt += lv;
		}
		res.ivt = new int[livt];
		res.vt = new float[livt*3];
		for(int i = 0; i < livt*3; i++)
			res.vt[i] = _vt.pop();
		for(int i = 0; i < livt; i++)
			res.ivt[i] = i;
		
		return res;
	}
	
}
