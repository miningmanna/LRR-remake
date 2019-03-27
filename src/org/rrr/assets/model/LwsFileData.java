package org.rrr.assets.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.rrr.Input;

public class LwsFileData {
	
	public Frame[] frames;
	public int[] parent;
	public String[] objFiles;
	
	public int firstFrame = 0;
	public int lastFrame = 0;
	public int frameStep = 0;
	public float framesPerSecond = 0;
	
	public static LwsFileData getLwsFileData(InputStream in) throws IOException {
		
		LwsFileData res = new LwsFileData();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		
		LinkedList<ObjectAlphaKeyFrames> _objAlphaFrames = new LinkedList<>();
		LinkedList<ObjectKeyFrames> _objKeyFrames = new LinkedList<>();
		LinkedList<String> _objFiles = new LinkedList<>();
		LinkedList<Integer> _objParent = new LinkedList<>();
		LinkedList<Vector3f> _objPivot = new LinkedList<>();
		
		ObjectAlphaKeyFrames lastAlphaFrames = null;
		Vector3f lastPivot = null;
		
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
				ObjectAlphaKeyFrames oaf = new ObjectAlphaKeyFrames();
				lastAlphaFrames = oaf;
				Vector3f pivot = new Vector3f(0, 0, 0);
				lastPivot = pivot;
				_objPivot.add(pivot);
				oaf.frames = new int[] {res.firstFrame, res.lastFrame};
				oaf.alpha = new float[] {1,1};
				
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
					if(line.startsWith("LockedChannels")) {
						int channels = Integer.parseInt(line.split(" ")[1]);
						// TODO: LockedChannels
					}
					if(line.startsWith("PivotPoint")) {
						String[] split = line.split(" ");
						lastPivot.x = Float.parseFloat(split[1]);
						lastPivot.y = Float.parseFloat(split[2]);
						lastPivot.z = Float.parseFloat(split[3]);
					}
					if(line.startsWith("ParentObject")) {
						_objParent.add(Integer.parseInt(line.substring(13))-1);
						break;
					} else if(line.equals("")) {
						_objParent.add(-1);
						break;
					}
				}
				
