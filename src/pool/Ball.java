package pool;

import com.sun.j3d.utils.geometry.Sphere;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

public class Ball {
    Point2D.Double pos;
    Point2D.Double vel;
    Point2D.Double acc;
    double size;
    int alpha;
    Color color;
    boolean sel, sunk, remove, showDirection;
    
    //For testing
    Point2D.Double lastPos;
    Point2D.Double lastVel;
    
    //Java 3D
    BranchGroup group;
    TransformGroup transformGroup;
    Sphere sphere;
    Transform3D transform = new Transform3D();
    
    public Ball(Color col, double x, double y, double a, double b, double s){
        pos = new Point2D.Double(x,y);
	vel = new Point2D.Double(a,b);
	acc = new Point2D.Double(0,0);
	color = col;
	size = s;
	sunk = false;
	remove = false;
	alpha = 255;
        lastPos = new Point2D.Double(x,y);
	lastVel = new Point2D.Double(a,b);
        showDirection = true;
        
        group = new BranchGroup();
        transformGroup = new TransformGroup();
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        sphere = new Sphere((float)size/2);
        transformGroup.addChild(sphere);
        draw3D();
        group.addChild(transformGroup);
    }
    
    public double getcx(){
	return pos.x + size/2;
    }
    
    public double getcy(){
	return pos.y + size/2;
    }
    
    public final void draw3D() {
        transform.setTranslation(new Vector3d(pos.x, pos.y, 0.0));
        transformGroup.setTransform(transform);
    }
    

    public double detectCollisionWith(Ball ball) {
	// a b c are the terms of a quadratic.  at^2 + bt + c  This code uses the quadratic equation to check for collisions.
	double a = ( (ball.vel.x) * (ball.vel.x) + (vel.x) * (vel.x) - 2*(ball.vel.x)*(vel.x) +
		     (ball.vel.y)*(ball.vel.y) + (vel.y) * (vel.y) - 2*(ball.vel.y)*(vel.y) );
	
	double b = 2 * ( (ball.getcx() * ball.vel.x) + (getcx() * vel.x) - (ball.getcx() * vel.x) -
			 (getcx() * ball.vel.x) + (ball.getcy() * ball.vel.y) + (getcy() * vel.y) - 
			 (ball.getcy() * vel.y) - (getcy() * ball.vel.y) );
	
	double c = ball.getcx() * ball.getcx() + getcx() * getcx() - 2 * (getcx() * ball.getcx()) +
	    ball.getcy() * ball.getcy() + getcy() * getcy() - 2 * (getcy() * ball.getcy())
	    - (size+ball.size)*(size+ball.size)/4;
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

    public double detectCollisionWith(Point p) {
	double a,b,c,t;
	a = vel.y*vel.y + vel.x*vel.x;
	b = 2*(vel.y*(getcy()-p.y) + vel.x*(getcx() - p.x));
	c = p.x*p.x + p.y*p.y - 2*p.x*getcx() - 2*p.y*getcy() +
	    getcy()*getcy() + getcx()*getcx() - size*size/4;
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
	b = 2*(vel.y*(getcy()-p.y) + vel.x*(getcx() - p.x));
	c = p.x*p.x + p.y*p.y - 2*p.x*getcx() - 2*p.y*getcy()
	    + getcy()*getcy() + getcx()*getcx() - distance*distance;
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
        Point2D.Double myCenter = new Point2D.Double(getcx(), getcy());
        if(myCenter.distance(ball.getcx(), ball.getcy())+.5< ((double)(size + ball.size))/2)
            return true;
        return false; 
    }
    

}