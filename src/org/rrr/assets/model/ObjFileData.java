package org.rrr.assets.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class ObjFileData {
	
	public float[] v;
	public float[] vt;
	public float[] vn;
	
	public int[] iv;
	public int[] ivt;
	public int[] ivn;
	
	public static ObjFileData getObjFileData(File f) throws IOException {
		
		ObjFileData res = new ObjFileData();
		
		int 	lv 		= 0,
				lvt 	= 0,
				lvn 	= 0,
				liv 	= 0,
				livt 	= 0,
				livn 	= 0;
		
		LinkedList<Float> 	v 	= new LinkedList<>(),
							vt 	= new LinkedList<>(),
							vn 	= new LinkedList<>();
		
		LinkedList<Integer> iv 	= new LinkedList<>(),
							ivt = new LinkedList<>(),
							ivn = new LinkedList<>();
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		String line;
		while((line = br.readLine()) != null) {
			if(line.startsWith("v ")) {
				lv++;
				String[] nums = line.substring(2).split(" ");
				if(nums.length != 3) {
					System.out.println("Invalid vector!");
					continue;
				}
				
				for(String s : nums)
					v.add(Float.parseFloat(s));
				continue;
			}
			if(line.startsWith("vt ")) {
				lvt++;
				String[] nums = line.substring(3).split(" ");
				if(nums.length != 2) {
					System.out.println("Invalid vector!");
					continue;
				}
				
				for(String s : nums)
					vt.add(Float.parseFloat(s));
				continue;
			}
			if(line.startsWith("vn ")) {
				lvn++;
				String[] nums = line.substring(3).split(" ");
				if(nums.length != 3) {
					System.out.println("Invalid vector!");
					continue;
				}
				
				for(String s : nums)
					vn.add(Float.parseFloat(s));
				continue;
			}
			if(line.startsWith("f ")) {
				String[] points = line.substring(2).split(" ");
				if(points.length != 3) {
					System.out.println("Invalid face!");
					continue;
				}
				
				for(String s : points) {
					String[] nums = s.split("\\/");
					if(points.length != 3) {
						System.out.println("Invalid face!");
						continue;
					}
					if(nums[0].length() > 0) {
						liv++;
						iv.add(Integer.parseInt(nums[0]));
					}
					
					if(nums[1].length() > 0) {
						livt++;
						ivt.add(Integer.parseInt(nums[1]));
					}
					
					if(nums[2].length() > 0) {
						livn++;
						ivn.add(Integer.parseInt(nums[2]));
					}
				}
			}
		}
		br.close();
		
		if(lv != 0) {
			res.v = new float[lv*3];
			for(int i = 0; i < lv; i++) {
				res.v[i*3]		= v.pop();
				res.v[i*3+1]	= v.pop();
				res.v[i*3+2]	= v.pop();
			}
		}
		if(lvt != 0) {
			res.vt = new float[lvt*2];
			for(int i = 0; i < lvt; i++) {
				res.vt[i*2]		= vt.pop();
				res.vt[i*2+1]	= vt.pop();
			}
		}
		if(lvn != 0) {
			res.vn = new float[lvn*3];
			for(int i = 0; i < lvn; i++) {
				res.vn[i*3]		= vn.pop();
				res.vn[i*3+1]	= vn.pop();
				res.vn[i*3+2]	= vn.pop();
			}
		}
		if(liv != 0) {
			res.iv = new int[liv];
			for(int i = 0; i < liv; i++) {
				res.iv[i] = iv.pop()-1;
			}
		}
		if(livt != 0) {
			res.ivt = new int[livt];
			for(int i = 0; i < livt; i++) {
				res.ivt[i] = ivt.pop()-1;
			}
		}
		if(livn != 0) {
			res.ivn = new int[livn];
			for(int i = 0; i < livn; i++) {
				res.ivn[i] = ivn.pop()-1;
			}
		}
		
		return res;
	}
}
