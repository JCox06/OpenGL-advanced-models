package uk.co.jcox.gllights;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    public static final float Z_NEAR = 0.01f;
    public static final float Z_FAR = 100.0f;

    private final Vector3f position = new Vector3f(0.0f, 2.0f, 10.0f);
    private final Vector3f facing = new Vector3f(0.0f, 0.0f, -1.0f);
    private Vector3f right = new Vector3f();
    private final Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

    private float yaw = 280;
    private float pitch = 0.0f;


    public Matrix4f getLookAt() {

        right = facing.cross(up, new Vector3f()).normalize();


        facing.x = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        facing.y = Math.sin(Math.toRadians(pitch));
        facing.z = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));

        return new Matrix4f().setLookAt(position, position.add(facing, new Vector3f()), up);
    }


    public void rotate(float xAmount, float yAmount) {
        yaw += xAmount;
        pitch += yAmount;
    }

    public Matrix4f getProjection(float aspectRatio) {
        return new Matrix4f().perspective((float) Math.PI / 4, aspectRatio, Z_NEAR, Z_FAR);
    }

    public void moveForward(float velocity) {
        this.position.add(facing.mul(velocity, new Vector3f()));
    }


    public void moveRight(float velocity) {
        this.position.add(right.mul(velocity, new Vector3f()));
    }

    public Vector3f getPosition() {
        return position;
    }
}