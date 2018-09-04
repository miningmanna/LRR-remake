package org.rrr;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	
	public Vector3f position;
	public Matrix4f frustrum;
	public Matrix4f combined;
	public Matrix4f rotate;
	
	public Vector3f up, right, direction;
	
	
	public Camera() {
		direction = new Vector3f(0, 0, 1);
		up = new Vector3f(0, 1, 0);
		right = new Vector3f(1, 0, 0);
		position = new Vector3f();
		frustrum = new Matrix4f();
		combined = new Matrix4f();
		rotate = new Matrix4f();
		rotate.identity();
		rotateX((float) Math.PI);
	}
	
	public void setFrustum(float fovy, float aspect, float zNear, float zFar) {
		frustrum.setPerspective(-fovy, -aspect, zNear, zFar);
	}
	
	public void update() {
		combined.set(frustrum);
		combined.mul(rotate);
		combined.translate(position);
	}
	
	public void move(Vector3f v) {
		Vector3f temp = new Vector3f(direction);
		position.add(temp.mul(v.z));
		position.add(0, v.y, 0);
		temp.set(right);
		position.add(temp.mul(v.x));
	}
	
	public void rotateY(float angle) {
		rotate.rotate(-angle, right);
		direction.rotateAxis(angle, right.x, right.y, right.z);
	}
	
	public void rotateX(float angle) {
		rotate.rotateY(angle);
		right.rotateY(-angle);
		direction.rotateY(-angle);
	}
	
}
