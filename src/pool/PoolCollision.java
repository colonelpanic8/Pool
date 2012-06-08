package pool;

import java.awt.geom.Point2D;
import java.util.Iterator;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import vector.ChangeBasis3f;

public abstract class PoolCollision {
    double time;
    PoolBall ball1;
    
    public static float velScale = .5f;

    public abstract void doEffects(PoolPanel pp);

    public void doCollision(PoolPanel pp) {
	Iterator<PoolCollision> collIterator = pp.collisions.iterator();
	Iterator<PoolBall> ballIterator;
	while(collIterator.hasNext()) {
	    PoolCollision item = collIterator.next();
	    if(item.involves(ball1)) {
		collIterator.remove();
	    }
	}
	doEffects(pp);
	ballIterator = pp.balls.iterator();
        
	while(ballIterator.hasNext()) {
	    PoolBall ball = ballIterator.next();
	    if(ball != ball1) {
		double t = ball1.detectCollisionWith(ball);
		t += time;
		if(t < 1 && t >= time){
		    pp.collisions.add(new BallCollision(t, ball1, ball));
		}
	    }
	}
	detectPolygonCollisions(pp, ball1);
	pp.detectPocketCollisions(ball1, time);
    }
        
    public boolean involves(PoolBall b) {
        return ball1 == b;
    }
    
    public void detectPolygonCollisions(PoolPanel pp, PoolBall x) {
        pp.detectPolygonCollisions(x, time);
    }
}

class BallCollision extends PoolCollision {
    PoolBall ball2;

    public BallCollision(double t, PoolBall b, PoolBall c){
	time = t;
	ball1 = b;
        ball2 = c;
    }

    @Override public void doCollision(PoolPanel pp) {
	Iterator<PoolBall> ballIterator;
        Iterator<PoolCollision> collIterator = pp.collisions.iterator();
        
        //Remove collisions involiving the balls
        while(collIterator.hasNext()) {
	    PoolCollision item = collIterator.next();
	    if(item.involves(ball1)) {
		collIterator.remove();
	    }
	}
        collIterator = pp.collisions.iterator();
        while(collIterator.hasNext()) {
	    PoolCollision item = collIterator.next();
	    if(item.involves(ball2)) {
		collIterator.remove();
	    }
	}
        
        doEffects(pp);
        
       
        //Check for new collisions involving the balls
	ballIterator = pp.balls.iterator();
	while(ballIterator.hasNext()) {
	    PoolBall ball = ballIterator.next();
	    if(ball != ball1 && ball != ball2) {
		double t = ball1.detectCollisionWith(ball);
		t += time;
		if(t <= 1 && t >= time){
		    pp.collisions.add(new BallCollision(t, ball1, ball));
		}
	    }
	}
        
        ballIterator = pp.balls.iterator();
	while(ballIterator.hasNext()) {
	    PoolBall ball = ballIterator.next();
	    if(ball != ball1 && ball != ball2){
		double t = ball2.detectCollisionWith(ball);
		t += time;
		if(t <= 1 && t >= time){
		    pp.collisions.add(new BallCollision(t, ball2, ball));
		}
	    }
	}
        
	detectPolygonCollisions(pp, ball1);
	pp.detectPocketCollisions(ball1, time);
	detectPolygonCollisions(pp, ball2);
	pp.detectPocketCollisions(ball2, time);
    }
    
