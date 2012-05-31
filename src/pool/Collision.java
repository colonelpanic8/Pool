package pool;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Iterator;

public abstract class Collision {
    double time;
    Ball ball1;

    public abstract void doEffects(PoolPanel pp);

    public void doCollision(PoolPanel pp) {
	Iterator<Collision> collIterator = pp.collisions.iterator();
	Iterator<Ball> ballIterator;
	while(collIterator.hasNext()) {
	    Collision item = collIterator.next();
	    if(item.involves(ball1)) {
		collIterator.remove();
	    }
	}
	doEffects(pp);
	ballIterator = pp.balls.iterator();
        
	while(ballIterator.hasNext()) {
	    Ball ball = ballIterator.next();
	    if(ball != ball1) {
		double t = ball1.detectCollisionWith(ball);
		t += time;
		if(t < 1 && t >= time){
		    pp.collisions.add(new BallCollision(t, ball1, ball));
		}
	    }
	}
	detectPolygonCollisions(pp, ball1);
	pp.checkPockets(ball1, time);
    }
        
    public boolean involves(Ball b) {
        return ball1 == b;
    }
    
    public void detectPolygonCollisions(PoolPanel pp, Ball x) {
        pp.detectPolygonCollisions(x, time);
    }
}

class BallCollision extends Collision {
    Ball ball2;

    public BallCollision(double t, Ball b, Ball c){
	time = t;
	ball1 = b;
        ball2 = c;
    }

    @Override public void doCollision(PoolPanel pp) {
	Iterator<Ball> ballIterator;
        Iterator<Collision> collIterator = pp.collisions.iterator();
        
        //Remove collisions involiving the balls
        while(collIterator.hasNext()) {
	    Collision item = collIterator.next();
	    if(item.involves(ball1)) {
		collIterator.remove();
	    }
	}
        collIterator = pp.collisions.iterator();
        while(collIterator.hasNext()) {
	    Collision item = collIterator.next();
	    if(item.involves(ball2)) {
		collIterator.remove();
	    }
	}
        
        doEffects(pp);
        
       
        //Check for new collisions involving the balls
	ballIterator = pp.balls.iterator();
	while(ballIterator.hasNext()) {
	    Ball ball = ballIterator.next();
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
	    Ball ball = ballIterator.next();
	    if(ball != ball1 && ball != ball2){
		double t = ball2.detectCollisionWith(ball);
		t += time;
		if(t <= 1 && t >= time){
		    pp.collisions.add(new BallCollision(t, ball2, ball));
		}
	    }
	}
        
	detectPolygonCollisions(pp, ball1);
	pp.checkPockets(ball1, time);
	detectPolygonCollisions(pp, ball2);
	pp.checkPockets(ball2, time);
    }
    
    @Override public void doEffects(PoolPanel pp) {
	//Remove collisions involving ball2
	Iterator<Collision> collIterator = pp.collisions.iterator();
	while(collIterator.hasNext()) {
	    Collision item = collIterator.next();
	    if(item.involves(ball2)) {
		collIterator.remove();
	    }
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
    
    @Override public boolean involves(Ball b) {
        return (b == ball1 || b == ball2);
    }
}

class WallCollision extends Collision {
    Point2D.Double newVel;
    PoolPolygon poly;
    int wall;
    public WallCollision(double t, Ball b, Point2D.Double v, PoolPolygon p, int w) {
	time = t;
	ball1 = b;
        newVel = v;
        poly = p;
        wall = w;
    }

    @Override public void doEffects(PoolPanel pp) {
        ball1.vel = newVel;
    }
    
    @Override public void detectPolygonCollisions(PoolPanel pp, Ball x) {
        pp.detectPolygonCollisions(x, time, this);
    }
}

class PointCollision extends Collision {
    Point point;
    
    public PointCollision(double t, Point p, Ball b) {
	time = t;
	ball1 = b;
	point = p;
    }

    @Override public void doEffects(PoolPanel pp) {
	Point2D.Double unit, trans, temp, res;
	double dist = point.distance(ball1.pos.x, ball1.pos.y);
	unit = new Point2D.Double((point.x - ball1.pos.x)/dist,
				  (point.y - ball1.pos.y)/dist );
	trans = new Point2D.Double(1/(unit.x + unit.y*unit.y/unit.x), 1/(unit.y + unit.x*unit.x/unit.y));
	temp = new Point2D.Double(trans.x*ball1.vel.x, trans.y*ball1.vel.x);
	temp.x += trans.y*ball1.vel.y;
	temp.y += -trans.x*ball1.vel.y;
	temp.x = -temp.x;
	
	res = new Point2D.Double(temp.x*unit.x, temp.x*unit.y);
	res.x += temp.y*unit.y;
	res.y += -temp.y*unit.x;
	ball1.vel = res;
    }
}

class PocketCollision extends Collision {
    public PocketCollision(Ball b, double t) {
	ball1 = b;
	time = t;
    }
    
    @Override public void doCollision(PoolPanel pp) {
        Iterator<Collision> collIterator = pp.collisions.iterator();
        while(collIterator.hasNext()) {
            Collision item = collIterator.next();
            if(item.involves(ball1)) {
                collIterator.remove();
            }
	}
	doEffects(pp);
    }
    
    @Override public void doEffects(PoolPanel pp) {
	ball1.vel.x = 0;
	ball1.vel.y = 0;
	ball1.sunk = true;
    }
}
