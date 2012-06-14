package pool;

import com.sun.j3d.utils.geometry.Sphere;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.geom.Point2D;
import javax.media.j3d.*;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import vector.Rotater3f;

public class PoolBall extends Sphere{
    Appearance appearance;
    TransparencyAttributes ta = new TransparencyAttributes(TransparencyAttributes.NONE, .0f);
    Vector3d pos, lpos;
    Vector3d vel, lvel;    
    Vector3d spin;
    double size;
    boolean sunk, active, wasactive, isRolling = true;
    float transparency = 1.0f;
    int ballNumber;
    
    Rotater3f rotater = new Rotater3f();
        
    //Java 3D
    BranchGroup group;
    TransformGroup transformGroup;
    Quat4f rotation = new Quat4f(), velRotation = new Quat4f();
    Transform3D transform = new Transform3D();
    private Vector3f aVector = new Vector3f();
    
    //For dogravity
    Vector3d acceleration = new Vector3d();
    Point3d contactPoint = new Point3d();
    Point3d sub = new Point3d();
    
    public PoolBall(Appearance app, double s, int bn){
        super((float)s, Sphere.GENERATE_TEXTURE_COORDS | Sphere.GENERATE_NORMALS, 30);
        //Get values
        size = s;
        appearance = app;
        ballNumber = bn;
        
        //Set flags
        active = false;
        sunk = false;        
        rotation.w = 1.0f;
        
        //Initialize instance variables
        pos = new Vector3d();
        vel = new Vector3d();
        lpos = new Vector3d();
        lvel = new Vector3d();
        spin = new Vector3d();
        transformGroup = new TransformGroup();
        group = new BranchGroup();        
        
        transformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        ta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
        ta.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
        appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
        appearance.setTransparencyAttributes(ta);
        this.setAppearance(appearance);
        transform.setTranslation(pos);
        transformGroup.setTransform(transform);
        transformGroup.addChild(this);
        group.addChild(transformGroup);
        setInactivePos();
        move(0);
    }    
    
    public final boolean move(double t) {
        pos.x += vel.x*t;
        pos.y += vel.y*t;
        pos.z += vel.z*t;
        double angle = (spin.length()*t);
        Vector3f spinPerp = new Vector3f((float)-spin.y, (float)spin.x, (float)spin.z);
        spinPerp.normalize();
        spinPerp.scale((float)Math.sin(angle/2));
        velRotation.set(spinPerp.x,
                        spinPerp.y,
                        spinPerp.z, 
                        (float)Math.cos(angle/2));        
        if(spin.length() > 0) {
            rotation.mul(velRotation, rotation);
        }        
        transform.setRotation(rotation);
        transform.setTranslation(new Vector3d(pos));                
        transformGroup.setTransform(transform);
        return vel.length() != 0;
    }
    
    public final void setInactivePos() {
        pos.set(2*(ballNumber-.5f)*size-(size*15), PoolSimulation.height/2 + PoolSimulation.borderSize + 2*size, 0.0);
    }    

