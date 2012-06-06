/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vector;

import javax.vecmath.*;

/**
 *
 * @author ivanmalison
 */
public class Rotater3f {
    
    protected Quat4f rotation = new Quat4f(), inverse = new Quat4f(), vector = new Quat4f();
    
    public Rotater3f() { }
    
    public Rotater3f(Vector3f axis, double angle) {
        setRotation(axis, angle);
    }
    
    public final void setRotation(Vector3f axis, double angle) {
        Vector3f a = new Vector3f(axis);
        a.scale((float)Math.sin(angle/2));
        rotation.set(a.x,
                     a.y,
                     a.z, 
                     (float)Math.cos(angle/2));
        inverse.inverse(rotation);
    }
    
    public Vector3f setAndRotate(Vector3f axis, double angle, Tuple3f obj) {
        Vector3f a = new Vector3f(axis);
        a.scale((float)Math.sin(angle/2));
        rotation.set(a.x,
                     a.y,
                     a.z, 
                     (float)Math.cos(angle/2));
        inverse.inverse(rotation);
        vector.set(obj.x,
                   obj.y,
                   obj.z,
                   0.0f);
        vector.mul(rotation,vector);
        vector.mul(inverse);
        return new Vector3f(vector.x, vector.y, vector.z);
    }
    
    public void setAndRotateInPlace(Vector3f axis, double angle, Tuple3f obj) {
        Vector3f a = new Vector3f(axis);
        a.scale((float)Math.sin(angle/2));
        rotation.set(a.x,
                     a.y,
                     a.z, 
                     (float)Math.cos(angle/2));
        inverse.inverse(rotation);
        vector.set(obj.x,
                   obj.y,
                   obj.z,
                   0.0f);
        vector.mul(rotation,vector);
        vector.mul(inverse);
        obj.set(vector.x, vector.y, vector.z);
    }
    
    public void setAndRotateInPlace(Vector3f axis, double angle, Tuple3d obj) {
        Vector3f a = new Vector3f(axis);
        a.scale((float)Math.sin(angle/2));
        rotation.set(a.x,
                     a.y,
                     a.z, 
                     (float)Math.cos(angle/2));
        inverse.inverse(rotation);
        vector.set((float)obj.x,
                   (float)obj.y,
                   (float)obj.z,
                   0.0f);
        vector.mul(rotation,vector);
        vector.mul(inverse);
        obj.set(vector.x, vector.y, vector.z);
    }
    
    public Vector3f rotate(Tuple3f obj) {
        vector.set(obj.x,
                   obj.y,
                   obj.z,
                   0.0f);
        vector.mul(rotation,vector);
        vector.mul(inverse);
        return new Vector3f(vector.x, vector.y, vector.z);
    }
    
    public void rotateInPlace(Tuple3f obj) {
        vector.set(obj.x,
                   obj.y,
                   obj.z,
                   0.0f);
        vector.mul(rotation,vector);
        vector.mul(inverse);
        obj.set(vector.x, vector.y, vector.z);
    }

    public void rotateInPlace(Vector3d obj) {
        vector.set((float)obj.x,
                   (float)obj.y,
                   (float)obj.z,
                   0.0f);
        vector.mul(rotation,vector);
        vector.mul(inverse);
        obj.set(vector.x, vector.y, vector.z);
    }
}
