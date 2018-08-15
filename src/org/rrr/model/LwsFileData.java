package org.rrr.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class LwsFileData {
	
	public Frame[] frames;
	public int[] parent;
	public String[] objFiles;
	
	public int firstFrame = 0;
	public int lastFrame = 0;
	public int frameStep = 0;
	public float framesPerSecond = 0;
	
	public static LwsFileData getLwsFileData(File f) throws IOException {
		
		LwsFileData res = new LwsFileData();
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		
		LinkedList<ObjectKeyFrames> _objKeyFrames = new LinkedList<>();
		LinkedList<String> _objFiles = new LinkedList<>();
		LinkedList<Integer> _objParent = new LinkedList<>();
		
		int lobjects = 0;
		int frames = 0;
		while((line = br.readLine()) != null) {
			
			if(line.startsWith("FistFrame"))
				res.firstFrame = Integer.parseInt(line.split(" ")[1]);
			if(line.startsWith("LastFrame")) {
				res.lastFrame = Integer.parseInt(line.split(" ")[1]);
				frames = res.lastFrame - res.firstFrame;
			}
			if(line.startsWith("FrameStep"))
				res.frameStep = Integer.parseInt(line.split(" ")[1]);
			if(line.startsWith("FramesPerSecond"))
				res.framesPerSecond = Float.parseFloat(line.split(" ")[1]);
			
			if(line.startsWith("AddNullObject") || line.startsWith("LoadObject")) {
				lobjects++;
				
				if(line.startsWith("AddNullObject"))
					_objFiles.add(null);
				else
					_objFiles.add(line.substring(12));
				
				for(int i = 0; i < 3; i++)
					line = br.readLine();
				int linfo = Integer.parseInt(line.substring(2));
				line = br.readLine();
				
				ObjectKeyFrames of = new ObjectKeyFrames();
				int lframes = Integer.parseInt(line.substring(2));
				of.lframes = lframes;
				of.frames = new int[lframes];
				of.relPos = new Vector3f[lframes];
				of.relRot = new Vector3f[lframes];
				of.scales = new Vector3f[lframes];
				
				for(int i = 0; i < lframes; i++) {
					
					line = br.readLine();
					line = line.substring(2);
					String[] s = line.split(" ");
					if(s.length > linfo) {
						System.out.println("More data than expected (" + s.length + "), but continuing...");
					} else if(s.length < linfo) {
						System.out.println("Less data than expected! (" + s.length + "), cant parse...");
						if(i == 0) {
							of.relPos[i] = new Vector3f(0, 0, 0);
							of.relRot[i] = new Vector3f(0, 0, 0);
							of.scales[i] = new Vector3f(0, 0, 0);
						} else {
							of.relPos[i] = of.relPos[i-1];
							of.relRot[i] = of.relRot[i-1];
							of.scales[i] = of.scales[i-1];
						}
					} else {
						of.relPos[i] = new Vector3f(	Float.parseFloat(s[0]),
														Float.parseFloat(s[1]),
														Float.parseFloat(s[2]));
						of.relRot[i] = new Vector3f(	(float) Math.toRadians(Float.parseFloat(s[4])),
														(float) Math.toRadians(Float.parseFloat(s[3])),
														(float) Math.toRadians(Float.parseFloat(s[5])));
						of.scales[i] = new Vector3f(	Float.parseFloat(s[6]),
														Float.parseFloat(s[7]),
														Float.parseFloat(s[8]));
					}
					line = br.readLine();
					line = line.substring(2);
					of.frames[i] = Integer.parseInt(line.split(" ")[0]);
					
				}
				while((line = br.readLine()) != null) {
					if(line.startsWith("ParentObject")) {
						_objParent.add(Integer.parseInt(line.substring(13))-1);
						break;
					} else if(line.equals("")) {
						_objParent.add(-1);
						break;
					}
				}
				
				_objKeyFrames.add(of);
				
			}
		}
		br.close();
		
		res.objFiles = new String[lobjects];
		res.parent = new int[lobjects];
		ObjectKeyFrames[] okfs = new ObjectKeyFrames[lobjects];
		for(int i = 0; i < lobjects; i++) {
			res.objFiles[i] = _objFiles.pop();
			res.parent[i] = _objParent.pop();
			okfs[i] = _objKeyFrames.pop();
		}
		
		
		res.frames = new Frame[frames];
		int[] lastKeyFrame = new int[lobjects];
		for(int i = 0; i < frames; i++) {
			Frame frame = res.makeFrame();
			res.frames[i] = frame;
			frame.transform = new Matrix4f[lobjects];
			frame.objectRelPos = new Vector3f[lobjects];
			frame.position = new Vector3f[lobjects];
			frame.objectRelRot = new Vector3f[lobjects];
			frame.rotation = new Vector3f[lobjects];
			frame.scales = new Vector3f[lobjects];
			frame.parent = res.parent;
			int lastIndex = 0;
			ObjectKeyFrames okf;
			float scalar = 1.0f;
			for(int j = 0; j < lobjects; j++) {
				okf = okfs[j];
				int ind = 0;
				if(( ind = contains(okf.frames, i)) != -1)
					lastKeyFrame[j] = ind;
				lastIndex = lastKeyFrame[j];
				if(lastIndex+1 < okf.lframes) {
					scalar = (float) ((float) (okf.frames[lastIndex] - i)) / ((float) (okf.frames[lastIndex+1] - okf.frames[lastIndex]));
					frame.objectRelPos[j] = new Vector3f(okf.relPos[lastIndex]).add(new Vector3f(okf.relPos[lastIndex]).sub(okf.relPos[lastIndex+1]).mul(scalar));
					frame.objectRelRot[j] = new Vector3f(okf.relRot[lastIndex]).add(new Vector3f(okf.relRot[lastIndex]).sub(okf.relRot[lastIndex+1]).mul(scalar));
					frame.scales[j] = new Vector3f(okf.scales[lastIndex]).add(new Vector3f(okf.scales[lastIndex]).sub(okf.scales[lastIndex+1]).mul(scalar));
				} else {
					frame.objectRelPos[j] = new Vector3f(okf.relPos[lastIndex]);
					frame.objectRelRot[j] = new Vector3f(okf.relRot[lastIndex]);
					frame.scales[j] = new Vector3f(okf.scales[lastIndex]);
				}
			}
			
			frame.genTransforms();
			
//			frame.genObjectsRot();
//			frame.genObjectsPos();
			
		}
		
		return res;
		
	}
	
	private Frame makeFrame() {
		return new Frame();
	}

	private static int contains(int[] a, int j) {
		for(int i = 0; i < a.length; i++)
			if(a[i] == j)
				return i;
		return -1;
	}
	
	public class Frame {
		
		public int[] parent;
		public Matrix4f[] transform;
		public Vector3f[][] axis;
		public Vector3f[] objectRelPos;
		public Vector3f[] position;
		public Vector3f[] objectRelRot;
		public Vector3f[] rotation;
		public Vector3f[] scales;
		
		public void genObjectsRot() {
			rotation = new Vector3f[objectRelRot.length];
			axis = new Vector3f[objectRelRot.length][3];
			for(int i = 0; i < parent.length; i++)
				genParentRot(i);
		}
		
		public void genParentRot(int i) {
			if(rotation[i] == null) {
				if(parent[i] == -1) {
					rotation[i] = new Vector3f(objectRelRot[i]);
				} else {
					genParentRot(parent[i]);
					rotation[i] = new Vector3f(rotation[parent[i]]).add(new Vector3f(objectRelRot[i]));
					axis[i][0] = new Vector3f(1, 0, 0)
								.rotateX(rotation[i].x)
								.rotateY(rotation[i].y)
								.rotateZ(rotation[i].z);
					axis[i][1] = new Vector3f(0, 1, 0)
								.rotateX(rotation[i].x)
								.rotateY(rotation[i].y)
								.rotateZ(rotation[i].z);
					axis[i][2] = new Vector3f(0, 0, 1)
								.rotateX(rotation[i].x)
								.rotateY(rotation[i].y)
								.rotateZ(rotation[i].z);
				}
			}
		}
		
		public void genObjectsPos() {
			position = new Vector3f[objectRelPos.length];
			for(int i = 0; i < parent.length; i++)
				genParentPos(i);
		}
		
		public void genParentPos(int i) {
			if(position[i] == null) {
				if(parent[i] == -1) {
					position[i] = new Vector3f(objectRelPos[i]);
				} else {
					genParentPos(parent[i]);
					Vector3f relPos = new Vector3f(objectRelPos[i]);
					relPos.rotateAxis(objectRelRot[parent[i]].x, axis[i][0].x, axis[i][0].y, axis[i][0].z);
					relPos.rotateAxis(objectRelRot[parent[i]].y, axis[i][1].x, axis[i][1].y, axis[i][1].z);
					relPos.rotateAxis(objectRelRot[parent[i]].z, axis[i][2].x, axis[i][2].y, axis[i][2].z);
					position[i] = new Vector3f(position[parent[i]]).add(relPos);
				}
			}
		}
		
		public void genTransforms() {
			transform = new Matrix4f[objectRelPos.length];
			position = new Vector3f[objectRelPos.length];
			for(int i = 0; i < objectRelPos.length; i++)
				genTransform(i);
		}
		
		private void genTransform(int i) {
			if(transform[i] != null)
				return;
			
			Matrix4f m = new Matrix4f();
			m.identity();
			m.translate(objectRelPos[i]);
			m.rotateXYZ(objectRelRot[i]);
			m.scale(scales[i]);
			if(parent[i] != -1) {
				genTransform(parent[i]);
				transform[parent[i]].mul(m, m);
			}
			transform[i] = m;
		}
//		
	}
	
	static private class ObjectKeyFrames {
		public int lframes;
		public int[] frames;
		public Vector3f[] relPos;
		public Vector3f[] relRot;
		public Vector3f[] scales;
	}
	
}