    public double detectCollisionWith(PoolBall ball) {
	// a b c are the terms of a quadratic.  at^2 + bt + c  This code uses the quadratic equation to check for collisions.
	double a = ( (ball.vel.x) * (ball.vel.x) + (vel.x) * (vel.x) - 2*(ball.vel.x)*(vel.x) +
		     (ball.vel.y) * (ball.vel.y) + (vel.y) * (vel.y) - 2*(ball.vel.y)*(vel.y) +
                     (ball.vel.z) * (ball.vel.z) + (vel.z) * (vel.z) - 2*(ball.vel.z)*(vel.z));
	
	double b = 2 * ((ball.pos.x * ball.vel.x) + (pos.x * vel.x) - (ball.pos.x * vel.x) - (pos.x * ball.vel.x) + 
                        (ball.pos.y * ball.vel.y) + (pos.y * vel.y) - (ball.pos.y * vel.y) - (pos.y * ball.vel.y) +
                        (ball.pos.z * ball.vel.z) + (pos.z * vel.z) - (ball.pos.z * vel.z) - (pos.z * ball.vel.z));
	
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
        
        if(vel.length() + spin.length() == 0) {
            return;
        }
        
        if(vel.z != 0) {
            return;
        }
        
        if(Math.abs(spin.z) > 0)
            spin.z -= Math.copySign(PoolSettings.englishDecay, spin.z);
        else
            spin.z = 0;
        isRolling = false;
        //Determine the dirction of friction.
        Vector3f fd = new Vector3f((float)spin.x, (float)spin.y, 0.0f);
        fd.scale((float)size);
        fd.x -= vel.x;
        fd.y -= vel.y;
        
        //Rolling without slipping.
        if(fd.length() < PoolSimulation.frictionThreshold) {
            isRolling = true;
            if(vel.length() > 0) {
                Vector3d sub = new Vector3d();
                sub.set(vel);
                sub.normalize();
                sub.scale(-1*PoolSimulation.rollingResistance);
                if(sub.length() > vel.length()) {
                    vel.set(0.0, 0.0, 0.0);
                    spin.set(vel.x, vel.y, spin.z);
                    return;
                }
                vel.add(sub);
                aVector.set(vel);
                aVector.scale((float)(1/size));
                spin.set(aVector.x, aVector.y, spin.z);             
            }
            return;
        }
        
        //Apply the force of friction.
        fd.normalize();
        fd.scale(PoolSimulation.friction);
        vel.x += fd.x;
        vel.y += fd.y;
        fd.scale((float)(2.5/size));

        spin.x -= fd.x;
        spin.y -= fd.y;
    }
    
    void doGravity(PoolPocket pocket) {
        if(pos.z <= (-PoolSimulation.pocketDepth+size)) {
            vel.set(0.0,0.0,0.0); 
            spin.set(vel);
            ballSunk();
            return;
        }
        acceleration.set((float)(pos.x - pocket.pos.x),
                         (float)(pos.y - pocket.pos.y),
                         0.0f);
        acceleration.normalize();
        acceleration.scale(pocket.size);        
        contactPoint.set(pocket.pos.x, pocket.pos.y, -size);
        contactPoint.add(acceleration);        
        if(pos.epsilonEquals(contactPoint, size)) {
            acceleration.set(pos);
            sub.set(contactPoint);
            sub.scale(-1);
            acceleration.add(sub);
            acceleration.normalize();
            if(acceleration.z != 0) {
                double scale = (1/acceleration.z);
                vel.x += acceleration.x*scale * PoolSimulation.gravity;
                vel.y += acceleration.y*scale * PoolSimulation.gravity;
            }
            
            acceleration.set(pos);
            acceleration.add(vel);
            if(acceleration.epsilonEquals(contactPoint, size-.01)) {
                System.out.println("Problem");
            }            
        } else {
            vel.z -= PoolSimulation.gravity;
        }        
    }
    
    public void decreaseTransparency() {
        if(transparency > 0) {
            transparency -=.006f;
            ta.setTransparency(transparency);
        } else {
            sunk = false;
            ta.setTransparencyMode(TransparencyAttributes.NONE);
        }
    }
    
    public void startFadeIn() {
        transparency = 1.0f;
        ta.setTransparencyMode(TransparencyAttributes.FASTEST);
        ta.setTransparency(transparency);
        sunk = true;
    }
    
    public void ballSunk() {
        startFadeIn();
        if(this.ballNumber == 0) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            pos.set((double)p.x, (double)p.y, 0.0);
            active = true;
            sunk = true;            
            PoolSimulation.ref.mouseController.putBallInHand(this);                        
        } else {
            active = false;
            setInactivePos();           
            rotation.set(0.0f, 0.0f, 0.0f, 1.0f);
        }            
    }

    boolean checkOverlap(Vector3f loc) {
        return loc.epsilonEquals(new Vector3f(pos), (float)(2*size));
    }
}