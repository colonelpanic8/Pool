package pool;

import com.sun.j3d.utils.geometry.Sphere;
import java.awt.geom.Point2D;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import vector.ChangeBasis3f;

public class PoolBall {
    protected static final float friction = .01f, rollingResistance = .01f, frictionThreshold = .01f;
    Appearance appearance;
    Point3d pos, lpos;
    Vector3d vel, lvel;    
    Vector3d spin;
    double size;
    boolean sunk, active;
    
    ChangeBasis3f changeBasis = new ChangeBasis3f();
        
    //Java 3D
    BranchGroup group;
    TransformGroup transformGroup;
    Sphere sphere;
    Quat4f rotation = new Quat4f(), velRotation = new Quat4f();
    Transform3D transform = new Transform3D();
    
    public PoolBall(Appearance app, double s){
        //Get values
        size = s;
        appearance = app;
        
        //Set flags
        active = false;
        sunk = false;  
        rotation.w = 1.0f;
        
        //Initialize instance variables
        pos = new Point3d();
        vel = new Vector3d();
        lpos = new Point3d();
        lvel = new Vector3d();
        spin = new Vector3d();
        transformGroup = new TransformGroup();
        group = new BranchGroup();
        sphere = new Sphere((float)size, Sphere.GENERATE_TEXTURE_COORDS | Sphere.GENERATE_NORMALS, 30);
        
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        sphere.setAppearance(appearance);
        transform.setTranslation(new Vector3d(pos.x, pos.y, 0.0));
        transformGroup.setTransform(transform);
        transformGroup.addChild(sphere);
        group.addChild(transformGroup);
                
    }
    
    public PoolBall(Appearance app, double x, double y, double a, double b, double s){
        pos = new Point3d(x,y,0.0);
	vel = new Vector3d(a,b,0.0);
        lpos = new Point3d(x,y,0.0);
	lvel = new Vector3d(a,b,0.0);
        spin = new Vector3d();
        appearance = app;
	size = s;
	sunk = false;
        active = true;
        group = new BranchGroup();
        transformGroup = new TransformGroup();
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        sphere = new Sphere((float)size, Sphere.GENERATE_TEXTURE_COORDS | Sphere.GENERATE_NORMALS, 30);
        sphere.setAppearance(appearance);
        transformGroup.addChild(sphere);
        rotation.w = 1.0f;
        transform.setTranslation(new Vector3d(pos.x, pos.y, 0.0));
        transformGroup.setTransform(transform);
        group.addChild(transformGroup);
    }
    
    public final void move(double t) {
        pos.x += vel.x*t;
        pos.y += vel.y*t;
        pos.z += vel.z*t;
        double angle = (vel.length()*t/size);
        Vector3f velPerp = new Vector3f((float)-vel.y, (float)vel.x, 0f);
        velPerp.normalize();
        velPerp.scale((float)Math.sin(angle/2));
        velRotation.set(velPerp.x,
                     velPerp.y,
                     0, 
                     (float)Math.cos(angle/2));
        
        if(vel.length() > 0) {
            rotation.mul(velRotation, rotation);
        }
        
        transform.setRotation(rotation);
        transform.setTranslation(new Vector3d(pos));                
        transformGroup.setTransform(transform);
    }
    

