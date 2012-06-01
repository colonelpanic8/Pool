package pool;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import javax.media.j3d.*;
import javax.vecmath.*;

public class Ball {
    myPoint3d pos;
    Point2D.Double vel;
    myPoint3d lpos;
    Point2D.Double lvel;
    
    Point2D.Double acc;
    double size;
    int alpha;
    Color color;
    boolean sel, sunk, remove, showDirection;
        
    //Java 3D
    BranchGroup group;
    TransformGroup transformGroup;
    Sphere sphere;
    Quat4f rotation = new Quat4f(), velRotation = new Quat4f();
    Transform3D transform = new Transform3D();
    
    public Ball(Color col, double x, double y, double a, double b, double s){
        pos = new myPoint3d(x,y,(double)0);
	vel = new Point2D.Double(a,b);
        lpos = new myPoint3d(x,y,(double)0);
	lvel = new Point2D.Double(a,b);
	acc = new Point2D.Double(0,0);
	size = s;
        color = col;
	sunk = false;
	remove = false;
	alpha = 255;
        showDirection = true;
        
        group = new BranchGroup();
        transformGroup = new TransformGroup();
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        sphere = new Sphere((float)size);
        transformGroup.addChild(sphere);
        rotation.w = 1.0f;
        transform.setTranslation(new Vector3d(pos.x, pos.y, 0.0));
        transformGroup.setTransform(transform);
        group.addChild(transformGroup);
    }
            
    
    public Ball(Appearance appearance, double x, double y, double a, double b, double s){
        pos = new myPoint3d(x,y,(double)0);
	vel = new Point2D.Double(a,b);
        lpos = new myPoint3d(x,y,(double)0);
	lvel = new Point2D.Double(a,b);
        
	acc = new Point2D.Double(0,0);
	size = s;
	sunk = false;
	remove = false;
	alpha = 255;
        showDirection = true;
        
 
        
        group = new BranchGroup();
        transformGroup = new TransformGroup();
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        sphere = new Sphere((float)size, Sphere.GENERATE_TEXTURE_COORDS, appearance);
        transformGroup.addChild(sphere);
        rotation.w = 1.0f;
        transform.setTranslation(new Vector3d(pos.x, pos.y, 0.0));
        transformGroup.setTransform(transform);
        group.addChild(transformGroup);
    }
    
    public final void move(double t) {
        pos.x += vel.x*t;
        pos.y += vel.y*t;
        double angle = (vel.distance(0.0,0.0)*t/size);
        Vector3f velPerp = new Vector3f((float)-vel.y, (float)vel.x, 0f);
        velPerp.normalize();
        velPerp.scale((float)Math.sin(angle/2));
        velRotation.set(velPerp.x,
                     velPerp.y,
                     0, 
                     (float)Math.cos(angle/2));
        
        if(vel.distance(0.0,0.0) > 0) {
            rotation.mul(velRotation, rotation);
        }
        
        transform.setRotation(rotation);
        transform.setTranslation(new Vector3d(pos));                
        transformGroup.setTransform(transform);
    }
    

    public double detectCollisionWith(Ball ball) {
	// a b c are the terms of a quadratic.  at^2 + bt + c  This code uses the quadratic equation to check for collisions.
	double a = ( (ball.vel.x) * (ball.vel.x) + (vel.x) * (vel.x) - 2*(ball.vel.x)*(vel.x) +
		     (ball.vel.y) * (ball.vel.y) + (vel.y) * (vel.y) - 2*(ball.vel.y)*(vel.y) );
	
	double b = 2 * ((ball.pos.x * ball.vel.x) + (pos.x * vel.x) - (ball.pos.x * vel.x) -
                (pos.x * ball.vel.x) + (ball.pos.y * ball.vel.y) + (pos.y * vel.y) - 
                (ball.pos.y * vel.y) - (pos.y * ball.vel.y));
	
	double c = ball.pos.x * ball.pos.x + pos.x * pos.x - 2 * (pos.x * ball.pos.x) +
                   ball.pos.y * ball.pos.y + pos.y * pos.y - 2 * (pos.y * ball.pos.y) -
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

    public double detectCollisionWith(Point p, int distance) {
	double a,b,c,t;
	a = vel.y*vel.y + vel.x*vel.x;
	b = 2*(vel.y*(pos.y-p.y) + vel.x*(pos.x - p.x));
	c = p.x*p.x + p.y*p.y - 2*p.x*pos.x - 2*p.y*pos.y
	    + pos.y*pos.y + pos.x*pos.x - distance*distance;
	if (a !=0 ){
	    double t1 = ( -b - Math.sqrt(b*b-4*a*c) )/(2 * a);
	    double t2 = ( -b + Math.sqrt(b*b-4*a*c) )/(2 * a);
	    t = t1 < t2 ? t1 : t2;
	} else {
	    t = -c/b;  
	}
	return t;
    }
    
    public boolean checkOverlap(Ball ball) {
        Point2D.Double myCenter = new Point2D.Double(pos.x, pos.y);
        if(myCenter.distance(ball.pos.x, ball.pos.y)< ((double)(size + ball.size)))
            return true;
        return false; 
    }
}

class myPoint3d extends Point3d {
    public myPoint3d(double a, double b, double c) {
        super(a,b,c);
    }
    
    public void setLocation(double a, double b, double c) {
        x = a;
        y = b;
        z = c;
    }
}