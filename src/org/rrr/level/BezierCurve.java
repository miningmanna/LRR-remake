package org.rrr.level;

import java.util.List;

import org.joml.Vector2f;

public class BezierCurve {
	
	float[] points;
	float[][] temp;
	public BezierCurve(List<Vector2f> _points) {
		this.points = new float[_points.size()*2];
		this.temp = new float[2][this.points.length];
		for(int i = 0; i < _points.size(); i++) {
			this.points[i*2] = _points.get(i).x;
			this.points[i*2+2] = _points.get(i).y;
		}
	}
	
	public BezierCurve(float[] _points) {
		this.points = new float[_points.length];
		this.temp = new float[2][this.points.length];
		System.arraycopy(_points, 0, points, 0, _points.length);
	}
	
	public Vector2f getPoint(float t) {
//		if(t > 1 || t < 0)
//			return null;
		System.arraycopy(points, 0, temp[0], 0, points.length);
		int curTemp = 0, nextTemp = 1;
		for(int pointNum = points.length/2; pointNum > 1; pointNum--) {
			System.out.println(pointNum);
			for(int j = 0; j < pointNum-1; j++) {
				System.out.println("j:" + j);
				temp[nextTemp][j*2]		= ((1-t)*temp[curTemp][j*2])	+ (t*temp[curTemp][(j+1)*2]);
				temp[nextTemp][j*2+1]	= ((1-t)*temp[curTemp][j*2+1])	+ (t*temp[curTemp][(j+1)*2+1]);
			}
			curTemp++;
			curTemp%=2;
			nextTemp++;
			nextTemp%=2;
		}
		return new Vector2f(temp[curTemp][0],temp[curTemp][1]);
	}
	
}