    public double detectCollisionWith(PoolBall ball) {
	// a b c are the terms of a quadratic.  at^2 + bt + c  This code uses the quadratic equation to check for collisions.
	double a = ( (ball.vel.x) * (ball.vel.x) + (vel.x) * (vel.x) - 2*(ball.vel.x)*(vel.x) +
		     (ball.vel.y) * (ball.vel.y) + (vel.y) * (vel.y) - 2*(ball.vel.y)*(vel.y) +
                     (ball.vel.z) * (ball.vel.z) + (vel.z) * (vel.z) - 2*(ball.vel.z)*(vel.z));
	
	double b = 2 * ((ball.pos.x * ball.vel.x) + (pos.x * vel.x) - (ball.pos.x * vel.x) - (pos.x * ball.vel.x) + 
                        (ball.pos.y * ball.vel.y) + (pos.y * vel.y) - (ball.pos.y * vel.y) - (pos.y * ball.vel.y) +
                        (ball.pos.z * ball.vel.z) + (pos.y * vel.z) - (ball.pos.z * vel.z) - (pos.z * ball.vel.z));
	
	double c = ball.pos.x * ball.pos.x + pos.x * pos.x - 2 * (pos.x * ball.pos.x) +
                   ball.pos.y * ball.pos.y + pos.y * pos.y - 2 * (pos.y * ball.pos.y) +
                   ball.pos.z * ball.pos.z + pos.z * pos.z - 2 * (pos.z * ball.pos.z) -
                   (size+ball.size)*(size+ball.size);
	double t;
	if (a !=0 ){
	    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);  // These are the two solutions to the quadratic equation.
	    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);  // The smaller solution is always selected (unless it is
	    t = t1 < t2 ? t1 : t2;
	} else {
	    t = -c/b;  
	}
        if(Math.abs(t)<.001 && t < 0) {
            t = 0;
        }
	return t; 
    }

    public double detectCollisionWith(Point2D.Double p) {
	double a,b,c,t;
	a = vel.y*vel.y + vel.x*vel.x;
	b = 2*(vel.y*(pos.y-p.y) + vel.x*(pos.x - p.x));
	c = p.x*p.x + p.y*p.y - 2*p.x*pos.x - 2*p.y*pos.y +
	    pos.y*pos.y + pos.x*pos.x - size*size;
	if (a !=0 ){
	    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
	    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
	    t = t1 < t2 ? t1 : t2;
	} else {
	    t = -c/b;  
	}
	return t;
    }

    public Point2D.Double detectCollisionWith(Point2D.Double p, double distance) {
	double a,b,c;
	a = vel.y*vel.y + vel.x*vel.x;
	b = 2*(vel.y*(pos.y-p.y) + vel.x*(pos.x - p.x));
	c = p.x*p.x + p.y*p.y - 2*p.x*pos.x - 2*p.y*pos.y
	    + pos.y*pos.y + pos.x*pos.x - distance*distance;
	if (a !=0 ){
	    return new Point2D.Double((-b - Math.sqrt(b*b-4*a*c) )/(2 * a),
                                      (-b + Math.sqrt(b*b-4*a*c) )/(2 * a));
            
	} else {
	   return new Point2D.Double(-c/b, -1.0);
	}        
    }
    
    public boolean checkOverlap(PoolBall ball) {
        Point2D.Double myCenter = new Point2D.Double(pos.x, pos.y);
        if(myCenter.distance(ball.pos.x, ball.pos.y)< ((double)(size + ball.size)))
            return true;
        return false; 
    }

    void updateVelocity() {
        
        if(vel.z != 0) {
            return;
        }
        
        //Determine the dirction of friction.
        Vector3f fd = new Vector3f((float)spin.x, (float)spin.y, 0.0f);
        fd.scale((float)size);
        fd.x -= vel.x;
        fd.y -= vel.y;
        
        //Rolling without slipping.
        if(fd.length() < frictionThreshold) {
            if(vel.length() > 0) {
                Vector3d sub = new Vector3d();
                sub.set(vel);
                sub.normalize();
                sub.scale(-1*rollingResistance);
                if(sub.length() > vel.length()) {
                    vel.set(0.0, 0.0, 0.0);
                    spin.set(vel);
                    return;
                }
                vel.add(sub);
                spin.set(vel);
                spin.scale(1/size);
             
            }
            return;
        }
        
        //Apply the force of friction.
        fd.normalize();
        fd.scale(friction);
        vel.x += fd.x;
        vel.y += fd.y;
        fd.scale((float)(2.5/size));
        spin.x += fd.x;
        spin.y += fd.y;     
    }
}