				_objKeyFrames.add(of);
				_objAlphaFrames.add(oaf);
				continue;
			}
			
			if(line.startsWith("LockedChannels")) {
				int channels = Integer.parseInt(line.split(" ")[1]);
				// TODO: LockedChannels
				continue;
			}
			
			if(line.startsWith("PivotPoint")) {
				String[] split = line.split(" ");
				lastPivot.x = Float.parseFloat(split[1]);
				lastPivot.y = Float.parseFloat(split[2]);
				lastPivot.z = Float.parseFloat(split[3]);
				continue;
			}
			
			if(line.startsWith("ObjDissolve (envelope)")) {
				
				for(int i = 0; i < 2; i++)
					line = br.readLine();
				int lframes = Integer.parseInt(line.substring(2));
				
				lastAlphaFrames.lframes = lframes;
				lastAlphaFrames.frames = new int[lframes];
				lastAlphaFrames.alpha = new float[lframes];
				for(int i = 0; i < lframes; i++) {
					
					line = br.readLine();
					lastAlphaFrames.alpha[i] = 1.0f-Float.parseFloat(line.substring(2));
					line = br.readLine();
					lastAlphaFrames.frames[i] = Integer.parseInt(line.substring(2).split(" ")[0]);
					
				}
				continue;
			}
			
		}
		br.close();
		
		res.objFiles = new String[lobjects];
		res.parent = new int[lobjects];
		ObjectKeyFrames[] okfs = new ObjectKeyFrames[lobjects];
		ObjectAlphaKeyFrames[] oakfs = new ObjectAlphaKeyFrames[lobjects];
		Vector3f[] objectPivot = new Vector3f[lobjects];
		for(int i = 0; i < lobjects; i++) {
			res.objFiles[i] = _objFiles.pop();
			res.parent[i] = _objParent.pop();
			okfs[i] = _objKeyFrames.pop();
			oakfs[i] = _objAlphaFrames.pop();
			objectPivot[i] = _objPivot.pop();
		}
		
		
		res.frames = new Frame[frames];
		int[] lastKeyFrameIndex = new int[lobjects];
		int[] lastAlphaKeyFramesIndex = new int[lobjects];
		for(int i = 0; i < frames; i++) {
			Frame frame = res.makeFrame();
			res.frames[i] = frame;
			frame.relalpha = new float[lobjects];
			frame.alpha = new float[lobjects];
			frame.transform = new Matrix4f[lobjects];
			frame.objectRelPos = new Vector3f[lobjects];
			frame.position = new Vector3f[lobjects];
			frame.objectRelRot = new Vector3f[lobjects];
			frame.rotation = new Vector3f[lobjects];
			frame.scales = new Vector3f[lobjects];
			frame.objectPivot = objectPivot;
			frame.parent = res.parent;
			int lastIndex = 0;
			ObjectKeyFrames okf;
			ObjectAlphaKeyFrames oakf;
			float scalar = 1.0f;
			for(int j = 0; j < lobjects; j++) {
				okf = okfs[j];
				int ind = 0;
				if(( ind = contains(okf.frames, i)) != -1)
					lastKeyFrameIndex[j] = ind;
				lastIndex = lastKeyFrameIndex[j];
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
			for(int j = 0; j < lobjects; j++) {
				oakf = oakfs[j];
				int ind = 0;
				if((ind = contains(oakf.frames, i)) != -1)
					lastAlphaKeyFramesIndex[j] = ind;
				lastIndex = lastAlphaKeyFramesIndex[j];
				if(lastIndex+1 < oakf.lframes) {
					scalar = (float) ((float) (oakf.frames[lastIndex] - i)) / ((float) (oakf.frames[lastIndex+1] - oakf.frames[lastIndex]));
					frame.relalpha[j] = oakf.alpha[lastIndex] + (oakf.alpha[lastIndex] - oakf.alpha[lastIndex+1])*scalar;
				} else {
					frame.relalpha[j] = oakf.alpha[lastIndex];
				}
				
				
			}
			
			frame.genTransforms();
			
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
		public float[] relalpha;
		public float[] alpha;
		public Matrix4f[] transform;
		public Vector3f[][] axis;
		public Vector3f[] objectRelPos;
		public Vector3f[] objectPivot;
		public Vector3f[] position;
		public Vector3f[] objectRelRot;
		public Vector3f[] rotation;
		public Vector3f[] scales;
		
		public void genTransforms() {
			transform = new Matrix4f[objectRelPos.length];
			position = new Vector3f[objectRelPos.length];
			for(int i = 0; i < objectRelPos.length; i++)
				genTransform(i);
		}
		
		private void genTransform(int i) {
			if(transform[i] != null)
				return;
			
			Matrix4f m = new Matrix4f().identity();
			
			Quaternionf rot = new Quaternionf();
			rot.rotateYXZ(objectRelRot[i].y, objectRelRot[i].x, objectRelRot[i].z);
//			m.scale(scales[i]);
			m.rotateAround(rot, objectPivot[i].x, objectPivot[i].y, objectPivot[i].z);
//			m.origin(new Vector3f(0));
			
			m.scaleAround(scales[i].x, scales[i].y, scales[i].z, objectPivot[i].x, objectPivot[i].y, objectPivot[i].z);
			m._m30(objectRelPos[i].x);
			m._m31(objectRelPos[i].y);
			m._m32(objectRelPos[i].z);
			
//			m.translate(objectRelPos[i]);
			
			alpha[i] = relalpha[i];
			if(parent[i] != -1) {
				genTransform(parent[i]);
				transform[parent[i]].mul(m, m);
				alpha[i] *= alpha[parent[i]];
			}
			transform[i] = m;
		}
	}
	
	static private class ObjectKeyFrames {
		public int lframes;
		public int[] frames;
		public Vector3f[] relPos;
		public Vector3f[] relRot;
		public Vector3f[] scales;
	}
	
	static private class ObjectAlphaKeyFrames {
		public int lframes;
		public int[] frames;
		public float[] alpha;
	}
	
}