    @Override public void doEffects(PoolPanel pp) {
        if(ball1.sunk || ball2.sunk) {
            ball1.vel.set(0.0, 0.0, 0.0);
            ball2.vel.set(0.0, 0.0, 0.0);
            ball1.sunk = true;
            ball2.sunk = true;
            return;
        }
	//Collision effects
	double xdif = ball2.pos.x - ball1.pos.x;
	double ydif = ball2.pos.y - ball1.pos.y;
	double dist = Math.sqrt(xdif*xdif + ydif*ydif);
	double xp = xdif/dist;
	double yp = ydif/dist;
	double xo = -yp;
	double yo = xp;
	double vp1 = xp * ball1.vel.x + yp * ball1.vel.y;
	double vp2 = xp * ball2.vel.x + yp * ball2.vel.y;
	double vo1 = xo * ball1.vel.x + yo * ball1.vel.y;
	double vo2 = xo * ball2.vel.x + yo * ball2.vel.y;
	ball1.vel.x = vp2 * xp - vo1 * yp;
	ball1.vel.y = vp2 * yp + vo1 * xp;
	ball2.vel.x = vp1 * xp - vo2 * yp;
	ball2.vel.y = vp1 * yp + vo2 * xp;
    }
    
    @Override public boolean involves(PoolBall b) {
        return (b == ball1 || b == ball2);
    }
}

class WallCollision extends PoolCollision {
    Vector3d newVel;
    PoolPolygon poly;
    int wall;
    public WallCollision(double t, PoolBall b, Vector3d v, PoolPolygon p, int w) {
	time = t;
	ball1 = b;
        newVel = v;
        poly = p;
        wall = w;
    }

    @Override public void doEffects(PoolPanel pp) {
        ball1.vel = newVel;
    }
    
    @Override public void detectPolygonCollisions(PoolPanel pp, PoolBall x) {
        pp.detectPolygonCollisions(x, time, this);
    }
}

class PointCollision extends PoolCollision {
    Point2D.Double point;
    
    public PointCollision(double t, Point2D.Double p, PoolBall b) {
	time = t;
	ball1 = b;
	point = p;
    }

    @Override public void doEffects(PoolPanel pp) {
        Vector3d res;
	Point2D.Double unit, trans, temp;
	double dist = point.distance(ball1.pos.x, ball1.pos.y);
	unit = new Point2D.Double((point.x - ball1.pos.x)/dist,
				  (point.y - ball1.pos.y)/dist );
	trans = new Point2D.Double(1/(unit.x + unit.y*unit.y/unit.x), 1/(unit.y + unit.x*unit.x/unit.y));
	temp = new Point2D.Double(trans.x*ball1.vel.x, trans.y*ball1.vel.x);
	temp.x += trans.y*ball1.vel.y;
	temp.y += -trans.x*ball1.vel.y;
	temp.x = -temp.x;
	
	res = new Vector3d(temp.x*unit.x, temp.x*unit.y, ball1.vel.z);
	res.x += temp.y*unit.y;
	res.y += -temp.y*unit.x;
	ball1.vel = res;
    }
}


class SinkCollision extends PoolCollision {
    PoolPocket pocket;
    
    public SinkCollision(PoolBall b, double t, PoolPocket p) {
        ball1 = b;
	time = t;
        pocket = p;
    }
    
    @Override
    public void doEffects(PoolPanel pp) {
    }
    
}

class PocketCollision extends PoolCollision {
    PoolPocket pocket;
    public PocketCollision(PoolBall b, double t, PoolPocket p) {
	ball1 = b;
	time = t;
        pocket = p;
    }
    
    @Override public void doEffects(PoolPanel pp) {
        Vector3f colDir = new Vector3f((float)(ball1.pos.x - pocket.pos.x),
                (float)(ball1.pos.y - pocket.pos.y),
                0.0f);
        Vector3f vel = new Vector3f(0.0f, 0.0f, 1.0f);
        ChangeBasis3f cb = new ChangeBasis3f(colDir, new Vector3f(colDir.y, -colDir.x, colDir.z),
                vel);
        vel.set((float)ball1.vel.x, (float)ball1.vel.y, 0.0f);
        cb.transform(vel);
        vel.x *= -1;
        cb.invert();
        cb.transform(vel);
        vel.scale(velScale);
        ball1.vel.x = vel.x;
	ball1.vel.y = vel.y;	
    }
}
